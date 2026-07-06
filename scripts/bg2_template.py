#!/usr/bin/env python3
"""Building Gadgets 2 template export parsing and voxel grid layout.

Serialization matches BG2Data.statePosListToNBTMapArray / statePosListFromNBTMapArray:
https://github.com/Direwolf20-MC/BuildingGadgets2/blob/main/src/main/java/com/direwolf20/buildinggadgets2/common/worlddata/BG2Data.java

statelist indices follow Minecraft BlockPos.betweenClosedStream(AABB) order:
  x varies fastest, then y, then z.
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Bg2Bounds:
    min_x: int
    min_y: int
    min_z: int
    width: int
    height: int
    depth: int

    @property
    def max_x(self) -> int:
        return self.min_x + self.width - 1

    @property
    def max_y(self) -> int:
        return self.min_y + self.height - 1

    @property
    def max_z(self) -> int:
        return self.min_z + self.depth - 1

    @property
    def volume(self) -> int:
        return self.width * self.height * self.depth


@dataclass(frozen=True)
class Bg2Template:
    name: str
    bounds: Bg2Bounds
    palette: list[str]
    states: list[int]
    required_items: dict[str, int]


def fail(message: str) -> None:
    raise SystemExit(message)


def bounds_from_corners(start: dict[str, int], end: dict[str, int]) -> Bg2Bounds:
    """Match VecHelpers.aabbFromBlockPos + BlockPos.betweenClosedStream."""
    min_x = min(start["X"], end["X"])
    max_x = max(start["X"], end["X"])
    min_y = min(start["Y"], end["Y"])
    max_y = max(start["Y"], end["Y"])
    min_z = min(start["Z"], end["Z"])
    max_z = max(start["Z"], end["Z"])
    return Bg2Bounds(
        min_x=min_x,
        min_y=min_y,
        min_z=min_z,
        width=max_x - min_x + 1,
        height=max_y - min_y + 1,
        depth=max_z - min_z + 1,
    )


def statelist_index(x: int, y: int, z: int, bounds: Bg2Bounds) -> int:
    """Index of (x, y, z) inside the BG2 statelist."""
    if not (bounds.min_x <= x <= bounds.max_x):
        fail(f"x={x} outside [{bounds.min_x}, {bounds.max_x}]")
    if not (bounds.min_y <= y <= bounds.max_y):
        fail(f"y={y} outside [{bounds.min_y}, {bounds.max_y}]")
    if not (bounds.min_z <= z <= bounds.max_z):
        fail(f"z={z} outside [{bounds.min_z}, {bounds.max_z}]")
    rel_x = x - bounds.min_x
    rel_y = y - bounds.min_y
    rel_z = z - bounds.min_z
    return rel_x + rel_y * bounds.width + rel_z * bounds.width * bounds.height


def parse_blockstate_entry(name: str, props: str | None) -> str:
    if not props:
        return name
    prop_pairs = []
    for key, value in re.findall(r'(\w+):"([^"]*)"', props):
        prop_pairs.append(f"{key}={value}")
    return f"{name}[{','.join(prop_pairs)}]"


def parse_state_pos_array_list(text: str) -> tuple[Bg2Bounds, list[str], list[int]]:
    text = text.strip()
    if not text.startswith("{") or not text.endswith("}"):
        fail("statePosArrayList must be an object literal")

    blockstatemap_match = re.search(r"blockstatemap:\[(.*)\],endpos:", text, re.S)
    endpos_match = re.search(r"endpos:\{X:(-?\d+),Y:(-?\d+),Z:(-?\d+)\}", text)
    startpos_match = re.search(r"startpos:\{X:(-?\d+),Y:(-?\d+),Z:(-?\d+)\}", text)
    statelist_match = re.search(r"statelist:\[I;([^\]]+)\]", text)
    if not all([blockstatemap_match, endpos_match, startpos_match, statelist_match]):
        fail("unable to parse statePosArrayList object")

    start = {
        "X": int(startpos_match.group(1)),
        "Y": int(startpos_match.group(2)),
        "Z": int(startpos_match.group(3)),
    }
    end = {
        "X": int(endpos_match.group(1)),
        "Y": int(endpos_match.group(2)),
        "Z": int(endpos_match.group(3)),
    }
    bounds = bounds_from_corners(start, end)

    palette: list[str] = []
    for entry in re.finditer(
        r'\{Name:"([^"]+)"(?:,Properties:\{([^}]*)\})?\}',
        blockstatemap_match.group(1),
    ):
        palette.append(parse_blockstate_entry(entry.group(1), entry.group(2)))

    states = [int(value.strip()) for value in statelist_match.group(1).split(",") if value.strip()]
    if len(states) != bounds.volume:
        fail(f"expected {bounds.volume} states, got {len(states)}")

    return bounds, palette, states


def load_template(path: Path) -> Bg2Template:
    data = json.loads(path.read_text(encoding="utf-8"))
    inner = data["statePosArrayList"]
    if not isinstance(inner, str):
        fail("expected statePosArrayList string export from Building Gadgets 2")
    bounds, palette, states = parse_state_pos_array_list(inner)
    required = data.get("requiredItems", {})
    if not isinstance(required, dict):
        fail("requiredItems must be an object")
    return Bg2Template(
        name=data.get("name", ""),
        bounds=bounds,
        palette=palette,
        states=states,
        required_items={str(k): int(v) for k, v in required.items()},
    )


def build_voxel_grid(template: Bg2Template) -> list[list[list[str]]]:
    """Return grid[z][y][x] for GTCEu aisle encoding (aisle=Z, row=Y, column=X)."""
    bounds = template.bounds
    palette = template.palette
    states = template.states
    air = palette[0] if palette else "minecraft:air"

    grid = [[[air for _ in range(bounds.width)] for _ in range(bounds.height)] for _ in range(bounds.depth)]
    for z in range(bounds.depth):
        for y in range(bounds.height):
            for x in range(bounds.width):
                wx = bounds.min_x + x
                wy = bounds.min_y + y
                wz = bounds.min_z + z
                idx = statelist_index(wx, wy, wz, bounds)
                state_id = states[idx]
                if state_id < 0 or state_id >= len(palette):
                    fail(f"statelist[{idx}]={state_id} out of palette range")
                grid[z][y][x] = palette[state_id]
    return grid
