#!/usr/bin/env bash
# يستخرج aapt2 المضمَّن في tools/bundletool.jar إلى tools/bin/ (Linux أو macOS)
set -euo pipefail
cd "$(dirname "$0")/.."
case "$(uname -s)" in
  Linux)  src="linux/aapt2" ;;
  Darwin) src="macos/aapt2" ;;
  *) echo "نظام غير مدعوم: $(uname -s)"; exit 1 ;;
esac
mkdir -p tools/bin
unzip -o -j -q tools/bundletool.jar "$src" -d tools/bin
chmod +x tools/bin/aapt2
echo "✓ tools/bin/aapt2 — أضفه إلى PATH:"
echo "  export PATH=\"$PWD/tools/bin:\$PATH\""
