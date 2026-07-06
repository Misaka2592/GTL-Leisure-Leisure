#!/usr/bin/env python3
"""Emit hardcoded Java literals from current .bin + symbols.json."""
import json
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def decode_bin(path: Path):
    data = path.read_bytes()
    offset = 8
    id_len = struct.unpack_from(">H", data, offset)[0]
    offset += 2 + id_len
    aisle_count, row_count, width = struct.unpack_from(">HHH", data, offset)
    offset += 6
    dict_size = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    dictionary = [
        data[offset + i * width : offset + (i + 1) * width].decode("ascii") for i in range(dict_size)
    ]
    offset += dict_size * width
    aisles = []
    for _ in range(aisle_count):
        aisle = []
        for _ in range(row_count):
            idx = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            aisle.append(dictionary[idx])
        aisles.append(aisle)
    return aisles


def main() -> int:
    structure_id = "quantum_nucleon_stabilizer_synthesizer"
    bin_path = ROOT / f"src/main/resources/assets/lleisure/structures/multiblock/{structure_id}.bin"
    symbols_path = ROOT / f"src/main/resources/assets/lleisure/structures/{structure_id}.symbols.json"
    symbols = json.loads(symbols_path.read_text(encoding="utf-8"))
    aisles = decode_bin(bin_path)

    print("    private static final Map<Character, BlockState> QUANTUM_SYMBOLS = Map.ofEntries(")
    entries = [(v, k) for k, v in symbols.items() if v != " "]
    for index, (symbol, state_key) in enumerate(sorted(entries, key=lambda item: item[0])):
        java_state = state_key.replace("#controller:", "")
        comma = "," if index < len(entries) - 1 else ""
        print(f'            Map.entry(\'{symbol}\', block("{java_state}")){comma}')
    print("    );")
    print()
    print("    private static final String[][] QUANTUM_AISLES = {")
    for zi, aisle in enumerate(aisles):
        print(f"        // aisle {zi}")
        print("        {")
        for row_index, row in enumerate(aisle):
            comma = "," if row_index < len(aisle) - 1 else ""
            print(f'            "{row}"{comma}')
        comma = "," if zi < len(aisles) - 1 else ""
        print(f"        }}{comma}")
    print("    };")
    return 0


if __name__ == "__main__":
    sys.exit(main())
