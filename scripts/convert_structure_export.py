#!/usr/bin/env python3
"""Convert Building Gadgets 2 template JSON into GTLASB2 .bin + symbols.json."""

from __future__ import annotations

import argparse
import json
import struct
import sys
from collections import OrderedDict
from pathlib import Path

from bg2_template import Bg2Template, build_voxel_grid, load_template

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_OUTPUT = ROOT / "src/main/resources/assets/lleisure/structures/multiblock"
MAGIC = b"GTLASB2"
KIND_FACTORY_PATTERN = 1
PRINTABLE_SYMBOLS = (
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    "#$%&*+-/=?@[]^_`{|}~"
)

CONTROLLER_BLOCK = "gtceu:fusion_glass"
CONTROLLER_LAYER_Y = 1


def fail(message: str) -> None:
    raise SystemExit(message)


def block_id(state: str) -> str:
    return state.split("[", 1)[0]


def pick_controller(grid) -> tuple[int, int, int]:
    """Controller = fusion_glass that is alone on its (aisle, row) slice at layer y=1."""
    depth, height, width = len(grid), len(grid[0]), len(grid[0][0])
    layer_candidates: list[tuple[int, int, int]] = []
    any_candidates: list[tuple[int, int, int]] = []

    for z in range(depth):
        for y in range(height):
            fusion_columns = [x for x in range(width) if block_id(grid[z][y][x]) == CONTROLLER_BLOCK]
            if len(fusion_columns) != 1:
                continue
            pos = (z, y, fusion_columns[0])
            any_candidates.append(pos)
            if y == CONTROLLER_LAYER_Y:
                layer_candidates.append(pos)

    candidates = layer_candidates or any_candidates
    if not candidates:
        fail("no unique fusion_glass position found for controller")

    center_x = width // 2
    return min(candidates, key=lambda pos: (abs(pos[2] - center_x), pos[0], pos[2]))


def assign_symbols(
    grid,
    controller_pos: tuple[int, int, int],
) -> tuple[list[list[str]], dict[str, str], dict]:
    depth, height, width = len(grid), len(grid[0]), len(grid[0][0])
    symbol_by_state: dict[str, str] = {"minecraft:air": " "}
    used_symbols: set[str] = {" "}

    def reserve(state: str, symbol: str) -> None:
        if symbol in used_symbols and symbol != " ":
            owners = [s for s, sym in symbol_by_state.items() if sym == symbol and s != state]
            if owners:
                fail(f"symbol {symbol!r} already used by {owners[0]!r}")
        if state in symbol_by_state and symbol_by_state[state] != symbol:
            fail(f"state {state!r} has conflicting symbols {symbol_by_state[state]!r} and {symbol!r}")
        symbol_by_state[state] = symbol
        if symbol != " ":
            used_symbols.add(symbol)

    cz, cy, cx = controller_pos
    controller_state = grid[cz][cy][cx]
    if block_id(controller_state) != CONTROLLER_BLOCK:
        fail(f"controller position {controller_pos} is {controller_state!r}, expected {CONTROLLER_BLOCK}")

    symbol_iter = iter(ch for ch in PRINTABLE_SYMBOLS if ch not in used_symbols)
    body_fusion_symbol = next(symbol_iter, None)
    if body_fusion_symbol is None:
        fail("ran out of printable symbols for fusion_glass body")
    reserve(f"#controller:{controller_state}", "G")
    reserve(controller_state, body_fusion_symbol)

    for z in range(depth):
        for y in range(height):
            for x in range(width):
                state = grid[z][y][x]
                if block_id(state) == "minecraft:air":
                    continue
                if state in symbol_by_state:
                    continue
                symbol = next(symbol_iter, None)
                if symbol is None:
                    fail(f"ran out of printable symbols at {state!r}")
                reserve(state, symbol)

    char_grid = [[[" " for _ in range(width)] for _ in range(height)] for _ in range(depth)]
    for z in range(depth):
        for y in range(height):
            for x in range(width):
                state = grid[z][y][x]
                if block_id(state) == "minecraft:air":
                    continue
                if (z, y, x) == controller_pos:
                    char_grid[z][y][x] = "G"
                elif state == controller_state:
                    char_grid[z][y][x] = body_fusion_symbol
                else:
                    char_grid[z][y][x] = symbol_by_state[state]

    aisles = []
    for z in range(depth):
        rows = []
        for y in range(height):
            rows.append("".join(char_grid[z][y]))
        aisles.append(rows)

    return aisles, symbol_by_state, {"controller": controller_pos}


def validate_string_grid(grid: list[list[str]], context: str) -> None:
    if not grid:
        fail(f"{context}: aisles must not be empty")
    row_count = len(grid[0])
    width = len(grid[0][0])
    for aisle_index, aisle in enumerate(grid):
        if len(aisle) != row_count:
            fail(f"{context}: inconsistent row count at aisle {aisle_index}")
        for row_index, row in enumerate(aisle):
            if len(row) != width:
                fail(f"{context}: inconsistent row width at aisle {aisle_index}, row {row_index}")


def pack_u8(value: int) -> bytes:
    return struct.pack(">B", value)


def pack_u16(value: int) -> bytes:
    return struct.pack(">H", value)


def encode_id(structure_id: str) -> bytes:
    encoded = structure_id.encode("utf-8")
    return pack_u16(len(encoded)) + encoded


def row_bytes(row: str) -> bytes:
    return row.encode("ascii")


def build_row_dictionary(rows: list[str]) -> tuple[list[str], list[int]]:
    dictionary: list[str] = []
    indexes: list[int] = []
    index_by_row: dict[str, int] = {}
    for row in rows:
        index = index_by_row.get(row)
        if index is None:
            index = len(dictionary)
            index_by_row[row] = index
            dictionary.append(row)
        indexes.append(index)
    return dictionary, indexes


def encode_grid(grid: list[list[str]]) -> bytes:
    aisle_count = len(grid)
    row_count = len(grid[0])
    width = len(grid[0][0])
    rows = [row for aisle in grid for row in aisle]
    dictionary, indexes = build_row_dictionary(rows)

    output = bytearray()
    output += pack_u16(aisle_count)
    output += pack_u16(row_count)
    output += pack_u16(width)
    output += pack_u16(len(dictionary))
    for row in dictionary:
        output += row_bytes(row)
    for index in indexes:
        output += pack_u16(index)
    return bytes(output)


def write_structure(path: Path, structure_id: str, aisles: list[list[str]]) -> None:
    validate_string_grid(aisles, structure_id)
    payload = bytearray()
    payload += pack_u8(KIND_FACTORY_PATTERN)
    payload += encode_id(structure_id)
    payload += encode_grid(aisles)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(MAGIC + bytes(payload))


def write_symbol_report(path: Path, symbol_by_state: dict[str, str]) -> None:
    ordered = OrderedDict(sorted(symbol_by_state.items(), key=lambda item: (item[1], item[0])))
    path.write_text(json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def update_index(output_dir: Path, relative_path: str) -> None:
    index_path = output_dir.parent / "structures.index"
    lines = []
    if index_path.exists():
        lines = [line for line in index_path.read_text(encoding="utf-8").splitlines() if line and not line.startswith("#")]
    rels = set(lines)
    rels.add(relative_path)
    index_path.write_text("# Auto-generated by convert_structure_export.py\n\n" + "\n".join(sorted(rels)) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Convert BG2 template JSON to GTLASB2 multiblock structure")
    parser.add_argument("input", type=Path, help="Building Gadgets 2 template JSON export")
    parser.add_argument("--id", required=True, help="structure id written into the .bin header")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    template: Bg2Template = load_template(args.input)
    grid = build_voxel_grid(template)
    controller = pick_controller(grid)
    aisles, symbols, info = assign_symbols(grid, controller)

    bin_path = args.output / f"{args.id}.bin"
    write_structure(bin_path, args.id, aisles)
    symbol_path = args.output.parent / f"{args.id}.symbols.json"
    write_symbol_report(symbol_path, symbols)
    update_index(args.output, f"multiblock/{args.id}.bin")

    b = template.bounds
    cz, cy, cx = info["controller"]
    print(f"wrote {bin_path.relative_to(ROOT)}")
    print(f"wrote {symbol_path.relative_to(ROOT)}")
    print(
        f"size={b.width}x{b.height}x{b.depth} "
        f"controller=aisle {cz} row {cy} col {cx} "
        f"world=({b.min_x + cx},{b.min_y + cy},{b.min_z + cz}) "
        f"symbols={len(symbols) - 1}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
