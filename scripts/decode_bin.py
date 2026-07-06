#!/usr/bin/env python3
"""Decode GTLASB2 bin and print slice summaries."""
import struct
import sys
from pathlib import Path


def decode_bin(path: Path):
    data = path.read_bytes()
    assert data[:7] == b"GTLASB2"
    offset = 7
    kind = data[offset]
    offset += 1
    id_len = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    structure_id = data[offset : offset + id_len].decode()
    offset += id_len
    aisle_count, row_count, width = struct.unpack_from(">HHH", data, offset)
    offset += 6
    dict_size = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    dictionary = []
    for _ in range(dict_size):
        dictionary.append(data[offset : offset + width].decode("ascii"))
        offset += width
    aisles = []
    for _ in range(aisle_count):
        aisle = []
        for _ in range(row_count):
            idx = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            aisle.append(dictionary[idx])
        aisles.append(aisle)
    return structure_id, aisles


def summarize(aisles, char):
    hits = []
    for z, aisle in enumerate(aisles):
        for y, row in enumerate(aisle):
            for x, ch in enumerate(row):
                if ch == char:
                    hits.append((z, y, x))
    return hits


def main():
    path = Path(sys.argv[1])
    sid, aisles = decode_bin(path)
    print(f"id={sid} aisles={len(aisles)} rows={len(aisles[0])} width={len(aisles[0][0])}")
    for ch in "GHNT":
        hits = summarize(aisles, ch)
        print(f"  {ch}: {len(hits)} at {hits[:8]}{'...' if len(hits)>8 else ''}")


if __name__ == "__main__":
    main()
