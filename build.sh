#!/usr/bin/env bash
set -e

export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.5-jbr"
export ANDROID_HOME="$HOME/android-sdk"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GODOT_LIB_DIR="$SCRIPT_DIR/../Android-Godot-Futoshiki-library"
GRADLE="./gradlew"
APK_DEBUG="app/build/outputs/apk/debug/app-debug.apk"
APK_RELEASE="app/build/outputs/apk/release/app-release.apk"
AAB_RELEASE="app/build/outputs/bundle/release/app-release.aab"

usage() {
    echo "Usage: $0 [debug|release|aab|pipeline]"
    echo ""
    echo "  debug     Build a debug APK for local testing (default)"
    echo "  release   Build a signed release APK"
    echo "  aab       Build a release Android App Bundle (for Play Store)"
    echo "  pipeline  Export Godot animation PCK first, then produce the release AAB"
    echo ""
    echo "Release signing env vars (required for 'release' / 'aab' / 'pipeline'):"
    echo "  KEYSTORE_PATH      Path to your .jks keystore file"
    echo "  KEYSTORE_PASSWORD  Keystore password"
    echo "  KEY_ALIAS          Key alias"
    echo "  KEY_PASSWORD       Key password"
    exit 1
}

check_animation_pck() {
    if [[ ! -f "app/src/main/assets/data.pck" ]]; then
        echo "==> WARNING: app/src/main/assets/data.pck not found."
        echo "    Run the pipeline mode or export the PCK manually:"
        echo "      cd $GODOT_LIB_DIR && ./build.sh pck"
        echo "    Continuing without animation content..."
    fi
}

build_godot_pck() {
    echo "==> Exporting Godot animation PCK..."
    if [[ ! -d "$GODOT_LIB_DIR" ]]; then
        echo "ERROR: Godot library not found at $GODOT_LIB_DIR"
        exit 1
    fi
    pushd "$GODOT_LIB_DIR" > /dev/null
    ./build.sh pck
    popd > /dev/null
    echo "==> PCK ready at app/src/main/assets/data.pck"
}

build_debug() {
    check_animation_pck
    echo "==> Building debug APK..."
    $GRADLE assembleDebug
    echo ""
    echo "==> Done: $APK_DEBUG"
}

build_release() {
    check_animation_pck
    echo "==> Building release APK (minified + ProGuard)..."
    $GRADLE assembleRelease

    if [[ -z "${KEYSTORE_PATH:-}" ]]; then
        echo ""
        echo "==> Done (unsigned): $APK_RELEASE"
        echo "    To sign, set KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD and re-run."
        return
    fi

    APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name "apksigner" | sort -V | tail -1)
    if [[ -n "$APKSIGNER" ]]; then
        echo "==> Verifying signature..."
        "$APKSIGNER" verify --verbose "$APK_RELEASE"
    fi

    echo ""
    echo "==> Done (signed): $APK_RELEASE"
}

build_aab() {
    check_animation_pck
    echo "==> Building release AAB (Android App Bundle)..."
    $GRADLE bundleRelease
    echo ""
    echo "==> Done: $AAB_RELEASE"
}

build_pipeline() {
    echo "==> Full pipeline: Godot PCK → Android App Bundle"
    echo ""
    build_godot_pck
    echo ""
    build_aab
}

case "${1:-debug}" in
    debug|--debug)       build_debug ;;
    release|--release)   build_release ;;
    aab|--aab)           build_aab ;;
    pipeline|--pipeline) build_pipeline ;;
    *)                   usage ;;
esac
