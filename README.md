# 微光论坛 SimpleForum

一个练手用的安卓论坛 App，功能参照主流社区类产品（下面几个板块、帖子列表、详情、评论、个人中心）。
纯 Java + XML 写的，没有上 Compose，也没上 MVVM，就是最传统的那套写法。数据全部走本地 Room，
没有服务端，算是把客户端这条链路先跑通。

> 项目还在开发中，功能没做完。这个仓库主要是记录自己一点点写的过程，
> commit 记录了从建项目到现在的每一步，包括中间写错又改回来的部分。

---

## 目录

- [目前实现了什么](#目前实现了什么)
- [还没做的](#还没做的)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [怎么跑起来](#怎么跑起来)
- [测试账号](#测试账号)
- [几个设计上的说明](#几个设计上的说明)
- [踩过的坑](#踩过的坑)
- [开发计划](#开发计划)

---

## 目前实现了什么

### 账号
- [x] 注册：用户名 / 昵称 / 邮箱 / 密码，带格式校验和重复校验
- [x] 登录：用户名或邮箱都能登
- [x] 记住密码、密码明文切换显示
- [x] 退出登录
- [x] 登录态持久化（SharedPreferences，不存整个实体）

### 帖子
- [x] 首页帖子列表，按时间倒序，置顶排前面
- [x] 板块筛选（全部 / 综合讨论 / 技术交流 / 生活日常 / 求助问答 / 资源分享）
- [x] 下拉刷新
- [x] 列表内点赞（红心状态切换）
- [x] 帖子详情：正文、浏览量、点赞、评论列表
- [x] 发帖：选板块、标题 / 正文、字数统计、退出二次确认
- [x] 评论：发评论、按楼层排序、回复某个人
- [x] 删除自己的帖子（级联删评论）
- [x] 搜索：标题 + 内容模糊匹配，带搜索历史

### 个人相关
- [x] 个人中心：头像、昵称、签名、等级、发帖数 / 回复数 / 经验值
- [x] 我的帖子、我的回复
- [x] 他人主页（从帖子/评论头像进入）
- [x] 设置：改昵称、改签名、清本地数据、版本号

### 界面
- [x] 启动页（带登录态判断）
- [x] 底部四个 tab（首页 / 板块 / 消息 / 我的）
- [x] 空状态、加载态提示
- [x] 统一的下拉刷新和 Toast

---

## 还没做的

这些是明确知道缺的，不是忘了：

- **消息推送**：消息页现在是个静态占位，没做真实的消息列表
- **收藏**：菜单里有入口，点击只弹提示
- **评论点赞**：后端数据结构里留了 likeCount，界面上还没接
- **发图片**：发帖只能发纯文字
- **深色模式**：只做了日间主题
- **真实网络层**：没有 Retrofit，全靠本地 Room
- **分页加载**：列表一次性全读出来，数据量大了会卡（评论 DAO 里已经预留了分页方法）

---

## 技术栈

| 项目 | 版本 / 说明 |
|---|---|
| 语言 | Java 8 |
| 最低版本 | Android 7.0 (API 24) |
| 编译版本 | API 34 |
| 构建 | Gradle 8.2 + AGP 8.1.2 |
| 数据库 | Room 2.5.2 |
| 图片 | Glide 4.15.1 |
| UI | Material Components 1.9.0、RecyclerView、SwipeRefreshLayout |

没有用第三方网络库，也没有用依赖注入框架。原因很简单：这个项目是拿来练手写的，
想先把 Android 原生的那套东西（Activity / Fragment 生命周期、RecyclerView 复用、
Room 线程调度）摸清楚，加太多框架反而看不清底下在发生什么。

---

## 项目结构

```
app/src/main/java/com/linzp/forum/
├── ForumApplication.java          # 启动时灌一次演示账号
├── base/
│   └── BaseActivity.java          # 页面跳转、返回键处理
├── data/
│   ├── db/                        # Room：三个 Dao + Database 单例
│   ├── entity/                    # TopicEntity / ReplyEntity / UserEntity
│   ├── model/                     # Category / TopicStats
│   ├── prefs/                     # UserSession 登录态
│   └── repository/                # 数据入口 + MockDataProvider
├── ui/
│   ├── SplashActivity.java
│   ├── MyContentActivity.java
│   ├── adapter/                   # 四个 Adapter
│   ├── login/  register/          # 登录注册
│   ├── main/                      # 主界面 + 四个 tab Fragment
│   ├── topic/                     # 帖子详情
│   ├── post/                      # 发帖、搜索
│   └── profile/                   # 他人主页、设置
└── util/                          # 校验、时间格式化、Toast 封装
```

分层思路就是最朴素的：`Activity/Fragment` 只碰 `Repository`，`Repository` 碰 `Dao`。
没有 ViewModel，异步用的是 `ExecutorService` + `Handler` 手动切线程 —— 这点后面打算改，
现在页面多了以后 `executor` 和 `mainHandler` 到处都在 new，挺啰嗦的。

---

## 怎么跑起来

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更新的版本
- JDK 17（AGP 8.x 要求）
- Android SDK Platform 34

### 步骤

1. 克隆项目

   ```bash
   git clone <仓库地址>
   cd SimpleForum
   ```

2. 在项目根目录创建 `local.properties`，指向自己的 SDK 路径（可以直接复制 `local.properties.example` 改）：

   ```properties
   sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
   ```

   注意 Windows 下路径里的冒号和反斜杠都要转义。

3. 用 Android Studio 打开项目，等 Gradle Sync 完。

4. 直接 Run 到真机或模拟器上。

第一次启动会自动往数据库里写一批演示数据（8 个用户、15 篇帖子、若干评论），
这样不用注册就能看到完整效果。想清空的话去「我的 → 设置 → 清空本地数据」。

---

## 测试账号

| 账号 | 密码 | 说明 |
|---|---|---|
| `admin` | `123456` | 管理员，等级最高 |
| `zhangwei` | `123456` | 普通用户 |
| `liuyang` | `123456` | 普通用户 |

所有演示账号密码都是 `123456`，登录用用户名或邮箱都行。

---

## 几个设计上的说明

**1. 密码为什么只做了 MD5**

`CommonUtils.md5()` 只是为了避免明文存库，并不是真正的安全方案。
生产环境应该用 bcrypt 或者 Argon2，还要加盐。这里明确知道是做得不对的，
但为了不引三方库就先这样了，注释里也标了。

**2. Room 的 `fallbackToDestructiveMigration`**

开发阶段表结构一直在变，写 Migration 太累，所以直接设成销毁重建。
上线前必须换成正式的 Migration，不然用户升级版本就丢数据。

**3. Fragment 用 show/hide 而不是 replace**

主界面的四个 tab 用的是 `hide()` + `show()`。用 `replace()` 的话每次切回来都要重建
Fragment，列表滚动位置会丢，体验很差。代价是四个 Fragment 常驻内存。

**4. 板块数据写死在代码里**

`Category.ALL` 是个静态数组，因为板块基本不会变。如果以后要做后台可配置，
就把这个数组换成一张表。

---

## 踩过的坑

写的过程中记录下来的一些问题，有的已经修了，有的还在：

**浏览量会莫名增加。**
一开始 `getTopicDetail()` 里带了给浏览量 +1 的逻辑，因为想着"打开详情就算一次浏览"。
但后来别的地方（点赞后刷新数据、我的回复列表查帖子标题）也调了这个方法，
结果用户点个赞浏览量都涨。后来拆出了 `getTopicById()` 这个只读方法才解决。

**列表里点赞只刷新了界面没更新数据。**
`notifyItemChanged()` 只是让 ViewHolder 重新绘制，数据源里的对象没变，
所以划出屏幕再划回来，点赞状态又变回去了。改成从数据库重新读一遍再更新数据源。

**RecyclerView 图片串位。**
`onBindViewHolder` 里如果不清空 ImageView，复用的 holder 会带着上一个 item 的图。
加一句 `setImageDrawable(null)` 就好了。

**切换密码可见性时光标跳到最前面。**
`setInputType()` 会把光标重置，得手动把 selection 恢复到原来的位置。

**RecyclerView 列表里的换了行的文本很难看。**
帖子内容里的 `\n` 在列表项里显示会占掉一大块，统一 `replace("\n", " ")` 处理了。

**Handler 内存泄漏的风险。**
SplashActivity 里的延时任务、各页面的 `mainHandler.post`，在 `onDestroy` 里都要
`removeCallbacksAndMessages(null)`，不然 Activity 销毁了回调还在跑。

---

## 开发计划

近期打算做的：

- [ ] 引入 ViewModel + LiveData，把各页面里的 `ExecutorService` 收拢一下
- [ ] 接 Retrofit，做一套真实的服务端接口
- [ ] 帖子列表分页加载
- [ ] 帖子详情页滑动到评论区自动加载更多

远期再看：

- [ ] 发帖带图（需要做图床或对象存储）
- [ ] 消息推送
- [ ] 深色模式
- [ ] 帖子收藏

---

## 说明

这是我个人拿来练手的项目，代码里如果有写得不合理的地方欢迎提 issue 指出来。
数据都是本地的，不涉及任何真实用户信息。

如果这个项目对你有帮助，点个 star 就好 :)
