package app.uncrumpled.systemhotkey;

import java.io.*;
import java.nio.file.*;

/**
 * Utility for loading the native library from JAR resources or java.library.path.
 */
final class NativeLoader {
    private static final String JAI_LIB_NAME = "system_hotkey";
    private static final String JNI_LIB_NAME = "system_hotkey_jni";
    private static boolean loaded = false;

    private NativeLoader() {}

    static synchronized void load() {
        if (loaded) return;

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String platform;
        String libExtension;
        String libPrefix;

        if (osName.contains("linux")) {
            platform = "linux";
            libExtension = ".so";
            libPrefix = "lib";
        } else if (osName.contains("windows")) {
            platform = "windows";
            libExtension = ".dll";
            libPrefix = "";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            platform = "macos";
            libExtension = ".dylib";
            libPrefix = "lib";
        } else {
            throw new UnsatisfiedLinkError("Unsupported OS: " + osName);
        }

        String arch;
        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            arch = "x86_64";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            arch = "aarch64";
        } else {
            throw new UnsatisfiedLinkError("Unsupported architecture: " + osArch);
        }

        // Try java.library.path first
        String libraryPath = System.getProperty("java.library.path");
        if (libraryPath != null) {
            for (String pathDir : libraryPath.split(File.pathSeparator)) {
                Path jaiLib = Paths.get(pathDir, libPrefix + JAI_LIB_NAME + libExtension);
                Path jniLib = Paths.get(pathDir, libPrefix + JNI_LIB_NAME + libExtension);

                if (Files.exists(jaiLib) && Files.exists(jniLib)) {
                    System.load(jaiLib.toAbsolutePath().toString());
                    System.load(jniLib.toAbsolutePath().toString());
                    loaded = true;
                    return;
                }
            }
        }

        // Fall back to extracting from JAR resources
        String nativesDir = "/natives/" + platform + "-" + arch + "/";
        String jaiResource = nativesDir + libPrefix + JAI_LIB_NAME + libExtension;
        String jniResource = nativesDir + libPrefix + JNI_LIB_NAME + libExtension;

        try {
            Path tempDir = Files.createTempDirectory("system-hotkey-natives");
            tempDir.toFile().deleteOnExit();

            // Extract and load Jai library first (dependency)
            Path jaiTempLib = extractResource(jaiResource, tempDir, libPrefix + JAI_LIB_NAME + libExtension);
            System.load(jaiTempLib.toAbsolutePath().toString());

            // Extract and load JNI library
            Path jniTempLib = extractResource(jniResource, tempDir, libPrefix + JNI_LIB_NAME + libExtension);
            System.load(jniTempLib.toAbsolutePath().toString());

            loaded = true;

        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to extract native library: " + e.getMessage());
        }
    }

    private static Path extractResource(String resourcePath, Path destDir, String fileName) throws IOException {
        try (InputStream is = NativeLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new UnsatisfiedLinkError("Native library not found in JAR: " + resourcePath);
            }

            Path destFile = destDir.resolve(fileName);
            destFile.toFile().deleteOnExit();
            Files.copy(is, destFile, StandardCopyOption.REPLACE_EXISTING);
            return destFile;
        }
    }
}
