package app.uncrumpled.systemhotkey;

/**
 * Key codes matching the Jai enum ordering.
 * Values correspond to the ordinal position in the Jai Key enum.
 */
public final class Key {
    // Letters
    public static final int A = 0;
    public static final int B = 1;
    public static final int C = 2;
    public static final int D = 3;
    public static final int E = 4;
    public static final int F = 5;
    public static final int G = 6;
    public static final int H = 7;
    public static final int I = 8;
    public static final int J = 9;
    public static final int K = 10;
    public static final int L = 11;
    public static final int M = 12;
    public static final int N = 13;
    public static final int O = 14;
    public static final int P = 15;
    public static final int Q = 16;
    public static final int R = 17;
    public static final int S = 18;
    public static final int T = 19;
    public static final int U = 20;
    public static final int V = 21;
    public static final int W = 22;
    public static final int X = 23;
    public static final int Y = 24;
    public static final int Z = 25;

    // Numbers
    public static final int NUM_0 = 26;
    public static final int NUM_1 = 27;
    public static final int NUM_2 = 28;
    public static final int NUM_3 = 29;
    public static final int NUM_4 = 30;
    public static final int NUM_5 = 31;
    public static final int NUM_6 = 32;
    public static final int NUM_7 = 33;
    public static final int NUM_8 = 34;
    public static final int NUM_9 = 35;

    // Function keys
    public static final int F1  = 36;
    public static final int F2  = 37;
    public static final int F3  = 38;
    public static final int F4  = 39;
    public static final int F5  = 40;
    public static final int F6  = 41;
    public static final int F7  = 42;
    public static final int F8  = 43;
    public static final int F9  = 44;
    public static final int F10 = 45;
    public static final int F11 = 46;
    public static final int F12 = 47;

    // Arrow keys
    public static final int UP    = 48;
    public static final int DOWN  = 49;
    public static final int LEFT  = 50;
    public static final int RIGHT = 51;

    // Special keys
    public static final int SPACE  = 52;
    public static final int ENTER  = 53;
    public static final int ESCAPE = 54;
    public static final int TAB    = 55;

    private static final String[] KEY_NAMES = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
        "UP", "DOWN", "LEFT", "RIGHT",
        "SPACE", "ENTER", "ESCAPE", "TAB"
    };

    private Key() {}

    /**
     * Convert a key code to its name.
     */
    public static String name(int key) {
        if (key >= 0 && key < KEY_NAMES.length) {
            return KEY_NAMES[key];
        }
        return "UNKNOWN(" + key + ")";
    }

    /**
     * Parse a key name to its code.
     * Returns -1 if not found.
     */
    public static int fromName(String name) {
        String upper = name.toUpperCase();
        for (int i = 0; i < KEY_NAMES.length; i++) {
            if (KEY_NAMES[i].equals(upper)) {
                return i;
            }
        }
        return -1;
    }
}
