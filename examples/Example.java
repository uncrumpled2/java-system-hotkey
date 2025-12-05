import app.uncrumpled.systemhotkey.*;
import java.util.List;

/**
 * Example demonstrating system-wide hotkey registration.
 *
 * Registers F9 and F10 hotkeys (simple, no modifiers).
 * Press F10 to quit.
 */
public class Example {
    public static void main(String[] args) {
        System.out.println("System Hotkey Example");
        System.out.println("=====================");
        System.out.println("Press F9 to trigger a hotkey");
        System.out.println("Press F10 to quit");
        System.out.println();

        try (SystemHotkey hotkey = SystemHotkey.create()) {
            // Register simple hotkeys (no modifiers)
            Hotkey hotkeyA = Hotkey.of(Modifier.NONE, Key.F9);
            Hotkey hotkeyQ = Hotkey.of(Modifier.NONE, Key.F10);

            if (hotkey.register(hotkeyA)) {
                System.out.println("Registered: " + hotkeyA);
            } else {
                System.err.println("Failed to register: " + hotkeyA);
            }

            if (hotkey.register(hotkeyQ)) {
                System.out.println("Registered: " + hotkeyQ);
            } else {
                System.err.println("Failed to register: " + hotkeyQ);
            }

            System.out.println("\nListening for hotkeys...\n");

            // Event loop
            boolean running = true;
            while (running) {
                List<Hotkey> triggered = hotkey.poll();

                for (Hotkey h : triggered) {
                    System.out.println("Triggered: " + h);

                    if (h.equals(hotkeyQ)) {
                        System.out.println("F10 pressed, exiting...");
                        running = false;
                    }
                }

                // Sleep to avoid busy-waiting
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    running = false;
                }
            }

            // Cleanup (also handled by try-with-resources)
            hotkey.unregister(hotkeyA);
            hotkey.unregister(hotkeyQ);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
