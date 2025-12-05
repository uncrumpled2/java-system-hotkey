package app.uncrumpled.systemhotkey;

/**
 * Keyboard modifiers for hotkeys.
 * Can be combined using bitwise OR or the combine() method.
 */
public final class Modifier {
    public static final int NONE    = 0x0;
    public static final int CONTROL = 0x1;
    public static final int SHIFT   = 0x2;
    public static final int ALT     = 0x4;
    public static final int SUPER   = 0x8; // Windows key / Command key

    private Modifier() {}

    /**
     * Combine multiple modifiers.
     * Example: Modifier.combine(Modifier.CONTROL, Modifier.SHIFT)
     */
    public static int combine(int... modifiers) {
        int result = 0;
        for (int m : modifiers) {
            result |= m;
        }
        return result;
    }

    /**
     * Check if a modifier set contains a specific modifier.
     */
    public static boolean contains(int modifiers, int modifier) {
        return (modifiers & modifier) != 0;
    }

    /**
     * Convert modifiers to a human-readable string.
     */
    public static String toString(int modifiers) {
        if (modifiers == NONE) return "NONE";

        StringBuilder sb = new StringBuilder();
        if ((modifiers & CONTROL) != 0) sb.append("CONTROL+");
        if ((modifiers & SHIFT) != 0) sb.append("SHIFT+");
        if ((modifiers & ALT) != 0) sb.append("ALT+");
        if ((modifiers & SUPER) != 0) sb.append("SUPER+");

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing +
        }
        return sb.toString();
    }
}
