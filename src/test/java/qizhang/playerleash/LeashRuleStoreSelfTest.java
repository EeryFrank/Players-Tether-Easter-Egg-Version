package qizhang.playerleash;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LeashRuleStoreSelfTest {
    private LeashRuleStoreSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("qizhang-player-leash-rules-");
        Path file = directory.resolve("qizhang-player-leash.properties");

        LeashRuleStore store = new LeashRuleStore(file);
        store.load();
        check(store.defaultAllowed(), "default must be allow");
        check(store.isAllowed("Alice", "Bob"), "default allow must apply");

        store.setRule("Alice", "Bob", false);
        check(!store.isAllowed("alice", "BOB"), "pair rule must be case-insensitive");
        check(store.isAllowed("Bob", "Alice"), "pair rule must be directional");

        store.setDefaultAllowed(false);
        store.setRule("Bob", "Alice", true);
        check(!store.isAllowed("Other", "Alice"), "default deny must apply");
        check(store.isAllowed("BOB", "alice"), "allow override must beat default deny");

        LeashRuleStore reloaded = new LeashRuleStore(file);
        reloaded.load();
        check(!reloaded.defaultAllowed(), "default must survive reload");
        check(!reloaded.isAllowed("Alice", "Bob"), "deny must survive reload");
        check(reloaded.isAllowed("Bob", "Alice"), "allow must survive reload");
        check(reloaded.ruleCount() == 2, "two rules expected after reload");

        check(reloaded.clearRule("Alice", "Bob"), "clear must remove existing rule");
        check(!reloaded.isAllowed("Alice", "Bob"), "clear must fall back to default deny");
        check(!Files.exists(file.resolveSibling(file.getFileName() + ".tmp")),
                "atomic temporary file must not remain");

        boolean invalidRejected = false;
        try {
            reloaded.setRule("bad.name", "Bob", true);
        } catch (IllegalArgumentException expected) {
            invalidRejected = true;
        }
        check(invalidRejected, "invalid player name must be rejected");

        int totalTamingSeconds = 0;
        int[] expectedSchedule = {30, 25, 20, 15, 10, 5};
        for (int currentLayers = 0; currentLayers < expectedSchedule.length; currentLayers++) {
            int seconds = TamingSchedule.secondsForNextLayer(currentLayers);
            check(seconds == expectedSchedule[currentLayers],
                    "unexpected taming threshold for layer " + (currentLayers + 1));
            totalTamingSeconds += seconds;
        }
        check(totalTamingSeconds == 105, "six taming layers must take 105 seconds total");
        check(TamingSchedule.effectDurationSeconds(6) == 60,
                "sixth layer must retain for 60 seconds after release");

        System.out.println("LEASH_RULE_STORE_SELF_TEST=PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
