#!/usr/bin/env bash
set -e

export JAVA_HOME="${JAVA_HOME:-$HOME/.sdkman/candidates/java/21.0.5-jbr}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE="./gradlew"
APK_DEBUG="app/build/outputs/apk/debug/app-debug.apk"
APK_RELEASE="app/build/outputs/apk/release/app-release.apk"
AAB_RELEASE="app/build/outputs/bundle/release/app-release.aab"

usage() {
    echo "Usage: $0 [debug|release|aab]"
    echo ""
    echo "  debug     Build a debug APK for local testing (default)"
    echo "  release   Build a signed release APK"
    echo "  aab       Build a release Android App Bundle (for Play Store)"
    echo ""
    echo "Release signing env vars (required for 'release' / 'aab'):"
    echo "  KEYSTORE_PATH      Path to your .jks keystore file"
    echo "  KEYSTORE_PASSWORD  Keystore password"
    echo "  KEY_ALIAS          Key alias"
    echo "  KEY_PASSWORD       Key password"
    exit 1
}

build_debug() {
    echo "==> Building debug APK..."
    $GRADLE assembleDebug
    echo ""
    echo "==> Done: $APK_DEBUG"
}

build_release() {
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
    echo "==> Building release AAB (Android App Bundle)..."
    $GRADLE bundleRelease
    echo ""
    echo "==> Done: $AAB_RELEASE"
}

case "${1:-debug}" in
    debug|--debug)       build_debug ;;
    release|--release)   build_release ;;
    aab|--aab)           build_aab ;;
    *)                   usage ;;
esac
