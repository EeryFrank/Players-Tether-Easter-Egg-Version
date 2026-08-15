package qizhang.playerleash;

final class TamingSchedule {
    static final int MAX_LAYERS = 6;
    private static final int[] NEXT_LAYER_SECONDS = {30, 25, 20, 15, 10, 5};

    private TamingSchedule() {
    }

    static int secondsForNextLayer(int currentLayers) {
        if (currentLayers < 0 || currentLayers >= MAX_LAYERS) {
            throw new IllegalArgumentException("currentLayers must be between 0 and 5");
        }
        return NEXT_LAYER_SECONDS[currentLayers];
    }

    static int effectDurationSeconds(int layers) {
        if (layers < 0 || layers > MAX_LAYERS) {
            throw new IllegalArgumentException("layers must be between 0 and 6");
        }
        return layers * 10;
    }
}
