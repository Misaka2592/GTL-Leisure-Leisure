#!/usr/bin/env python3
"""Validate BG2 template parsing against requiredItems and key block positions."""

from __future__ import annotations

import re
import sys
from collections import Counter
from pathlib import Path

from bg2_template import load_template, statelist_index, build_voxel_grid


def block_id(state: str) -> str:
    return state.split("[", 1)[0]

ROOT = Path(__file__).resolve().parents[1]


def main() -> int:
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(r"c:\Users\32324\Desktop\new structure.txt")
    template = load_template(path)
    grid = build_voxel_grid(template)

    counts = Counter()
    for aisle in grid:
        for row in aisle:
            for state in row:
                bid = state.split("[", 1)[0]
                if bid != "minecraft:air":
                    counts[bid] += 1

    mismatches = []
    for item, expected in template.required_items.items():
        got = counts.get(item, 0)
        if got != expected:
            mismatches.append(f"{item}: got {got}, want {expected}")

    magic = []
    lasers = []
    for z, aisle in enumerate(grid):
        for y, row in enumerate(aisle):
            for x, state in enumerate(row):
                bid = block_id(state)
                wx = template.bounds.min_x + x
                wy = template.bounds.min_y + y
                wz = template.bounds.min_z + z
                if bid == "kubejs:magic_core":
                    magic.append((wx, wy, wz))
                if "laser_target_hatch" in state:
                    facing = re.search(r"facing=([^,\]]+)", state)
                    lasers.append((facing.group(1) if facing else "?", wx, wy, wz))

    print(f"template={path.name}")
    print(f"bounds volume={template.bounds.volume} mismatches={len(mismatches)}")
    for line in mismatches[:10]:
        print(f"  {line}")
    print(f"magic_core={magic}")
    for laser in lasers:
        print(f"  laser {laser[0]} at ({laser[1]}, {laser[2]}, {laser[3]})")

    # Sanity: statelist roundtrip for corners
    b = template.bounds
    corners = [
        (b.min_x, b.min_y, b.min_z),
        (b.max_x, b.max_y, b.max_z),
    ]
    for x, y, z in corners:
        idx = statelist_index(x, y, z, b)
        print(f"corner ({x},{y},{z}) idx={idx} state={template.palette[template.states[idx]]}")

    return 1 if mismatches else 0


if __name__ == "__main__":
    raise SystemExit(main())
