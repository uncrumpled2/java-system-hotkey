#!/usr/bin/env bash
# Run example with X11 libraries from nix store

cd "$(dirname "$0")"

# Quick lookup using locate or cached paths
find_nix_lib() {
    local lib=$1
    # Try locate first (fastest)
    if command -v locate &>/dev/null; then
        locate -l1 "/nix/store/*${lib}" 2>/dev/null && return
    fi
    # Fallback to direct glob (slower but works)
    echo /nix/store/*-${lib%%.so*}*/lib/${lib} 2>/dev/null | head -1
}

X11_LIB=$(find_nix_lib libX11.so.6)
XCB_LIB=$(find_nix_lib libxcb.so.1)

if [ -z "$X11_LIB" ] || [ ! -f "$X11_LIB" ]; then
    echo "Error: libX11 not found. Install: nix-env -iA nixpkgs.xorg.libX11"
    exit 1
fi

X11_PATH=$(dirname "$X11_LIB")
XCB_PATH=$(dirname "$XCB_LIB" 2>/dev/null)

export LD_LIBRARY_PATH="$X11_PATH:$XCB_PATH:$LD_LIBRARY_PATH"

echo "Using X11 from: $X11_PATH"
exec java -cp build/classes -Djava.library.path="$(pwd)/src/main/resources/natives/linux-x86_64" Example "$@"
