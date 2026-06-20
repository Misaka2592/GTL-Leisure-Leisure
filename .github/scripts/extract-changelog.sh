#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:?Usage: extract-changelog.sh <version> [output-file]}"
OUTPUT="${2:-release-notes.md}"

python3 - <<'PY' "$VERSION" "$OUTPUT"
import sys
from pathlib import Path

version, output = sys.argv[1], Path(sys.argv[2])
lines = Path("CHANGELOG.md").read_text(encoding="utf-8").splitlines()
header = f"## [{version}]"
try:
    start = next(i for i, line in enumerate(lines) if line.startswith(header))
except StopIteration:
    raise SystemExit(f"CHANGELOG.md has no section for {version}")

body: list[str] = []
for line in lines[start + 1 :]:
    if line.startswith("## ["):
        break
    body.append(line)

text = "\n".join(body).strip()
if not text:
    raise SystemExit(f"CHANGELOG.md section for {version} is empty")

output.write_text(text + "\n", encoding="utf-8")
print(f"Wrote release notes to {output}")
PY
