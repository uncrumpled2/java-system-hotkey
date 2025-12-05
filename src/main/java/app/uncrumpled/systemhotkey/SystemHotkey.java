package app.uncrumpled.systemhotkey;

import java.util.List;
import java.util.ArrayList;

/**
 * System-wide hotkey registration and polling.
 *
 * Usage:
 *   SystemHotkey hotkey = SystemHotkey.create();
 *   hotkey.register(Hotkey.of(Modifier.CONTROL | Modifier.SHIFT, Key.A));
 *
 *   // In your event loop:
 *   for (Hotkey h : hotkey.poll()) {
 *       System.out.println("Triggered: " + h);
 *   }
 *
 *   hotkey.close();
 *
 * Clojure usage:
 *   (def hotkey (SystemHotkey/create))
 *   (.register hotkey (Hotkey/of (bit-or Modifier/CONTROL Modifier/SHIFT) Key/A))
 *   (doseq [h (.poll hotkey)] (println "Triggered:" h))
 *   (.close hotkey)
 */
public final class SystemHotkey implements AutoCloseable {
    private long nativePtr;
    private boolean closed = false;

    static {
        NativeLoader.load();
    }

    private SystemHotkey(long ptr) {
        this.nativePtr = ptr;
    }

    /**
     * Create a new SystemHotkey context.
     */
    public static SystemHotkey create() {
        long ptr = nativeInit();
        if (ptr == 0) {
            throw new RuntimeException("Failed to initialize native hotkey context");
        }
        return new SystemHotkey(ptr);
    }

    /**
     * Register a hotkey.
     * Returns true if registration succeeded.
     */
    public boolean register(Hotkey hotkey) {
        checkNotClosed();
        return nativeRegister(nativePtr, hotkey.modifiers(), hotkey.key());
    }

    /**
     * Register a hotkey with modifiers and key.
     * Convenience method for direct registration.
     */
    public boolean register(int modifiers, int key) {
        return register(Hotkey.of(modifiers, key));
    }

    /**
     * Unregister a previously registered hotkey.
     * Returns true if unregistration succeeded.
     */
    public boolean unregister(Hotkey hotkey) {
        checkNotClosed();
        return nativeUnregister(nativePtr, hotkey.modifiers(), hotkey.key());
    }

    /**
     * Unregister a hotkey with modifiers and key.
     */
    public boolean unregister(int modifiers, int key) {
        return unregister(Hotkey.of(modifiers, key));
    }

    /**
     * Poll for triggered hotkeys.
     * Returns a list of hotkeys that were triggered since the last poll.
     * This should be called regularly in your application's event loop.
     */
    public List<Hotkey> poll() {
        checkNotClosed();
        int[] raw = nativePoll(nativePtr);
        List<Hotkey> result = new ArrayList<>(raw.length / 2);
        for (int i = 0; i < raw.length; i += 2) {
            result.add(Hotkey.fromNative(raw[i], raw[i + 1]));
        }
        return result;
    }

    /**
     * Check if this context has been closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Close and release native resources.
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            nativeShutdown(nativePtr);
            nativePtr = 0;
        }
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("SystemHotkey context has been closed");
        }
    }

    // Native methods
    private static native long nativeInit();
    private static native void nativeShutdown(long ptr);
    private static native boolean nativeRegister(long ptr, int modifiers, int key);
    private static native boolean nativeUnregister(long ptr, int modifiers, int key);
    private static native int[] nativePoll(long ptr);
}
