#!/usr/bin/env bash
set -e

check_prereqs() {
    if ! command -v java &>/dev/null; then
        echo "ERROR: Java not found. Install JDK 21+ and ensure 'java' is on PATH."
        exit 1
    fi

    java_version=$(java -version 2>&1 | head -1 | grep -oP '\d+' | head -1)
    if [[ "$java_version" -lt 21 ]]; then
        echo "ERROR: Java 21+ required (found version $java_version)."
        exit 1
    fi

    if [[ -z "${ANDROID_HOME:-}" ]]; then
        echo "ERROR: ANDROID_HOME is not set. Point it to your Android SDK directory."
        exit 1
    fi
    if [[ ! -d "$ANDROID_HOME" ]]; then
        echo "ERROR: ANDROID_HOME ($ANDROID_HOME) does not exist."
        exit 1
    fi
    if [[ ! -d "$ANDROID_HOME/platforms" ]]; then
        echo "ERROR: Android SDK platforms not found under ANDROID_HOME."
        exit 1
    fi

    if [[ ! -f "./gradlew" ]]; then
        echo "ERROR: ./gradlew not found. Run this script from the project root."
        exit 1
    fi
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
GRADLE="./gradlew"
APK_DEBUG="app/build/outputs/apk/debug/app-debug.apk"
APK_RELEASE_UNSIGNED="app/build/outputs/apk/release/app-release-unsigned.apk"
APK_RELEASE_SIGNED="app/build/outputs/apk/release/app-release.apk"
AAB_RELEASE="app/build/outputs/bundle/release/app-release.aab"

usage() {
    echo "Usage: $0 [debug|release|aab]"
    echo ""
    echo "  debug     Build a debug APK for local testing (default)"
    echo "  release   Build a release APK"
    echo "  aab       Build a release Android App Bundle (for Play Store)"
    echo ""
    echo "Optional (for release / aab signing):"
    echo "  Set KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
    exit 1
}

build_debug() {
    check_prereqs
    echo "==> Building debug APK..."
    $GRADLE assembleDebug
    echo ""
    echo "==> Done: $APK_DEBUG"
}

build_release() {
    check_prereqs
    echo "==> Building release APK (minified + ProGuard)..."
    $GRADLE assembleRelease

    if [[ -f "$APK_RELEASE_SIGNED" ]]; then
        APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name "apksigner" | sort -V | tail -1)
        if [[ -n "$APKSIGNER" ]]; then
            echo "==> Verifying signature..."
            "$APKSIGNER" verify --verbose "$APK_RELEASE_SIGNED"
        fi
        echo ""
        echo "==> Done (signed): $APK_RELEASE_SIGNED"
    else
        echo ""
        echo "==> Done (unsigned): $APK_RELEASE_UNSIGNED"
        echo "    To sign, set KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD and re-run."
    fi
}

build_aab() {
    check_prereqs
    echo "==> Building release AAB..."
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
