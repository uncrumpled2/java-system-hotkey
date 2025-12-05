package app.uncrumpled.systemhotkey;

import java.util.Objects;

/**
 * Immutable hotkey representation.
 * Combines modifiers and a key code.
 */
public final class Hotkey {
    private final int modifiers;
    private final int key;

    private Hotkey(int modifiers, int key) {
        this.modifiers = modifiers;
        this.key = key;
    }

    /**
     * Create a hotkey with the given modifiers and key.
     */
    public static Hotkey of(int modifiers, int key) {
        return new Hotkey(modifiers, key);
    }

    /**
     * Create a hotkey with no modifiers.
     */
    public static Hotkey of(int key) {
        return new Hotkey(Modifier.NONE, key);
    }

    /**
     * Create a hotkey from raw values (used by JNI).
     */
    static Hotkey fromNative(int modifiers, int key) {
        return new Hotkey(modifiers, key);
    }

    public int modifiers() {
        return modifiers;
    }

    public int key() {
        return key;
    }

    /**
     * Check if this hotkey has a specific modifier.
     */
    public boolean hasModifier(int modifier) {
        return Modifier.contains(modifiers, modifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotkey)) return false;
        Hotkey hotkey = (Hotkey) o;
        return modifiers == hotkey.modifiers && key == hotkey.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifiers, key);
    }

    @Override
    public String toString() {
        String modStr = Modifier.toString(modifiers);
        String keyStr = Key.name(key);
        if (modifiers == Modifier.NONE) {
            return keyStr;
        }
        return modStr + "+" + keyStr;
    }
}
