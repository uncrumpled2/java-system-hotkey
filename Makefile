# Makefile for java-system-hotkey

JAVA_HOME ?= $(shell dirname $(shell dirname $(shell readlink -f $(shell which java))))
JAI_SRC_DIR = ../jai-system-hotkey

# Platform detection
UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)

ifeq ($(UNAME_S),Linux)
    PLATFORM = linux
    LIB_EXT = .so
    LIB_PREFIX = lib
    JAI_LIB_PREFIX =
    JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
    LDFLAGS = -shared -fPIC
    X11_LIBS = -lX11
endif
ifeq ($(UNAME_S),Darwin)
    PLATFORM = macos
    LIB_EXT = .dylib
    LIB_PREFIX = lib
    JAI_LIB_PREFIX =
    JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin
    LDFLAGS = -dynamiclib
    X11_LIBS =
endif
ifeq ($(findstring MINGW,$(UNAME_S)),MINGW)
    PLATFORM = windows
    LIB_EXT = .dll
    LIB_PREFIX =
    JAI_LIB_PREFIX =
    JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32
    LDFLAGS = -shared
    X11_LIBS =
endif

ifeq ($(UNAME_M),x86_64)
    ARCH = x86_64
else ifeq ($(UNAME_M),amd64)
    ARCH = x86_64
else ifeq ($(UNAME_M),aarch64)
    ARCH = aarch64
else ifeq ($(UNAME_M),arm64)
    ARCH = aarch64
endif

# Output directories
BUILD_DIR = build
CLASSES_DIR = $(BUILD_DIR)/classes
NATIVES_DIR = src/main/resources/natives/$(PLATFORM)-$(ARCH)
JAI_LIB = $(NATIVES_DIR)/$(LIB_PREFIX)system_hotkey$(LIB_EXT)
JNI_LIB = $(NATIVES_DIR)/$(LIB_PREFIX)system_hotkey_jni$(LIB_EXT)

# Java sources
JAVA_SRCS = $(wildcard src/main/java/app/uncrumpled/systemhotkey/*.java)

.PHONY: all clean java jni jai example mvn-publish

all: java jni

# Compile Java sources
java: $(CLASSES_DIR)
	javac -d $(CLASSES_DIR) $(JAVA_SRCS)

$(CLASSES_DIR):
	mkdir -p $(CLASSES_DIR)

# Build JNI library (requires Jai library to be built first)
jni: $(JNI_LIB)

$(NATIVES_DIR):
	mkdir -p $(NATIVES_DIR)

$(JNI_LIB): src/main/c/system_hotkey_jni.c src/main/c/system_hotkey_jni.h $(JAI_LIB) | $(NATIVES_DIR)
	$(CC) $(LDFLAGS) $(JNI_INCLUDES) -I$(NATIVES_DIR) \
		-o $@ src/main/c/system_hotkey_jni.c \
		-L$(NATIVES_DIR) -lsystem_hotkey $(X11_LIBS) \
		-Wl,-rpath,'$$ORIGIN'

# Copy Jai library (must be built separately in jai-system-hotkey)
jai: $(JAI_LIB)

$(JAI_LIB): | $(NATIVES_DIR)
	@echo "Copying Jai library from $(JAI_SRC_DIR)..."
	@if [ -f "$(JAI_SRC_DIR)/lib/$(JAI_LIB_PREFIX)system_hotkey$(LIB_EXT)" ]; then \
		cp "$(JAI_SRC_DIR)/lib/$(JAI_LIB_PREFIX)system_hotkey$(LIB_EXT)" "$@"; \
	else \
		echo "Error: Jai library not found. Build it first with:"; \
		echo "  cd $(JAI_SRC_DIR) && jai build_shared.jai"; \
		exit 1; \
	fi

# Run example
# For nix: set NIX_X11_PATH to your nix X11 lib path, e.g.:
#   make example NIX_X11_PATH=/nix/store/xxx-libX11-xxx/lib:/nix/store/xxx-libxcb-xxx/lib
NIX_X11_PATH ?=
example: java jni
	javac -cp $(CLASSES_DIR) -d $(CLASSES_DIR) examples/Example.java
	LD_LIBRARY_PATH="$(NIX_X11_PATH):$$LD_LIBRARY_PATH" \
		java -cp $(CLASSES_DIR) -Djava.library.path=$(shell pwd)/$(NATIVES_DIR) Example

# Generate JNI header (optional, for reference)
jni-header: java
	javac -h src/main/c -d $(CLASSES_DIR) src/main/java/app/uncrumpled/systemhotkey/SystemHotkey.java

# Create JAR
jar: java
	mkdir -p $(BUILD_DIR)
	jar cf $(BUILD_DIR)/system-hotkey.jar -C $(CLASSES_DIR) .

# Clean build artifacts
clean:
	rm -rf $(BUILD_DIR)
	rm -f $(JNI_LIB)

# Clean all including native libs
clean-all: clean
	rm -rf src/main/resources/natives

# Publish to Maven Central (via Central Portal)
# Requires: GPG key configured, ~/.m2/settings.xml with Central Portal credentials
# Note: Central Portal does NOT support snapshots - remove -SNAPSHOT from version first
mvn-publish: jni
	mvn clean deploy -P release

# Help
help:
	@echo "Targets:"
	@echo "  all                 - Build Java and JNI (default)"
	@echo "  java                - Compile Java sources"
	@echo "  jni                 - Build JNI native library"
	@echo "  jai                 - Copy Jai library (must be built separately)"
	@echo "  example             - Run the example program"
	@echo "  jar                 - Create JAR file"
	@echo "  mvn-publish         - Deploy release to Maven Central"
	@echo "  clean               - Clean build artifacts"
	@echo "  clean-all           - Clean everything including natives"
	@echo ""
	@echo "Build the Jai library first:"
	@echo "  cd $(JAI_SRC_DIR) && jai build_shared.jai"
	@echo ""
	@echo "Maven publishing requires:"
	@echo "  - GPG key configured for signing"
	@echo "  - ~/.m2/settings.xml with OSSRH credentials"
	@echo "  - For release: update version in pom.xml (remove -SNAPSHOT)"
