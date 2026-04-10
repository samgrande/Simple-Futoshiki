#!/usr/bin/env bash
set -e

# ─── Usage ────────────────────────────────────────────────────────────────────
usage() {
    echo "Usage: $0 <path-to-keystore.jks>"
    echo ""
    echo "  Sets up your release keystore for signing and GitHub Actions."
    echo "  Run this once after receiving the .jks file from your admin."
    exit 1
}

[[ $# -lt 1 ]] && usage

JKS_SRC="$1"

if [[ ! -f "$JKS_SRC" ]]; then
    echo "Error: File not found: $JKS_SRC"
    exit 1
fi

# ─── Step 1: Copy keystore to a safe location ─────────────────────────────────
KEYSTORE_DIR="$HOME/keystores"
KEYSTORE_DEST="$KEYSTORE_DIR/futoshiki-release.jks"

mkdir -p "$KEYSTORE_DIR"
cp "$JKS_SRC" "$KEYSTORE_DEST"
chmod 600 "$KEYSTORE_DEST"
echo "✓ Keystore copied to: $KEYSTORE_DEST"

# ─── Step 2: Collect credentials from user ────────────────────────────────────
echo ""
echo "Enter the credentials your admin provided:"
echo ""

read -rp "  Keystore password : " KEYSTORE_PASSWORD
read -rp "  Key alias         : " KEY_ALIAS
read -rsp "  Key password      : " KEY_PASSWORD
echo ""

# ─── Step 3: Verify the keystore + credentials work ──────────────────────────
echo ""
echo "Verifying keystore credentials..."
if ! keytool -list \
    -keystore "$KEYSTORE_DEST" \
    -storepass "$KEYSTORE_PASSWORD" \
    -alias "$KEY_ALIAS" > /dev/null 2>&1; then
    echo "Error: Could not verify keystore. Check your password and alias."
    exit 1
fi
echo "✓ Credentials verified successfully"

# ─── Step 4: Append env vars to shell profile ─────────────────────────────────
PROFILE=""
if [[ "$SHELL" == */zsh ]]; then
    PROFILE="$HOME/.zshrc"
elif [[ "$SHELL" == */bash ]]; then
    PROFILE="$HOME/.bashrc"
elif [[ -f "$HOME/.zshrc" ]]; then
    PROFILE="$HOME/.zshrc"
elif [[ -f "$HOME/.bashrc" ]]; then
    PROFILE="$HOME/.bashrc"
else
    PROFILE="$HOME/.profile"
fi

MARKER="# Futoshiki keystore"
if grep -q "$MARKER" "$PROFILE" 2>/dev/null; then
    # Remove old block before rewriting
    sed -i "/$MARKER/,+4d" "$PROFILE"
fi

cat >> "$PROFILE" <<EOF

$MARKER
export KEYSTORE_PATH="$KEYSTORE_DEST"
export KEYSTORE_PASSWORD="$KEYSTORE_PASSWORD"
export KEY_ALIAS="$KEY_ALIAS"
export KEY_PASSWORD="$KEY_PASSWORD"
EOF

echo "✓ Env vars written to: $PROFILE"

# ─── Step 5: Generate KEYSTORE_BASE64 for GitHub Actions ──────────────────────
echo ""
echo "─────────────────────────────────────────────────────────"
echo "GitHub Actions secret — KEYSTORE_BASE64:"
echo "─────────────────────────────────────────────────────────"
base64 -w 0 "$KEYSTORE_DEST"
echo ""
echo "─────────────────────────────────────────────────────────"
echo ""
echo "Add the above output as a GitHub secret named KEYSTORE_BASE64."
echo "Also add these three secrets:"
echo "  KEYSTORE_PASSWORD = (the password you just entered)"
echo "  KEY_ALIAS         = $KEY_ALIAS"
echo "  KEY_PASSWORD      = (the key password you just entered)"
echo ""
echo "Done. Run: source $PROFILE"
