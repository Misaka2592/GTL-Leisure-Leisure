import json
import re
from pathlib import Path

ROOTS = [
    (Path(r"D:\Programming\clone\GregTech-Modern-1.20.1-1.4.4"), "GTCEu"),
    (Path(r"D:\Programming\clone\GTLCore"), "GTLCore"),
    (Path(r"D:\Programming\clone\GTLAdditions"), "GTLAdditions"),
]

LANG_FILES = [
    Path(r"D:\Programming\clone\GregTech-Modern-1.20.1-1.4.4\src\main\resources\assets\gtceu\lang\zh_cn.json"),
    Path(r"D:\Programming\clone\GTLCore\src\main\resources\assets\gtceu\lang\zh_cn.json"),
    Path(r"D:\Programming\clone\GTLAdditions\src\main\resources\assets\gtladditions\lang\zh_cn.json"),
]

elements = {}


def add_element(field_name, protons, neutrons):
    elements[field_name.lower()] = {"protons": protons, "neutrons": neutrons}


def load_zh_cn():
    lang = {}
    for path in LANG_FILES:
        if not path.exists():
            continue
        lang.update(json.loads(path.read_text(encoding="utf-8")))
    return lang


def zh_name(lang, material_id, mod):
    if mod == "GTLAdditions":
        key = f"material.gtladditions.{material_id}"
    else:
        key = f"material.gtceu.{material_id}"
    return lang.get(key, "?")


for root, _ in ROOTS:
    for path in root.rglob("*"):
        if path.suffix not in (".java", ".kt"):
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        for match in re.finditer(
            r"(?m)^\s*(\w+)\s*=\s*GTElements\.createAndRegister\(\s*(\d+)\s*,\s*(\d+)",
            text,
        ):
            add_element(match.group(1), int(match.group(2)), int(match.group(3)))
        for match in re.finditer(
            r"val\s+(\w+)\s*(?::[^=]+)?=\s*GTElements\.createAndRegister\(\s*(\d+)\s*,\s*(\d+)",
            text,
        ):
            add_element(match.group(1), int(match.group(2)), int(match.group(3)))
        for match in re.finditer(
            r"public static final Element (\w+) = createAndRegister\(\s*(\d+)\s*,\s*(\d+)",
            text,
        ):
            add_element(match.group(1), int(match.group(2)), int(match.group(3)))


materials = []


def scan_material_file(path, mod):
    text = path.read_text(encoding="utf-8", errors="ignore")
    chunks = re.split(r"Material\.Builder\(", text)
    for chunk in chunks[1:]:
        id_match = re.search(r'(?:GTCEu|GTLAdditions)\.id\("([^"]+)"\)', chunk)
        if not id_match:
            continue
        mat_id = id_match.group(1)
        elem_match = re.search(
            r"\.element\((?:GTLElements|GTLAddElements|GTElements)\.(\w+)\)", chunk
        )
        if not elem_match:
            continue
        elem = elem_match.group(1).lower()
        if elem not in elements:
            continue
        info = elements[elem]
        processable = any(
            token in chunk
            for token in (
                ".dust(",
                ".dust()",
                ".ingot(",
                ".ingot()",
                ".gem(",
                ".liquid(",
                ".gas(",
                ".plasma(",
                ".fluid(",
                ".fluid()",
            )
        )
        materials.append(
            {
                "material_id": mat_id,
                "element": elem,
                "protons": info["protons"],
                "neutrons": info["neutrons"],
                "processable": processable,
                "mod": mod,
            }
        )


for root, mod in ROOTS:
    for path in root.rglob("*"):
        if path.suffix not in (".java", ".kt"):
            continue
        if "material" not in str(path).lower():
            continue
        scan_material_file(path, mod)

by_id = {}
for item in materials:
    key = item["material_id"].lower()
    if key not in by_id or item["processable"]:
        by_id[key] = item

lang = load_zh_cn()
tier8 = sorted(
    [m for m in by_id.values() if m["protons"] > 118],
    key=lambda x: (x["protons"], x["material_id"]),
)

for item in tier8:
    item["zh_name"] = zh_name(lang, item["material_id"], item["mod"])

out_path = Path(__file__).with_name("z_gt118_materials.txt")
lines = []
lines.append("Materials with element Z > 118 (GTCEu + GTLCore + GTLAdditions)")
lines.append("")
lines.append(
    f"{'Z':>8} | {'N':>8} | {'Material ID':<42} | {'中文名':<24} | {'Mod':<12} | Processable | Qualifies"
)
lines.append("-" * 130)
for item in tier8:
    qualifies = item["processable"] and (item["protons"] > 0 or item["neutrons"] > 0)
    lines.append(
        f"{item['protons']:>8} | {item['neutrons']:>8} | {item['material_id']:<42} | {item['zh_name']:<24} | "
        f"{item['mod']:<12} | {'YES' if item['processable'] else 'no':<11} | {'YES' if qualifies else 'no'}"
    )

qual = [m for m in tier8 if m["processable"] and (m["protons"] > 0 or m["neutrons"] > 0)]
lines.append("")
lines.append(f"Total Z>118 materials: {len(tier8)}")
lines.append(f"Qualifying for LeisureNucleonRecipes coupling filter: {len(qual)}")
lines.append("")
lines.append("Qualifying materials:")
for item in qual:
    lines.append(
        f"  - {item['material_id']} | {item['zh_name']} (Z={item['protons']}, mod={item['mod']})"
    )

out_path.write_text("\n".join(lines), encoding="utf-8")
print(out_path.read_text(encoding="utf-8"))
