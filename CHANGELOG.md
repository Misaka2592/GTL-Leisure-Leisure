# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1] - 2026-07-06

### Added
- 新增了 **戴森球控制系统** 的增强，包括发电效率提升、材料消耗优化、发射机制优化等（测试中）
- 新增了 **戴森球控制系统** 增强的配置文件，现在支持游戏内热修改相关配置了（测试中）

### Fixed
- 替换了 **归墟·定元仪** 的结构，该结构仍为测试结构，后续可能会再次替换
- 修复了 **归墟·定元仪** 的多方块预览与实际情况不符的问题（测试中）
- 移除了 JEI 多方块预览的mixin，现在使用gtceu原生api展示多方块结构
- 修改了 当前版本 与 初始版本 的版本号，现在为 0.1.1，初始版本为 0.0.1（release 处仍然为 1.0.0）
- 移除了 **Him**

## [0.0.1] - 2026-06-20

### Added

- 初始发布：**Leisure Leisure** (`lleisure`) GTL 附属模组
- GT 材料 **Proton**、**Neutron**
- **Nucleon Aggregation Catalyst** MK I–IX 与 Prototype 物品
- 多方块 **Quantum Nucleon Stabilizer-Synthesizer**（归墟·定元仪）
  - 配方类型 `nucleon_decoupling_protocol`、`nucleon_coupling_protocol`
  - GTLASB2 二进制结构资源与 WorldEdit `.schem` 转换脚本
- 本地化 中英文 语言文件

[0.0.1]: https://github.com/Misaka2592/GTL-Leisure-Leisure/releases/tag/v1.0.0
