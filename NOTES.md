# 开发笔记

写这个项目过程中的一些记录，主要是给自己看的，想到什么写什么。

## 2026-08-12

今天把项目建起来了。纠结了一下用 Kotlin 还是 Java，最后还是选了 Java —— 主要是
手上几本 Android 的书都是 Java 版的，Kotlin 那套协程、扩展函数看着爽但没系统学过，
怕写着写着自己都看不懂。先用 Java 把整个流程走一遍，后面再考虑换。

AGP 用的 8.1.2，Gradle 8.2。JDK 17。这个组合是 Android Studio 新建项目默认给的，没折腾。

## 2026-08-13

搞图标花了大半天。想找个好看点的矢量图标，最后放弃了，自己用 path 画了个对话气泡，
能看就行。adaptive icon 要同时准备 `mipmap-anydpi-v26` 和各密度目录，
低版本设备取不到 anydpi 的会崩，这个坑记一下。

## 2026-08-14 ~ 16

设计数据表。三个实体：帖子、回复、用户。

回复和帖子用外键关联，设了 `onDelete = CASCADE`，删帖子的时候评论跟着删。
踩了个坑：Room 里加了外键就必须给子表建索引，不然编译期直接报 warning 并且
运行时会崩。所以在 `@Entity` 上加了 `indices = {@Index("topic_id")}`。

工具类写了个 `TimeUtils`，列表里显示相对时间（"3小时前"）比显示完整时间戳友好多了。
里面 `isYesterday` 的判断写的时候绕了一下 —— 跨年的时候 `DAY_OF_YEAR` 相减会变成负数
（1月1日 减 12月31日 = 1 - 365 = -364），所以加了跨年的特判。

## 2026-08-18

写 mock 数据的时候意识到一个问题：帖子的自增 id 是插入数据库之后才有的，
但评论的 `topicId` 又依赖帖子的 id。所以不能先构造好评论再一起插。

正确顺序是：

1. 先 `insertAll(topics)`，插完之后 `topics` 里每个对象的 `id` 就有值了
2. 再 `buildReplies(topics)`，用这些 id 构造评论
3. 最后插评论

一开始我顺序写错了，评论全都插到了 `topic_id = 0` 下面，页面上一条评论都看不到，
排查了一会儿才反应过来。

## 2026-08-21

登录页画完了。密码框加了个眼睛图标切换明文，本来五分钟的事，结果 `setInputType()`
会把光标位置重置到最前面，用户切一下光标就跳了。解决办法是切换前记下 `getSelectionEnd()`，
切换完再 `setSelection()` 恢复。

## 2026-08-24

主界面四个 tab 用 Fragment。

最开始想用 `ViewPager2 + FragmentStateAdapter`，但我的需求是点底部 tab 切换，
不想要左右滑动（论坛类 App 一般都不给滑），用 ViewPager2 反而要额外禁掉滑动，就算了。

改成手动管理 Fragment，用 `add` + `hide` + `show` 而不是 `replace`。
`replace` 每次都销毁重建，列表滚动位置会丢，来回切几次体验就很难受。

四个 Fragment 一起 `commitNow()` 加进去，之后切 tab 只是 hide/show。
代价是内存里常驻四个 Fragment，对于这个体量的 App 可以接受。

注意 `commitNow()` 必须配合 `if (savedInstanceState == null)` 使用，不然旋转屏幕之后
Fragment 会重复添加，报 `Fragment already added`。所以读了一下 `findFragmentByTag`。

## 2026-08-26

写 `TopicAdapter` 的时候想起以前踩过的坑：RecyclerView 复用导致图片错位。
如果 `onBindViewHolder` 里不重置 ImageView，快速滑动的时候会看到别的 item 的图。
虽然现在头像都是空的（没接图床），还是先按正确的写法来：

```java
if (!TextUtils.isEmpty(url)) {
    Glide.with(context).load(url).into(ivAvatar);
} else {
    ivAvatar.setImageDrawable(null);   // 关键
    ivAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder);
    ivAvatar.setImageResource(R.drawable.ic_avatar_default);
}
```

## 2026-08-29

详情页是这次最麻烦的一个页面。帖子本身和评论在同一个 RecyclerView 里，
所以 Adapter 要处理两个 viewType。

有个下标问题：`getItemCount()` 返回 `replies.size() + 1`（帖子占一位），
但 `onBindViewHolder` 拿到 position 要减 1 才是评论列表的下标。
第一次写的时候忘了减，点评论都是错位的，改成 `int replyIndex = position - 1;` 才对。

另外 `getItemViewType()` 那里判断 `position == 0` 返回 HEADER，这个不能写错，
因为 RecyclerView 是靠这个来复用 ViewHolder 的。

## 2026-09-08

今天集中修了几个 bug。

**浏览量问题最典型。** `getTopicDetail()` 里带了 `increaseViewCount()`，
本意是"打开详情页算一次浏览"。但后来我在三个地方都调了它：

- 点赞后刷数据
- 我的回复列表里查帖子标题
- 详情页点赞后刷新

结果就是：点个赞，浏览量 +1；进一次"我的回复"，浏览量 +N。

这种"方法名看起来是查询，实际有副作用"的设计是最难查的，因为调用方完全想不到。
拆成了 `getTopicById()`（纯查询）和 `getTopicDetail()`（会 +1），名字上区分开。

**列表点赞不生效。** `notifyItemChanged()` 只是通知重绘，数据源里的对象还是旧的。
划出屏幕再划回来，holder 重新绑定又变回旧状态了。
得先把数据库里的新数据读出来，替换掉 list 里的对象，再 notify。

## 之后

接下来想先处理这几件事：

1. 页面里的 `ExecutorService` 和 `Handler` 越来越多，考虑引入 ViewModel 统一管
2. 列表数据是一次性全查出来的，帖子多了会卡
3. 想上 Retrofit 试试真实接口，但还没想好服务端写什么

慢慢来吧。
