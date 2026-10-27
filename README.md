# c001apk-Revived

基于 [bggRGjQaUbCoE/c001apk-compose](https://github.com/bggRGjQaUbCoE/c001apk-compose) 持续维护的 Android 酷安第三方客户端，重点改进接口兼容、富媒体浏览、首页管理和回复体验。

[下载版本](https://github.com/frisk1127/c001apk-Revived/releases) · [更新日志](CHANGELOG.md) · [反馈问题](https://github.com/frisk1127/c001apk-Revived/issues)

## 相比上游增加了什么

对比基线为上游 `main` 的 [`d44c471f`](https://github.com/bggRGjQaUbCoE/c001apk-compose/commit/d44c471fb38fa600d28fa33b5dd7fcfd6abb7197)（2024-09-16）。下表描述当前 `dev` 分支，已发布版本的功能以对应 Release 说明为准。

| 方向 | 上游已有能力 | 本分支新增或改进 |
| --- | --- | --- |
| 首页 | 信息流和多个首页板块 | 板块显示开关、拖动排序及配置保存；发布按钮上方增加带动画的回顶按钮 |
| 本地收藏 | 收藏和浏览历史 | JSON 备份导出、导入合并去重、保留收藏时间；导入前完整校验，已有记录不被覆盖 |
| 实况与 HDR 图片 | 常规图片与 GIF 浏览 | 实况图片识别、播放、声音开关、重播及保存；Ultra HDR 图片显示与标识 |
| 图片查看与保存 | 图片预览、缩放和保存 | 修复多处长按、缩放、翻页、黑屏和缓存问题；GIF 保存原始数据；优化状态栏、弹窗取色和进出动画 |
| 产品参数 | 产品页面与相关信息流 | 原生配置列表、同机型配置比较，以及最多 10 项配置的跨机型对比 |
| 酷安众测 | 常规链接跳转 | 原生众测详情、报名须知、活动规则及报名入口 |
| 发布与回复 | 文字、图片、表情输入及评论列表 | `@` 用户和 `#` 话题选择；回复对象头像及关系展示；系统图片选择器、其他媒体入口与输入面板动画改进 |
| 搜索与外部链接 | 站内搜索和链接跳转 | 支持 `coolmarket` 搜索链接及编码关键词；修复通过帖子链接冷启动时的崩溃 |
| 横屏与返回 | 宽屏列表／详情双栏 | 首页先占满可用宽度，打开动态后左右各半，关闭后恢复；增加分栏过渡并改善返回动画和 Tab 状态恢复 |
| 触觉与中文体验 | 基础交互和主题设置 | 可开关的触觉反馈、系统触感、自定义强度与预览、机型兼容选项；补全多处中文菜单和操作结果提示 |
| 接口兼容 | 原有酷安接口接入 | 修复评论读取、应用更新响应解析等兼容问题；评论使用独立的版本请求头，保留其他页面的参数 |

Material You 动态取色、暗色／纯黑主题、自定义主题和调色板、字体与内容缩放、私信、黑名单等基础能力继续沿用上游。

Ultra HDR 显示需要 Android 14 及以上，并取决于设备和图片本身的支持情况。实况图片的可用功能也取决于原始媒体是否提供对应视频数据。

[查看与上游基线的完整差异](https://github.com/frisk1127/c001apk-Revived/compare/d44c471f...dev)

## 下载与升级

- 基本运行要求：Android 7.0（API 24）及以上。
- 稳定版和预发布版在 [Releases](https://github.com/frisk1127/c001apk-Revived/releases) 中区分；开发工作位于 `dev` 分支。
- 项目更名为 `c001apk-Revived`，Android 包名仍为 `com.example.c001apk.compose`。签名一致且版本号不低于已安装版本时，可以覆盖安装并保留本地数据。
- 本地收藏备份入口：**消息 → 本地收藏 → 右上角菜单 → 导出收藏／导入收藏**。更名前后的备份格式兼容。

## 构建

工程使用 Gradle Wrapper、JDK 17 或兼容的更高版本、Android SDK 35 和 Build Tools 36.0.0。

在 Windows PowerShell 中执行：

```powershell
.\gradlew assembleDebug
.\gradlew assembleRelease
.\gradlew :app:testDebugUnitTest :media-support:testDebugUnitTest
```

APK 位于 `app/build/outputs/apk/` 下对应的构建目录。版本号由 Git 提交数生成，版本名包含提交短哈希；开发分支还会带上分支名和未提交改动标记。

Release 默认开启 R8。需要排查混淆相关问题时，可以使用 `assembleRelease -PdisableR8=true` 构建对照包。签名信息通过不纳入 Git 的 `local.properties` 配置。

工程包含 `app`、`mojito`、`SketchImageViewLoader` 和 `media-support` 模块。开发分支提供文件日志，便于定位问题。

## 来源与许可证

上游项目：[bggRGjQaUbCoE/c001apk-compose](https://github.com/bggRGjQaUbCoE/c001apk-compose)。

本项目沿用仓库中的 AGPL-3.0 许可证，详见 [LICENSE](LICENSE)。上游与第三方组件的来源说明保留在代码及应用的开源许可证页面中。
