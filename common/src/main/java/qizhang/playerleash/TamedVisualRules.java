// SPDX-License-Identifier: GPL-3.0-only

package qizhang.playerleash;

/** Pure rules shared by every loader and Minecraft-version renderer. */
public final class TamedVisualRules {
    private static final int WOLF_MODEL_FIRST_LAYER = 3;
    private static final int HEART_PARTICLE_FIRST_LAYER = 6;

    private TamedVisualRules() {
    }

    public static boolean usesWolfModel(int amplifier) {
        return amplifier + 1 >= WOLF_MODEL_FIRST_LAYER;
    }

    public static boolean showsHeartParticles(int amplifier) {
        return amplifier + 1 >= HEART_PARTICLE_FIRST_LAYER;
    }
}
