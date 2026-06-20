#!/usr/bin/env python3
"""Convert WorldEdit .schem exports into lleisure GTLASB2 .bin structure resources."""

from __future__ import annotations

import argparse
import json
import struct
import sys
from collections import OrderedDict
from pathlib import Path

import nbtlib

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_INPUT = ROOT / "scripts/schem_input"
DEFAULT_OUTPUT = ROOT / "src/main/resources/assets/lleisure/structures/multiblock"
MAGIC = b"GTLASB2"
KIND_FACTORY_PATTERN = 1
PRINTABLE_SYMBOLS = (
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    "#$%&*+-/=?@[]^_`{|}~"
)


def fail(message: str) -> None:
    raise SystemExit(message)


def read_schem_nbt(path: Path) -> nbtlib.tag.Compound:
    return nbtlib.load(path, byteorder="big")


def palette_map(root: nbtlib.tag.Compound, path: Path) -> dict[int, str]:
    palette_tag = root.get("Palette")
    if palette_tag is None:
        fail(f"{path}: missing Palette tag")

    mapping: dict[int, str] = {}
    for block_name, block_id in palette_tag.items():
        mapping[int(block_id)] = str(block_name)
    if not mapping:
        fail(f"{path}: empty Palette")
    return mapping


def block_indices(root: nbtlib.tag.Compound, width: int, height: int, length: int, path: Path) -> list[int]:
    version = int(root.get("Version", 2))
    if version >= 3:
        blocks = root.get("Blocks")
        if blocks is None or "Data" not in blocks:
            fail(f"{path}: schematic v{version} missing Blocks/Data")
        data = blocks["Data"]
        indices = [int(value) & 0xFF for value in data]
    else:
        if "BlockData" not in root:
            fail(f"{path}: schematic v{version} missing BlockData")
        indices = [int(value) & 0xFF for value in root["BlockData"]]

    expected = width * height * length
    if len(indices) != expected:
        fail(f"{path}: expected {expected} blocks, got {len(indices)}")
    return indices


def resolve_dimensions(root: nbtlib.tag.Compound, path: Path) -> tuple[int, int, int]:
    try:
        width = int(root["Width"])
        height = int(root["Height"])
        length = int(root["Length"])
    except KeyError as error:
        fail(f"{path}: missing dimension tag {error}")
    if min(width, height, length) <= 0:
        fail(f"{path}: invalid dimensions {width}x{height}x{length}")
    return width, height, length


def load_symbol_map(path: Path | None) -> dict[str, str]:
    if path is None:
        return {}
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        fail(f"{path}: symbol map must be a JSON object")
    result: dict[str, str] = {}
    for block_state, symbol in data.items():
        if not isinstance(block_state, str) or not isinstance(symbol, str) or len(symbol) != 1:
            fail(f"{path}: each entry must map a block state string to a single character")
        if not 0x20 <= ord(symbol) <= 0x7E:
            fail(f"{path}: symbol for {block_state!r} must be printable ASCII")
        result[block_state] = symbol
    return result


DEFAULT_AIR_STATES = frozenset(
    {
        "minecraft:air",
        "minecraft:cave_air",
        "minecraft:void_air",
        "minecraft:structure_void",
    }
)


def is_air_block(block_state: str, air: str) -> bool:
    if block_state == air:
        return True
    if block_state in DEFAULT_AIR_STATES:
        return True
    namespace, _, path = block_state.partition(":")
    if not path:
        return False
    return path == "air" or path.endswith("_air")


def assign_symbols(
    palette: dict[int, str],
    user_map: dict[str, str],
    controller: str | None,
    air: str,
) -> dict[str, str]:
    mapping: dict[str, str] = {}
    used_symbols: set[str] = set()

    def reserve(block_state: str, symbol: str, *, allow_shared: bool = False) -> None:
        allow_shared = allow_shared or symbol == " "
        existing = mapping.get(block_state)
        if existing is not None and existing != symbol:
            fail(f"Block state {block_state!r} assigned conflicting symbols {existing!r} and {symbol!r}")
        if not allow_shared and symbol in used_symbols:
            owners = [state for state, mapped in mapping.items() if mapped == symbol and state != block_state]
            if owners:
                fail(f"Symbol {symbol!r} assigned to multiple block states: {owners} and {block_state!r}")
        mapping[block_state] = symbol
        if not allow_shared:
            used_symbols.add(symbol)

    for block_state in palette.values():
        if is_air_block(block_state, air):
            reserve(block_state, " ", allow_shared=True)

    if controller:
        if controller not in palette.values():
            fail(f"Controller block state {controller!r} not found in schematic palette")
        reserve(controller, "G")

    for block_state, symbol in user_map.items():
        if block_state not in palette.values():
            fail(f"Symbol map references unknown block state {block_state!r}")
        reserve(block_state, symbol)

    symbol_iter = iter(PRINTABLE_SYMBOLS)
    for block_state in sorted(set(palette.values())):
        if block_state in mapping:
            continue
        while True:
            try:
                symbol = next(symbol_iter)
            except StopIteration:
                fail(f"Too many unique block states to assign printable symbols for {block_state!r}")
            if symbol not in used_symbols:
                reserve(block_state, symbol)
                break
    return mapping


def schem_to_aisles(
    path: Path,
    user_map: dict[str, str],
    controller: str | None,
    air: str,
) -> tuple[str, list[list[str]]]:
    root = read_schem_nbt(path)
    width, height, length = resolve_dimensions(root, path)
    palette = palette_map(root, path)
    indices = block_indices(root, width, height, length, path)
    symbols = assign_symbols(palette, user_map, controller, air)

    aisles: list[list[str]] = []
    for z in range(length):
        rows: list[str] = []
        for y in range(height):
            row_chars: list[str] = []
            for x in range(width):
                index = (y * length + z) * width + x
                block_state = palette[indices[index]]
                row_chars.append(symbols[block_state])
            rows.append("".join(row_chars))
        aisles.append(rows)
    return path.stem, aisles


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
            for char in row:
                code = ord(char)
                if code < 0x20 or code > 0x7E:
                    fail(f"{context}: non-printable character U+{code:04X}")


def pack_u8(value: int) -> bytes:
    return struct.pack(">B", value)


def pack_u16(value: int) -> bytes:
    return struct.pack(">H", value)


def encode_id(structure_id: str) -> bytes:
    encoded = structure_id.encode("utf-8")
    if not encoded:
        fail("Structure id must not be empty")
    if len(encoded) > 0xFFFF:
        fail(f"Structure id {structure_id!r} is too long")
    return pack_u16(len(encoded)) + encoded


def row_bytes(row: str) -> bytes:
    try:
        encoded = row.encode("ascii")
    except UnicodeEncodeError as error:
        fail(f"Row contains non-ASCII characters: {error}")
    for byte in encoded:
        if byte < 0x20 or byte > 0x7E:
            fail(f"Row contains non-printable byte {byte}")
    return encoded


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


def write_symbol_report(path: Path, palette: dict[int, str], symbols: dict[str, str]) -> None:
    ordered = OrderedDict(
        (block_state, symbols[block_state])
        for block_state in sorted(set(palette.values()), key=lambda value: (value != "minecraft:air", value))
    )
    path.write_text(json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_structure_index(output_dir: Path, relative_paths: list[str]) -> None:
    index_path = output_dir.parent / "structures.index"
    lines = ["# Auto-generated by convert_schem_to_structure.py", ""]
    lines.extend(sorted(set(relative_paths)))
    index_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def convert_file(
    input_path: Path,
    output_dir: Path,
    structure_id: str | None,
    symbol_map: dict[str, str],
    controller: str | None,
    air: str,
) -> Path:
    default_id, aisles = schem_to_aisles(input_path, symbol_map, controller, air)
    resolved_id = structure_id or default_id
    output_path = output_dir / f"{resolved_id}.bin"
    write_structure(output_path, resolved_id, aisles)

    root = read_schem_nbt(input_path)
    palette = palette_map(root, input_path)
    symbols = assign_symbols(palette, symbol_map, controller, air)
    symbol_path = output_dir.parent / f"{resolved_id}.symbols.json"
    write_symbol_report(symbol_path, palette, symbols)
    return output_path


def collect_inputs(input_path: Path) -> list[Path]:
    if input_path.is_file():
        if input_path.suffix.lower() != ".schem":
            fail(f"{input_path} is not a .schem file")
        return [input_path]
    if not input_path.is_dir():
        fail(f"{input_path} does not exist")
    files = sorted(input_path.glob("*.schem"))
    if not files:
        fail(f"No .schem files found under {input_path}")
    return files


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Convert WorldEdit .schem files into lleisure GTLASB2 .bin files.")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Input .schem file or directory")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT, help="Output directory for .bin files")
    parser.add_argument("--id", dest="structure_id", help="Override structure id for single-file conversion")
    parser.add_argument(
        "--controller",
        help="Block state id mapped to controller symbol G (default: lleisure controller from symbol map)",
    )
    parser.add_argument("--air", default="minecraft:air", help="Block state id treated as air")
    parser.add_argument("--symbol-map", type=Path, help="Optional JSON map of block state -> single-char symbol")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    inputs = collect_inputs(args.input)
    if args.structure_id and len(inputs) != 1:
        fail("--id can only be used when converting a single .schem file")

    user_map = load_symbol_map(args.symbol_map)
    written_paths: list[str] = []
    for input_path in inputs:
        controller = args.controller
        if controller is None:
            for block_state, symbol in user_map.items():
                if symbol == "G":
                    controller = block_state
                    break
        output_path = convert_file(
            input_path,
            args.output,
            args.structure_id,
            user_map,
            controller,
            args.air,
        )
        written_paths.append(("multiblock/" + output_path.name).replace("\\", "/"))
        print(f"wrote {output_path.relative_to(ROOT)}")

    write_structure_index(args.output, written_paths)
    print(f"wrote {(args.output.parent / 'structures.index').relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
