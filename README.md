# 假期倒计时 / Holiday Countdown

赛博霓虹风的中国大陆法定节假日 Android 倒计时应用。内置 2026 年假期与调休安排，提供倒计时、工作日统计、全年时间轴、桌面小组件、提醒、前台背景音乐和状态动画。

Cyber-neon Android holiday countdown for mainland China. It includes the 2026 holiday and make-up workday schedule, countdowns, workday totals, an annual timeline, home-screen widget, reminders, foreground music, and state animations.

## 功能 / Features

- 自动计算距下一假期的自然日与工作日；假期中显示剩余时间与进度。
- 依据上次假期结束后的次日至下次假期开始日计算上班阶段进度。
- Room 本地日历、远程 HTTPS JSON 更新、JSON 导入导出与手动日期覆盖。
- WorkManager 周期同步、节前/节后提醒、Jetpack Glance 桌面小组件。
- Jetpack Compose 赛博界面、动态粒子、假期动画与真实消行规则的俄罗斯方块上班动画。
- Media3 前台循环背景音乐，支持音量、自动播放和动画开关。

- Calculates calendar and work days until the next holiday; shows remaining time and progress during a holiday.
- Calculates work-phase progress from the day after the previous holiday to the next holiday start.
- Uses Room for the local calendar, HTTPS JSON updates, import/export, and manual date overrides.
- Includes WorkManager sync, holiday reminders, and a Jetpack Glance home-screen widget.
- Built with a cyber-neon Compose UI, particles, holiday animation, and a Tetris-style work animation with line clears.
- Plays foreground-only looping music with configurable volume, autoplay, and animation settings.

## Requirements / 环境要求

- Android Studio with JDK 17 or newer
- Android SDK Platform 36
- Android Gradle Plugin 8.9.2, Kotlin 2.1.20, Gradle 8.14
- minSdk 26, targetSdk 36

## Build / 构建

```powershell
$env:JAVA_HOME='F:\Software\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\<your-user>\AppData\Local\Android\Sdk'
.\gradlew.bat assembleDebug
```

Debug APK is generated under `app/build/outputs/apk/debug/`. Download published APKs from [GitHub Releases](../../releases).

Debug APK 位于 `app/build/outputs/apk/debug/`；发布版本请从 [GitHub Releases](../../releases) 下载。

## Holiday data / 节假日数据

Built-in data is available offline. The default remote URL is a placeholder: configure `HOLIDAY_DATA_URL` in `app/build.gradle.kts`, or enter an HTTPS JSON endpoint in the app settings. Remote data must use schema version 1 and a monotonically increasing `revision`.

内置数据可离线使用。默认远程地址为占位地址：可在 `app/build.gradle.kts` 配置 `HOLIDAY_DATA_URL`，或在应用设置中输入 HTTPS JSON 地址。远程数据必须使用 schema version 1，并递增 `revision`。

## Release note / 发布说明

The v1.0.2 release asset is an **unsigned Release APK**. It is suitable for manual installation/testing and is not presented as a production-signed distribution.

v1.0.2 发布资产为**未签名 Release APK**，适用于手动安装与测试，并非生产签名发行包。