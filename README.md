# java-system-hotkey

Java/Clojure bindings for system-wide hotkey registration via JNI.

## Prerequisites

- JDK 11+
- The Jai compiler (to build the native library)
- GCC or Clang (for JNI bridge)
- X11 libraries (Linux only): `libX11`, `libxcb`, and their dependencies
  - Debian/Ubuntu: `apt install libx11-6 libxcb1`
  - Nix: Add `xorg.libX11` and `xorg.libxcb` to your environment

## Building

### 1. Build the Jai shared library

```bash
cd ../jai-system-hotkey
jai build_shared.jai
```

This creates `lib/libsystem_hotkey.so` (Linux), `lib/libsystem_hotkey.dylib` (macOS), or `lib/system_hotkey.dll` (Windows).

### 2. Build the Java library and JNI bridge

```bash
cd ../java-system-hotkey
make jai    # Copy the Jai library
make all    # Build Java classes and JNI bridge
```

Or with Maven:
```bash
mvn compile
# (Still need to run `make jni` for native libs)
```

### 3. Run the example

```bash
make example
```

## Usage

### Java

```java
import com.systemhotkey.*;

try (SystemHotkey hotkey = SystemHotkey.create()) {
    // Register Ctrl+Shift+A
    int mods = Modifier.combine(Modifier.CONTROL, Modifier.SHIFT);
    hotkey.register(Hotkey.of(mods, Key.A));

    // Poll loop
    while (running) {
        for (Hotkey h : hotkey.poll()) {
            System.out.println("Triggered: " + h);
        }
        Thread.sleep(10);
    }
}
```

### Clojure

```clojure
(import '[com.systemhotkey SystemHotkey Hotkey Modifier Key])

(let [hotkey (SystemHotkey/create)
      mods (bit-or Modifier/CONTROL Modifier/SHIFT)]
  (try
    (.register hotkey (Hotkey/of mods Key/A))
    ;; Poll loop
    (loop []
      (doseq [h (.poll hotkey)]
        (println "Triggered:" h))
      (Thread/sleep 10)
      (recur))
    (finally
      (.close hotkey))))
```

See `examples/ClojureExample.md` for more Clojure patterns.

## Project Structure

```
java-system-hotkey/
├── src/main/java/com/systemhotkey/
│   ├── SystemHotkey.java    # Main API class
│   ├── Hotkey.java          # Immutable hotkey representation
│   ├── Key.java             # Key constants
│   ├── Modifier.java        # Modifier constants
│   └── NativeLoader.java    # Native library loader
├── src/main/c/
│   ├── system_hotkey_jni.c  # JNI bridge implementation
│   └── system_hotkey_jni.h  # JNI header
├── src/main/resources/natives/
│   └── <platform>-<arch>/   # Native libraries
├── examples/
│   ├── Example.java         # Java example
│   └── ClojureExample.md    # Clojure usage examples
├── Makefile
└── pom.xml
```

## API Reference

### SystemHotkey

- `SystemHotkey.create()` - Create a new context
- `.register(Hotkey)` - Register a hotkey
- `.unregister(Hotkey)` - Unregister a hotkey
- `.poll()` - Returns `List<Hotkey>` of triggered hotkeys
- `.close()` - Release resources (also via try-with-resources)

### Hotkey

- `Hotkey.of(modifiers, key)` - Create a hotkey
- `.modifiers()` - Get modifier flags
- `.key()` - Get key code
- `.toString()` - Human-readable format (e.g., "CONTROL+SHIFT+A")

### Modifier

- `NONE`, `CONTROL`, `SHIFT`, `ALT`, `SUPER`
- `Modifier.combine(...)` - Combine multiple modifiers

### Key

- Letters: `A`-`Z`
- Numbers: `NUM_0`-`NUM_9`
- Function keys: `F1`-`F12`
- Arrows: `UP`, `DOWN`, `LEFT`, `RIGHT`
- Special: `SPACE`, `ENTER`, `ESCAPE`, `TAB`

## License

Same license as jai-system-hotkey.

## Maven
  - make mvn-publish - Deploy a release to Maven Central (requires GPG
  signing via -P release profile)

  To use these commands, you'll need:
  1. A GPG key configured for signing (for releases)
  2. ~/.m2/settings.xml with your maven central credentials:

## Misc testing commands

NIX_X11_PATH="/nix/store/26c0x3gh2g5dpczvjxgjzn0mc22zxpjz-libX11-1.8.12/lib:/nix/store/09aq563zkqcw9ikxn02p4bm13i2hz51r-libxcb-1.17.0/lib" make example

LD_LIBRARY_PATH="/nix/store/26c0x3gh2g5dpczvjxgjzn0mc22zxpjz-libX11-1.8.12/lib:/nix/store/09aq563zkqcw9ikxn02p4bm13i2hz51r-libxcb-1.17.0/lib" clj -M:run

LD_LIBRARY_PATH="/nix/store/26c0x3gh2g5dpczvjxgjzn0mc22zxpjz-libX11-1.8.12/lib:/nix/store/09aq563zkqcw9ikxn02p4bm13i2hz51r-libxcb-1.17.0/lib" \
    clj -J-Djava.library.path=/root/programming/repo/java-system-hotkey/
  src/main/resources/natives/linux-x86_64 \
    -M:run

  LD_LIBRARY_PATH="/nix/store/26c0x3gh2g5dpczvjxgjzn0mc22zxpjz-libX11-1.8.12/lib:/nix/store/09aq563zkqcw9ikxn02p4bm13i2hz51r-libxcb-1.17.0/lib
  " clj -J-Djava.library.path=/root/programming/repo/java-system-hotkey/src/main/resources/natives/linux-x86_64 -M:run

## Github Actions
  Workflow features:
  - Manual trigger via workflow_dispatch
  - Parallel builds for all platforms (Linux, macOS x64, macOS arm64, Windows)
  - All artifacts collected and committed in a single commit
  - Optional Maven Central publishing with version tagging

  Required Setup

  Repository Secrets (Settings → Secrets and variables → Actions → Secrets)

  | Secret           | Description                                       |
  |------------------|---------------------------------------------------|
  | AWS_ECR_PASSWORD | ECR password (get via aws ecr get-login-password) |
  | MAVEN_USERNAME   | Maven Central username (for publishing)           |
  | MAVEN_PASSWORD   | Maven Central password/token                      |
  | GPG_PRIVATE_KEY  | ASCII-armored GPG key for signing                 |
  | GPG_PASSPHRASE   | GPG key passphrase                                |

  Repository Variables (Settings → Secrets and variables → Actions → Variables)

  | Variable            | Description                                   |
  |---------------------|-----------------------------------------------|
  | JAI_MACOS_X64_URL   | URL to download Jai for macOS x86_64 (tar.gz) |
  | JAI_MACOS_ARM64_URL | URL to download Jai for macOS arm64 (tar.gz)  |
  | JAI_WINDOWS_URL     | URL to download Jai for Windows (zip)         |

  ECR Password

  Generate with:
  aws ecr get-login-password --region ap-southeast-2

  You may want to set up a scheduled job or use OIDC authentication instead of a static password since ECR tokens
  expire.
