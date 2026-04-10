#!/usr/bin/env bash
set -e

# ─── Usage ────────────────────────────────────────────────────────────────────
usage() {
    echo "Usage: $0"
    echo ""
    echo "  Generates a release keystore for signing Futoshiki APKs."
    echo "  Run this once on the admin machine and share the output .jks with the developer."
    exit 1
}

[[ "${1:-}" == "-h" || "${1:-}" == "--help" ]] && usage

# ─── Check keytool is available ───────────────────────────────────────────────
if ! command -v keytool &> /dev/null; then
    echo "Error: 'keytool' not found. Install a JDK first."
    echo "  Ubuntu/Debian: sudo apt install default-jdk"
    echo "  macOS:         brew install openjdk"
    exit 1
fi

# ─── Collect info ─────────────────────────────────────────────────────────────
echo "─────────────────────────────────────────────────────────"
echo " Futoshiki Release Keystore Generator"
echo "─────────────────────────────────────────────────────────"
echo ""
echo "You will be prompted for keystore details."
echo "Keep these credentials safe — they cannot be recovered if lost."
echo ""

read -rp  "  Key alias (e.g. futoshiki)         : " KEY_ALIAS
read -rsp "  Keystore password (min 6 chars)    : " KEYSTORE_PASSWORD; echo ""
read -rsp "  Confirm keystore password          : " KEYSTORE_PASSWORD_2; echo ""

if [[ "$KEYSTORE_PASSWORD" != "$KEYSTORE_PASSWORD_2" ]]; then
    echo "Error: Keystore passwords do not match."
    exit 1
fi

read -rsp "  Key password (min 6 chars)         : " KEY_PASSWORD; echo ""
read -rsp "  Confirm key password               : " KEY_PASSWORD_2; echo ""

if [[ "$KEY_PASSWORD" != "$KEY_PASSWORD_2" ]]; then
    echo "Error: Key passwords do not match."
    exit 1
fi

echo ""
echo "Certificate identity (can be your name/org — embedded in the APK signature):"
read -rp  "  Full name or org (e.g. HeX Corp)   : " DNAME_CN
read -rp  "  Country code (2 letters, e.g. US)  : " DNAME_C

OUTPUT_DIR="$HOME/keystores"
OUTPUT_FILE="$OUTPUT_DIR/futoshiki-release.jks"

mkdir -p "$OUTPUT_DIR"

# ─── Generate ─────────────────────────────────────────────────────────────────
echo ""
echo "Generating keystore..."

keytool -genkeypair \
    -v \
    -keystore "$OUTPUT_FILE" \
    -storetype JKS \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias "$KEY_ALIAS" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=$DNAME_CN, C=$DNAME_C"

chmod 600 "$OUTPUT_FILE"

# ─── Summary ──────────────────────────────────────────────────────────────────
echo ""
echo "─────────────────────────────────────────────────────────"
echo "✓ Keystore generated: $OUTPUT_FILE"
echo "─────────────────────────────────────────────────────────"
echo ""
echo "Share these with the developer (keep them secure):"
echo "  File             : $OUTPUT_FILE"
echo "  Key alias        : $KEY_ALIAS"
echo "  Keystore password: (what you just set)"
echo "  Key password     : (what you just set)"
echo ""
echo "The developer runs setup-keystore.sh with this file to configure their machine and GitHub Actions."
echo "─────────────────────────────────────────────────────────"
