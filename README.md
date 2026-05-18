# STM32CubeMXPatch

## 项目简介（中文）
STM32CubeMXPatch 的功能非常单一：修复在非英文输入法（例如中文输入法）下使用 STM32CubeMX 时可能出现的输入/粘贴问题。

## Project Overview (English)
The functionality of STM32CubeMXPatch is intentionally minimal: it fixes input/paste issues when using STM32CubeMX with non-English input methods (e.g. Chinese IME). 

---

## 功能（中文）
- 仅修复非英文输入法（IME）导致的输入/粘贴异常

## Features (English)
- Fixes input/paste anomalies caused by non-English input methods (IME) only

---

## 使用方法（中文）
1. 获取 jar：
   - 下载 Release 中的 app-all.jar，或自行编译（项目使用 Gradle，生成 shadowJar，产物通常为 build/libs/app-all.jar）。
2. 备份并复制：
   - 以管理员权限备份并复制 app-all.jar 到：
     C:\Program Files\STMicroelectronics\STM32Cube\STM32CubeMX
3. 修改启动配置：
   - 编辑 STM32CubeMX.l4j.ini（同目录下），在适当位置加入一行：
     -javaagent:app-all.jar=debug
   - 说明：加入 =debug 会在启动时打开一个调试/日志窗口，便于查看输出；如果不希望看到额外的命令窗口，请将参数改为：
     -javaagent:app-all.jar
4. 权限与提示：
   - 复制到 Program Files 以及编辑 .ini 可能需要管理员权限。修改前请备份原 .l4j.ini。

## Usage (English)
1. Obtain the jar:
   - Download app-all.jar from Releases, or build it yourself (Gradle shadowJar, output typically at build/libs/app-all.jar).
2. Backup and copy:
   - With admin rights, backup and copy app-all.jar to:
     C:\Program Files\STMicroelectronics\STM32Cube\STM32CubeMX
3. Edit startup config:
   - Open STM32CubeMX.l4j.ini (same folder) and add a line:
     -javaagent:app-all.jar=debug
   - Note: The =debug suffix opens a debug/log console on startup. To avoid the extra console window, remove the suffix:
     -javaagent:app-all.jar
4. Permissions and notes:
   - Copying into Program Files and editing the .ini usually require admin rights. Backup the original .l4j.ini before changes.

---

## 构建提示（中文/English）
- 若自行编译：使用 Gradle 的 shadowJar 插件来生成包含依赖的单一 jar（app-all.jar）。例如：gradlew shadowJar，然后在 build/libs/ 找到 app-all.jar。
- If you build locally: use Gradle shadowJar to produce a fat jar (app-all.jar). e.g. gradlew shadowJar — output in build/libs/.

---

## 许可与贡献（中文/English）
- 请参考仓库中已有的 LICENSE 文件（如果存在）。欢迎提交 issue 与 PR，但本项目功能单一，请在 PR 中注明用途与测试步骤。

---