#!/bin/bash
# Builds the unsigned template APK used by the in-browser builder (docs/).
set -euo pipefail
cd "$(dirname "$0")"
R=${H2A_ROOT:-..}
AJ=$R/tools/android.jar; D8=$R/tools/d8.jar
AAPT2=${AAPT2:-aapt2}
rm -rf build && mkdir -p build/flat build/classes build/dex
$AAPT2 compile --dir res -o build/flat/res.zip
$AAPT2 link -o build/base.apk -I "$AJ" --manifest AndroidManifest.xml \
  --min-sdk-version 24 --target-sdk-version 34 --version-code 1 --version-name 0.0.0-h2a-version-placeholder \
  build/flat/res.zip
javac -source 1.8 -target 1.8 -Xlint:-options,-deprecation -cp "$AJ" -d build/classes src/com/h2a/*.java
java -Xmx512M -cp "$D8" com.android.tools.r8.D8 --release --min-api 24 --lib "$AJ" --output build/dex $(find build/classes -name '*.class')
python3 repack.py build/base.apk build/dex/classes.dex ${1:-build/h2a-template.apk}
