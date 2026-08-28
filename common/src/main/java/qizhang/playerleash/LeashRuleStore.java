// SPDX-License-Identifier: LGPL-3.0-or-later

package qizhang.playerleash;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

final class LeashRuleStore {
    private static final String DEFAULT_KEY = "default";
    private static final String RULE_PREFIX = "rule.";
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private final Path file;
    private final Map<Pair, Boolean> rules = new LinkedHashMap<>();
    private boolean defaultAllowed = true;

    LeashRuleStore(Path file) {
        this.file = file;
    }

    synchronized void load() throws IOException {
        if (!Files.exists(file)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        boolean loadedDefault = parseDecision(properties.getProperty(DEFAULT_KEY), true);
        Map<Pair, Boolean> loadedRules = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(RULE_PREFIX)) {
                continue;
            }
            String pairText = key.substring(RULE_PREFIX.length());
            int separator = pairText.indexOf('.');
            if (separator <= 0 || separator >= pairText.length() - 1) {
                continue;
            }
            String holder = normalize(pairText.substring(0, separator));
            String target = normalize(pairText.substring(separator + 1));
            if (!validName(holder) || !validName(target)) {
                continue;
            }
            String value = properties.getProperty(key);
            if (!"allow".equalsIgnoreCase(value) && !"deny".equalsIgnoreCase(value)) {
                continue;
            }
            loadedRules.put(new Pair(holder, target), "allow".equalsIgnoreCase(value));
        }
        defaultAllowed = loadedDefault;
        rules.clear();
        rules.putAll(loadedRules);
    }

    synchronized void reload() throws IOException {
        load();
    }

    synchronized boolean isAllowed(String holder, String target) {
        return rules.getOrDefault(new Pair(normalize(holder), normalize(target)), defaultAllowed);
    }

    synchronized boolean defaultAllowed() {
        return defaultAllowed;
    }

    synchronized int ruleCount() {
        return rules.size();
    }

    synchronized void setDefaultAllowed(boolean allowed) throws IOException {
        boolean previous = defaultAllowed;
        defaultAllowed = allowed;
        try {
            save();
        } catch (IOException exception) {
            defaultAllowed = previous;
            throw exception;
        }
    }

    synchronized void setRule(String holder, String target, boolean allowed) throws IOException {
        requireName(holder);
        requireName(target);
        Pair pair = new Pair(normalize(holder), normalize(target));
        Boolean previous = rules.put(pair, allowed);
        try {
            save();
        } catch (IOException exception) {
            if (previous == null) {
                rules.remove(pair);
            } else {
                rules.put(pair, previous);
            }
            throw exception;
        }
    }

    synchronized boolean clearRule(String holder, String target) throws IOException {
        requireName(holder);
        requireName(target);
        Pair pair = new Pair(normalize(holder), normalize(target));
        Boolean previous = rules.remove(pair);
        boolean removed = previous != null;
        if (removed) {
            try {
                save();
            } catch (IOException exception) {
                rules.put(pair, previous);
                throw exception;
            }
        }
        return removed;
    }

    synchronized List<RuleEntry> entries() {
        ArrayList<RuleEntry> result = new ArrayList<>();
        for (Map.Entry<Pair, Boolean> entry : rules.entrySet()) {
            result.add(new RuleEntry(entry.getKey().holder(), entry.getKey().target(), entry.getValue()));
        }
        result.sort(Comparator.comparing(RuleEntry::holder).thenComparing(RuleEntry::target));
        return List.copyOf(result);
    }

    static boolean validName(String name) {
        return name != null && PLAYER_NAME.matcher(name).matches();
    }

    private static void requireName(String name) {
        if (!validName(name)) {
            throw new IllegalArgumentException("玩家名必须为 1-16 位英文字母、数字或下划线");
        }
    }

    private synchronized void save() throws IOException {
        Files.createDirectories(file.getParent());
        Properties properties = new Properties();
        properties.setProperty(DEFAULT_KEY, defaultAllowed ? "allow" : "deny");
        for (Map.Entry<Pair, Boolean> entry : rules.entrySet()) {
            Pair pair = entry.getKey();
            properties.setProperty(
                    RULE_PREFIX + pair.holder() + "." + pair.target(),
                    entry.getValue() ? "allow" : "deny");
        }

        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            properties.store(writer, "QiZhang player leash rules; direction is holder -> target");
        }
        try {
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean parseDecision(String value, boolean fallback) {
        if ("allow".equalsIgnoreCase(value)) {
            return true;
        }
        if ("deny".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    record RuleEntry(String holder, String target, boolean allowed) {
    }

    private record Pair(String holder, String target) {
    }
}
