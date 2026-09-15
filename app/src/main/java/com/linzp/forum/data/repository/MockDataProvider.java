package com.linzp.forum.data.repository;

import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.entity.UserEntity;
import com.linzp.forum.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 本地假数据。
 * 目前没有服务端，先塞一批数据让页面能看出效果，
 * 等接口做好以后这个类整体删掉，Repository 换成网络实现。
 *
 * 帖子前几条是手写的，剩下的用标题和正文片段拼出来 —— 一条条贴太费劲，
 * 而且分页那个功能得有三四十条数据才看得出效果。
 */
public final class MockDataProvider {

    private static final Random RANDOM = new Random(20260818L);

    /** 第一次安装时插入的演示用户密码统一是 123456 */
    public static final String DEMO_PASSWORD = "123456";

    /** 演示帖子总数，首页一页 10 条，正好能翻三页 */
    private static final int TOPIC_COUNT = 30;

    private static final long MINUTE = 60 * 1000L;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    /** username、昵称、邮箱、签名、经验值 */
    private static final String[][] USERS = {
            {"admin", "管理员", "admin@forum.dev", "这里的管理员，有问题可以私信我", "9999"},
            {"zhangwei", "张伟", "zhangwei@163.com", "前端搬砖三年，最近在学 Flutter", "860"},
            {"liuyang", "刘洋", "liuyang@qq.com", "后端 Java 工程师，喜欢折腾服务器", "640"},
            {"chenjing", "陈静", "chenjing@gmail.com", "UI 设计师，接单中，欢迎来聊", "420"},
            {"wangfang", "王芳", "wangfang@126.com", "还在读书，前端菜鸟一枚", "180"},
            {"zhaolei", "赵磊", "zhaolei@outlook.com", "十年 Android 老兵，现在带团队", "1580"},
            {"sunqi", "孙琪", "sunqi@foxmail.com", "产品经理，日常和开发吵架", "350"},
            {"zhouhao", "周浩", "zhouhao@163.com", "刚入行半年，多多指教", "90"},
    };

    /** 开头这几条手写，模板拼出来的看着还是差点意思 */
    private static final String[][] FEATURED_TOPICS = {
            {"大家的第一个 App 是怎么做出来的？",
                    "我是从写一个记事本开始的，做完发现其实没想象中那么难，主要是环境配置劝退。\n\n想听听各位的经历，第一行 Hello World 到现在多久了？"},
            {"Android Studio 升级到最新版后 Gradle 一直报错",
                    "报错大概是 Could not resolve all files for configuration，清缓存、换源都试过，还是不行。\n\nAGP 版本 8.1.2，Gradle 8.2。"},
            {"分享几个自己常用的效率工具",
                    "1. Everything 文件搜索，秒杀 Windows 自带\n2. Snipaste 截图贴图，写文档神器\n3. Bandizip 解压，没有广告\n4. PowerToys 的 FancyZones 分屏很好用"},
            {"关于 UI 设计规范的一点碎碎念",
                    "最近接手一个项目，页面里字体大小有 8 种，间距有 11 种，看得我血压高。\n\n真心建议做设计的时候就把规范定下来，至少字号和间距收敛到四五档。"},
            {"大三了，该考研还是直接找工作？",
                    "专业是软件工程，成绩中等，项目经验不多。家里希望我考研，但我自己更想早点出来攒经验。\n\n有没有过来人给点建议。"},
            {"记录一次线上 OOM 排查",
                    "上周线上内存突然飙到 4G，重启才恢复。最后定位到一个列表接口把全量数据查出来塞进内存缓存了。\n\n教训：任何没有过期策略的本地缓存都是定时炸弹。"},
            {"产品提需求的时候能不能把话说清楚",
                    "今天收到一个需求：做一个好看一点的首页。\n\n好看是多好看？能不能给个参考？结果回我一句「你是专业的你看着办」。"},
            {"RecyclerView 图片错乱问题终于解决了",
                    "列表快速滑动的时候图片会跳到别的 item 上，查了半天。原因是复用 holder 的时候没重置 ImageView。"},
            {"用 Java 写了一个简单的论坛 App",
                    "就是本 App 的原型。功能有登录注册、发帖、评论、个人中心。代码放在 GitHub 上了，边写边记 commit。"},
            {"有没有比较好用的记账 App 推荐",
                    "之前用的那个开始弹广告了，想换一个。要求：不要广告，支持多账本，能导出 CSV 最好。"},
    };

    /** 标题模板，%s 处填一个技术名词或者场景 */
    private static final String[] TITLE_TEMPLATES = {
            "%s 到底要不要学，有点迷茫",
            "求助：%s 用起来一堆坑",
            "关于 %s，说点自己的看法",
            "%s 踩坑记录，希望能帮到后来人",
            "有没有人一起研究 %s",
            "%s 这块有推荐的资料吗",
            "记一次 %s 相关的排查过程",
            "%s 入门到什么程度算够用了",
    };

    /** 填进标题里的话题词 */
    private static final String[] SUBJECTS = {
            "Kotlin 协程", "自定义 View", "Jetpack Compose", "单元测试", "性能优化",
            "Gradle 插件", "RxJava", "Material 3", "内存泄漏排查", "组件化",
            "Flutter", "TypeScript", "Docker", "K8s", "CI 流水线",
            "数据库索引", "HTTP 缓存", "接口签名", "分布式锁", "日志规范",
    };

    /** 正文开头、中间、结尾三块拼一下，比一条条写省事 */
    private static final String[] CONTENT_OPENERS = {
            "最近在做一个小项目，用到 %s，遇到点问题想请教一下。",
            "先说背景：%s 之前只是听过，这次是真上手写了一遍。",
            "看了不少文章，还是觉得 %s 这块绕，说下我自己的理解。",
            "身边好几个同事都在聊 %s，我也去试了试。",
    };

    private static final String[] CONTENT_DETAILS = {
            "按文档写的能跑通，但换成我们项目的场景就不行了，暂时没想明白问题出在哪。",
            "简单场景挺好用，复杂一点就到处是坑，文档写得也比较含糊。",
            "对比了一下之前的写法，代码确实少了不少，就是调试的时候不太好看栈。",
            "我的做法是先跑通最小可用版本，再一点点往项目里搬。",
            "试了两天，整体感觉是能用，但别急着全量上，先挑个不重要的模块练手。",
    };

    private static final String[] CONTENT_ENDINGS = {
            "有经验的朋友麻烦指点一下，先谢过。",
            "以上是我的一点理解，说得不对的地方欢迎拍砖。",
            "大家平时是怎么处理的？想听听别的思路。",
            "先记在这里，后面有进展再更新。",
    };

    /** 评论模板，代码里拼一下就能生成一堆，不用手写 */
    private static final String[] REPLY_TEMPLATES = {
            "这个我们也踩过，%s",
            "同意楼主，%s",
            "补充一点：%s",
            "不太认同，%s",
            "收藏了，%s",
    };

    private static final String[] REPLY_POINTS = {
            "最后是换了个写法才好",
            "文档里其实有一行说明，很容易漏看",
            "关键还是要知道它内部怎么跑的",
            "多写两个 demo 就明白了",
            "版本不一样行为也不太一样，注意看 release note",
            "我觉得够用就行，不用投入太多",
            "还是得结合实际项目去练",
            "官方示例其实写得挺清楚的",
    };

    private MockDataProvider() {
    }

    // ---------------------------------------------------------------- 用户

    public static List<UserEntity> buildUsers() {
        List<UserEntity> users = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (int i = 0; i < USERS.length; i++) {
            String[] row = USERS[i];
            UserEntity user = new UserEntity();
            user.setUsername(row[0]);
            user.setNickname(row[1]);
            user.setEmail(row[2]);
            user.setPasswordHash(CommonUtils.md5(DEMO_PASSWORD));
            user.setSignature(row[3]);
            user.setAvatarUrl("");
            user.setExp(Integer.parseInt(row[4]));
            // 注册时间往前铺开，免得所有人都是同一天
            user.setRegisterTime(now - (USERS.length - i) * 20L * DAY - i * 3L * HOUR);
            // 演示用的统计值，看着别太空
            user.setTopicCount(2 + RANDOM.nextInt(10));
            user.setReplyCount(5 + RANDOM.nextInt(30));
            users.add(user);
        }
        return users;
    }

    private static String userNameOf(int index) {
        if (index < 1 || index > USERS.length) {
            return "游客";
        }
        return USERS[index - 1][1];
    }

    // ---------------------------------------------------------------- 帖子

    public static List<TopicEntity> buildTopics() {
        List<TopicEntity> topics = new ArrayList<>();
        long now = System.currentTimeMillis();

        // 先放手写的那几条，管理员置顶第一条
        for (int i = 0; i < FEATURED_TOPICS.length; i++) {
            String[] row = FEATURED_TOPICS[i];
            topics.add(buildTopic(row[0], row[1],
                    categoryOf(i + 1),
                    authorOf(i + 1),
                    now - (i + 1) * 5L * HOUR,
                    i == 0 ? 1 : 0,
                    i % 3 == 0 ? 1 : 0));
        }

        // 剩下的按模板拼，每条的板块、作者、时间都错开，看着不至于太整齐
        int generated = TOPIC_COUNT - FEATURED_TOPICS.length;
        for (int i = 0; i < generated; i++) {
            String subject = SUBJECTS[i % SUBJECTS.length];
            String title = String.format(
                    TITLE_TEMPLATES[i % TITLE_TEMPLATES.length], subject);
            String content = buildContent(subject, i);
            long createTime = now - (FEATURED_TOPICS.length + i + 1) * 9L * HOUR
                    - (i % 4) * 37L * MINUTE;
            topics.add(buildTopic(title, content,
                    categoryOf(i * 3 + 2),
                    authorOf(i * 5 + 2),
                    createTime,
                    0,
                    i % 7 == 5 ? 1 : 0));
        }
        return topics;
    }

    /** 三段拼一条正文，中间随机挑，跑出来的结果固定 */
    private static String buildContent(String subject, int seed) {
        String opener = String.format(
                CONTENT_OPENERS[seed % CONTENT_OPENERS.length], subject);
        String detail = CONTENT_DETAILS[(seed * 2 + 1) % CONTENT_DETAILS.length];
        String ending = CONTENT_ENDINGS[(seed * 3 + 2) % CONTENT_ENDINGS.length];
        return opener + "\n\n" + detail + "\n\n" + ending;
    }

    /** 板块轮着来，别全堆在一个板块里 */
    private static int categoryOf(int seed) {
        return seed % 5 + 1;
    }

    /** 作者在 8 个演示用户里轮，管理员也会发几条 */
    private static int authorOf(int seed) {
        return seed % USERS.length + 1;
    }

    private static TopicEntity buildTopic(String title, String content, int categoryId,
                                          long authorId, long createTime,
                                          int isTop, int isEssence) {
        TopicEntity topic = new TopicEntity();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setCategoryId(categoryId);
        topic.setAuthorId(authorId);
        topic.setAuthorName(userNameOf((int) authorId));
        topic.setAuthorAvatar("");
        // 浏览量按帖子的新旧给个范围，越老的看得越多
        topic.setViewCount(200 + RANDOM.nextInt(2000));
        topic.setLikeCount(RANDOM.nextInt(120));
        topic.setReplyCount(0);
        topic.setCreateTime(createTime);
        topic.setUpdateTime(createTime);
        topic.setIsTop(isTop);
        topic.setIsEssence(isEssence);
        topic.setLiked(0);
        return topic;
    }

    // ---------------------------------------------------------------- 评论

    /**
     * 每条帖子配 2-6 条评论，详情页才不至于空荡荡的。
     * 楼层从 1 开始，楼主自己占 0 楼。
     */
    public static List<ReplyEntity> buildReplies(List<TopicEntity> topics) {
        List<ReplyEntity> replies = new ArrayList<>();
        if (topics == null || topics.isEmpty()) {
            return replies;
        }
        long replyId = 1;
        for (int i = 0; i < topics.size(); i++) {
            TopicEntity topic = topics.get(i);
            // 前几条手写的帖子评论多一点，看着热闹
            int count = i < FEATURED_TOPICS.length
                    ? 4 + RANDOM.nextInt(4) : 2 + RANDOM.nextInt(3);
            for (int j = 0; j < count; j++) {
                replies.add(buildReply(topic, i, j, replyId++));
            }
        }
        return replies;
    }

    private static ReplyEntity buildReply(TopicEntity topic, int topicIndex,
                                          int floor, long replyId) {
        ReplyEntity reply = new ReplyEntity();
        reply.setId(replyId);
        reply.setTopicId(topic.getId());
        reply.setContent(buildReplyText(topicIndex, floor));
        reply.setFloorNo(floor + 1);
        // 楼层越靠后时间越晚，不然评论区时间顺序是乱的
        reply.setCreateTime(topic.getCreateTime() + (floor + 1) * 25L * MINUTE);

        int authorId = authorOf(topicIndex + floor * 3 + 1);
        reply.setAuthorId(authorId);
        reply.setAuthorName(userNameOf(authorId));
        reply.setAuthorAvatar("");
        reply.setLikeCount(RANDOM.nextInt(9));
        reply.setLiked(0);
        // 每隔几条模拟一次"回复某人"
        reply.setReplyToName(floor % 3 == 2 ? userNameOf(authorOf(topicIndex)) : null);
        return reply;
    }

    private static String buildReplyText(int topicIndex, int floor) {
        String template = REPLY_TEMPLATES[(topicIndex + floor) % REPLY_TEMPLATES.length];
        String point = REPLY_POINTS[(topicIndex * 2 + floor) % REPLY_POINTS.length];
        return String.format(template, point);
    }
}
