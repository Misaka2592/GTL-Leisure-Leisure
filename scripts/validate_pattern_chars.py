#!/usr/bin/env python3
import re
from pathlib import Path

text = Path("src/main/java/com/misaka/gtlleisureaddon/util/LeisureMultiBlockStructure.java").read_text(encoding="utf-8")
aisles = re.search(r"QUANTUM_AISLES = \{(.*)\};", text, re.S).group(1)
pattern_chars = {c for c in aisles if c not in " \n\r\t\",/{}'"}
symbol_chars = set(re.findall(r"Map\.entry\('(.)'", text))
preview_chars = set("012345")
print("pattern chars:", "".join(sorted(pattern_chars)))
print("symbol keys:", "".join(sorted(symbol_chars)))
print("missing from symbols:", "".join(sorted(pattern_chars - symbol_chars)))
print("preview-only in pattern?", preview_chars & pattern_chars)
