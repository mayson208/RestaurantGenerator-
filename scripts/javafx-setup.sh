#!/usr/bin/env bash
set -euo pipefail

# Download and prepare JavaFX SDK locally under .javafx/
JFX_VERSION=23
DEST=".javafx"
mkdir -p "$DEST"

ARCH="$(uname -m)"
if [[ "$ARCH" == "arm64" || "$ARCH" == "aarch64" ]]; then
  URL="https://download2.gluonhq.com/openjfx/${JFX_VERSION}/openjfx-${JFX_VERSION}_aarch64_mac-bin-sdk.zip"
else
  URL="https://download2.gluonhq.com/openjfx/${JFX_VERSION}/openjfx-${JFX_VERSION}_osx-x64_bin-sdk.zip"
fi

ZIP="$DEST/openjfx.zip"
SDK_DIR="$DEST/javafx-sdk-${JFX_VERSION}"

if [[ ! -d "$SDK_DIR" ]]; then
  echo "Downloading JavaFX ${JFX_VERSION} from $URL ..." >&2
  curl -L -o "$ZIP" "$URL"
  echo "Unzipping to $DEST ..." >&2
  unzip -q -o "$ZIP" -d "$DEST"
  rm -f "$ZIP"
fi

# Print the lib path for callers
echo "$SDK_DIR/lib"
