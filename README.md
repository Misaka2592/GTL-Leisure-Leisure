# Leisure Leisure

GTL 整合包附属模组，为 GTL 整合包生态扩展核子相关材料、物品、配方与多方块机器。

| 项 | 值 |
|---|---|
| Mod ID | `lleisure` |
| 版本 | 1.0.0 |
| 平台 | Minecraft 1.20.1 · Forge 47+ |
| 许可证 | [GPL-3.0-or-later](LICENSE) |
| 作者 | Misaka2592 |

## 依赖

| 模组 | 要求           | 说明 |
|---|--------------|---|
| GTLCore |  | GTL 整合包核心 |
| GTLAdditions | 必需 (≥ 2.6.5) | 并行仓、线程修改器等能力 |

## 内容概览

### 材料

- **Proton** / **Neutron** — 核子相关 GT 材料（粉尘、气体形态）

### 物品

- **Nucleon Aggregation Catalyst** — MK I–IX 与 Prototype 共 10 种聚合催化剂
- 示例占位物品（`example_component`、`example_catalyst`）

### 多方块

- **Quantum Nucleon Stabilizer-Synthesizer**（归墟·定元仪）
  - 配方类型： 核子退耦规程、核子耦联规程
  - 支持并行仓、激光输入、天球分歧引擎
  - 结构采用 GTLASB2 `.bin` 格式（与 GTLAdditions 相同）

### 配方类型

- 核子退耦规程：将核子分解为质子与中子
- 核子耦联规程：将质子与中子合成为核子

### 技术说明

- 结构资源：`assets/lleisure/structures/multiblock/*.bin`（由 `scripts/convert_schem_to_structure.py` 从 WorldEdit `.schem` 生成）
- JEI 预览兼容 GTLCore 自定义 `PatternPreviewWidget`（通过 mixin 注入 `predicateMap`）

## 开发环境

### 前置条件

- **JDK 17+**（推荐 21）
- 依赖 JAR 放在本项目根目录的 `libs/`（见 `build.gradle` 的 `flatDir` 配置；JAR 文件本身被 git 忽略，克隆后需自行放入，清单见 `libs/README.md`）
- 可选 KubeJS 资源：复制 `gradle.properties.local.example` 为 `gradle.properties.local`，设置相对路径，例如 `kubejs_assets_dir=../kubejs`；或设置环境变量 `KUBEJS_ASSETS_DIR`。未配置时跳过同步，不影响编译与运行。

### 构建环境

```powershell
git clone https://github.com/Misaka2592/GTL-Leisure-Leisure.git
```
- 自行构建时注意修改 `build.gradle` 和 `gradle.properties` 中的kubejs_assets_dir路径指向正确的资源目录（如果需要）
- 本环境使用 GTL 1.4.5.1 版本的 kubejs 资产开发

### 多方块结构蓝图转换

```powershell
pip install -r scripts/requirements.txt

# 将 .schem 放入 scripts/schem_input/
python scripts/convert_schem_to_structure.py
```

生成结果写入 `src/main/resources/assets/lleisure/structures/multiblock/`，并更新 `structures.index` 与 `.symbols.json`。

### 常用任务

| 命令 | 说明 |
|---|---|
| `.\gradlew build` | 编译并打包 |
| `.\gradlew deployMod` | 构建并复制到 `deploy/` |
| `.\gradlew runClient` | 启动开发客户端 |
| `.\gradlew spotlessApply` | 格式化 Java / JSON |


## 许可证

本项目以 **GNU General Public License v3.0 or later** 发布。详见 [LICENSE](LICENSE)。

## 变更记录

见 [CHANGELOG.md](CHANGELOG.md)。
