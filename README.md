<div align="center">

<img src="app/src/main/ic_launcher-playstore.png" width="96" alt="c001apk-Revived 图标">

<h1>c001apk-Revived</h1>

<p>基于 Jetpack Compose 的酷安第三方客户端</p>

<p>
  <img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 7.0+">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-AGPL--3.0-blue" alt="AGPL-3.0"></a>
</p>

<p>
  <a href="https://github.com/frisk1127/c001apk-Revived/releases">下载应用</a> ·
  <a href="CHANGELOG.md">更新日志</a> ·
  <a href="https://github.com/frisk1127/c001apk-Revived/issues">问题反馈</a>
</p>

</div>

在 [c001apk-compose](https://github.com/bggRGjQaUbCoE/c001apk-compose) 的基础上继续维护，支持动态浏览、图文发布、评论回复、消息与私信，并加入实况图片、HDR 显示、产品参数对比和收藏备份等功能。

## 功能

- **实况与 HDR 图片** — 支持实况图片播放、声音开关、重播与保存，以及 Ultra HDR 图片显示。
- **图片浏览** — 全屏预览、手势缩放、滑动切换与长按保存，支持保存原始 GIF。
- **产品参数** — 原生参数页面，支持同机型不同配置和跨机型对比，最多同时比较 10 组配置。
- **原生众测** — 在应用内查看众测详情、活动规则和报名须知，并进入报名页面。
- **首页与横屏** — 自定义首页板块显示与顺序，支持一键回顶；横屏首页全宽显示，打开详情后切换为左右双栏。
- **发布与回复** — 支持选择 `@` 用户、`#` 话题和系统相册图片，评论中展示回复对象头像与回复关系。
- **本地收藏** — 收藏与浏览历史；支持将收藏导出为 JSON 备份，导入时自动合并去重，保留收藏时间。
- **搜索与跳转** — 站内搜索与搜索历史，支持通过酷安帖子链接和搜索链接直接打开对应页面。
- **主题与触感** — Material You 动态取色、自定义主题、深色与纯黑模式、字体和内容缩放；触觉反馈可开关、调节强度并预览。

> Ultra HDR 需要 Android 14 及以上和兼容设备；实况播放需要图片附带视频数据。

## 下载

前往 [Releases](https://github.com/frisk1127/c001apk-Revived/releases) 下载，支持 **Android 7.0 及以上**。日常使用建议选择稳定版，想提前体验新功能可以选择预发布版。各版本的具体变化见 [更新日志](CHANGELOG.md)。

从旧版 c001apk-compose 升级时，同签名且版本号不低于已安装版本的安装包可直接覆盖安装，保留原有数据。收藏备份入口位于 **消息 → 本地收藏 → 右上角菜单**。

## 开发

开发分支为 `dev`，使用 Kotlin 和 Jetpack Compose。

<details>
<summary>本地构建</summary>

需要 JDK 17 或兼容的更高版本、Android SDK 35 和 Build Tools 36.0.0。在项目根目录执行：

```powershell
.\gradlew assembleDebug
.\gradlew assembleRelease
.\gradlew :app:testDebugUnitTest :media-support:testDebugUnitTest
```

APK 输出至 `app/build/outputs/apk/` 对应目录。签名信息通过不纳入 Git 的 `local.properties` 配置。

Release 默认开启 R8，排查混淆问题时可使用 `assembleRelease -PdisableR8=true` 构建。

</details>

## 致谢与许可

感谢 [c001apk-compose](https://github.com/bggRGjQaUbCoE/c001apk-compose) 及其贡献者。第三方组件及许可信息可在应用的开源许可证页面查看。

本项目采用 [AGPL-3.0](LICENSE) 许可证。
