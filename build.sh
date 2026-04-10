#!/usr/bin/env bash
set -e

export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.5-jbr"
export ANDROID_HOME="$HOME/android-sdk"

GRADLE="./gradlew"
APK_DEBUG="app/build/outputs/apk/debug/app-debug.apk"
APK_RELEASE="app/build/outputs/apk/release/app-release-unsigned.apk"
APK_RELEASE_SIGNED="app/build/outputs/apk/release/app-release-signed.apk"

usage() {
    echo "Usage: $0 [debug|release]"
    echo ""
    echo "  debug    Build a debug APK for local testing (default)"
    echo "  release  Build a signed, production-grade release APK"
    echo ""
    echo "Release signing env vars (required for 'release'):"
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

    if [[ -z "$KEYSTORE_PATH" ]]; then
        echo ""
        echo "==> Done (unsigned): $APK_RELEASE"
        echo "    To sign for Play Store, set KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD and re-run."
        return
    fi

    echo "==> Signing APK..."
    "$ANDROID_HOME/build-tools/35.0.0/apksigner" sign \
        --ks "$KEYSTORE_PATH" \
        --ks-pass "pass:$KEYSTORE_PASSWORD" \
        --ks-key-alias "$KEY_ALIAS" \
        --key-pass "pass:$KEY_PASSWORD" \
        --out "$APK_RELEASE_SIGNED" \
        "$APK_RELEASE"

    echo "==> Verifying signature..."
    "$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose "$APK_RELEASE_SIGNED"

    echo ""
    echo "==> Done (signed): $APK_RELEASE_SIGNED"
}

case "${1:-debug}" in
    debug)   build_debug ;;
    release) build_release ;;
    *)       usage ;;
esac
