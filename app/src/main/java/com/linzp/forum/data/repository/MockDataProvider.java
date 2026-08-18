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
 */
public final class MockDataProvider {

    private static final Random RANDOM = new Random(20260818L);

    /** 第一次安装时插入的演示用户密码统一是 123456 */
    public static final String DEMO_PASSWORD = "123456";

    private MockDataProvider() {
    }

    public static List<UserEntity> buildUsers() {
        List<UserEntity> users = new ArrayList<>();
        long now = System.currentTimeMillis();

        users.add(createUser("admin", "管理员", "admin@forum.dev",
                "这里的管理员，有问题可以私信我", 0, 9999, now - 200L * 24 * 3600 * 1000));
        users.add(createUser("zhangwei", "张伟", "zhangwei@163.com",
                "前端搬砖三年，最近在学 Flutter", 12, 860, now - 150L * 24 * 3600 * 1000));
        users.add(createUser("liuyang", "刘洋", "liuyang@qq.com",
                "后端 Java 工程师，喜欢折腾服务器", 8, 640, now - 120L * 24 * 3600 * 1000));
        users.add(createUser("chenjing", "陈静", "chenjing@gmail.com",
                "UI 设计师，接单中，欢迎来聊", 5, 420, now - 90L * 24 * 3600 * 1000));
        users.add(createUser("wangfang", "王芳", "wangfang@126.com",
                "还在读书，前端菜鸟一枚", 3, 180, now - 60L * 24 * 3600 * 1000));
        users.add(createUser("zhaolei", "赵磊", "zhaolei@outlook.com",
                "十年 Android 老兵，现在带团队", 20, 1580, now - 300L * 24 * 3600 * 1000));
        users.add(createUser("sunqi", "孙琪", "sunqi@foxmail.com",
                "产品经理，日常和开发吵架", 6, 350, now - 45L * 24 * 3600 * 1000));
        users.add(createUser("zhouhao", "周浩", "zhouhao@163.com",
                "刚入行半年，多多指教", 2, 90, now - 20L * 24 * 3600 * 1000));

        return users;
    }

    private static UserEntity createUser(String username, String nickname, String email,
                                         String signature, int topicCount, int exp, long registerTime) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPasswordHash(CommonUtils.md5(DEMO_PASSWORD));
        user.setSignature(signature);
        user.setAvatarUrl("");
        user.setTopicCount(topicCount);
        user.setReplyCount(topicCount * 2);
        user.setExp(exp);
        user.setRegisterTime(registerTime);
        return user;
    }

    public static List<TopicEntity> buildTopics() {
        List<TopicEntity> topics = new ArrayList<>();
        long now = System.currentTimeMillis();

        topics.add(buildTopic("大家的第一个 App 是怎么做出来的？",
                "我是从写一个记事本开始的，做完发现其实没想象中那么难，主要是环境配置劝退。\n\n" +
                        "想听听各位的经历，第一行 Hello World 到现在多久了？",
                1, 1, "管理员", 2, 1860, 42, now - 2L * 3600 * 1000, 1, 1));

        topics.add(buildTopic("Android Studio 升级到最新版后 Gradle 一直报错",
                "报错信息大概是 Could not resolve all files for configuration，\n" +
                        "试过清缓存、换源都不行，有没有人碰到过？\n\n" +
                        "AGP 版本 8.1.2，Gradle 8.2。",
                2, 2, "张伟", 3, 428, 17, now - 5L * 3600 * 1000, 0, 0));

        topics.add(buildTopic("分享几个自己常用的效率工具",
                "1. Everything 文件搜索，秒杀 Windows 自带\n" +
                        "2. Snipaste 截图贴图，写文档神器\n" +
                        "3. Bandizip 解压，没有广告\n" +
                        "4. PowerToys 的 FancyZones 分屏很好用\n\n" +
                        "还有别的欢迎补充。",
                5, 3, "刘洋", 1, 1204, 56, now - 8L * 3600 * 1000, 0, 1));

        topics.add(buildTopic("关于 UI 设计规范的一点碎碎念",
                "最近接手一个项目，页面里字体大小有 8 种，间距有 11 种，看得我血压高。\n\n" +
                        "真心建议做设计的时候就把规范定下来，至少字号和间距收敛到 4-5 档。\n" +
                        "后面改起来真的会省很多事。",
                1, 4, "陈静", 2, 876, 33, now - 12L * 3600 * 1000, 0, 0));

        topics.add(buildTopic("大三了，该考研还是直接找工作？",
                "专业是软件工程，成绩中等，项目经验不多。\n" +
                        "家里希望我考研，但我自己更想早点出来干活攒经验。\n" +
                        "有没有过来人给点建议，感谢。",
                4, 5, "王芳", 5, 642, 78, now - 20L * 3600 * 1000, 0, 0));

        topics.add(buildTopic("记录一次线上 OOM 排查",
                "上周线上突然内存飙到 4G，重启才恢复。\n\n" +
                        "最后定位到是一个列表接口把全量数据查出来塞进内存缓存了，\n" +
                        "数据从 2 万涨到 80 万之后直接顶不住。\n\n" +
                        "教训：任何没有过期策略的本地缓存都是定时炸弹。",
                2, 6, "赵磊", 3, 2340, 96, now - 1L * 24 * 3600 * 1000, 0, 1));

        topics.add(buildTopic("产品提需求的时候能不能把话说清楚",
                "今天收到一个需求：做一个好看一点的首页。\n\n" +
                        "好看是多好看？能不能给个参考？\n" +
                        "结果回我一句\"你是专业的你看着办\"。\n" +
                        "……行吧。",
                1, 7, "孙琪", 8, 1560, 143, now - 1L * 24 * 3600 * 1000 - 3 * 3600 * 1000, 0, 0));

        topics.add(buildTopic("租房踩坑实录，希望大家别重复我的错误",
                "上个月租了个房，看房时挺好的，住进去发现水压巨小，晚上洗澡像滴灌。\n" +
                        "还有就是中介说的\"离地铁近\"，走 20 分钟。\n\n" +
                        "总结：看房一定要晚上去，一定要开水龙头试。",
                3, 8, "周浩", 2, 388, 21, now - 2L * 24 * 3600 * 1000, 0, 0));

        topics.add(buildTopic("RecyclerView 图片错乱问题终于解决了",
                "列表快速滑动的时候图片会跳到别的 item 上，查了半天。\n" +
                        "原因是没有在 onBindViewHolder 里重置 ImageView，\n" +
                        "复用的时候还留着上一个 holder 的图。\n\n" +
                        "加一句 setImageDrawable(null) 就好了。",
                2, 2, "张伟", 4, 692, 45, now - 3L * 24 * 3600 * 1000, 0, 0));

        topics.add(buildTopic("有没有比较好用的记账 App 推荐",
                "之前用的那个开始弹广告了，想换一个。\n" +
                        "要求：不要广告，支持多账本，能导出 CSV 最好。\n" +
                        "付费的也行，一次性买断最好。",
                4, 3, "刘洋", 1, 534, 29, now - 4L * 24 * 3600 * 1000, 0, 0));

        topics.add(buildTopic("养猫之后才知道的事",
                "1. 猫真的会半夜在你脸上蹦迪\n" +
                        "2. 猫毛无处不在，黑衣服基本告别\n" +
                        "3. 你以为买的是猫窝，它只睡纸箱\n" +
                        "4. 但是真的很治愈，值了",
                3, 4, "陈静", 6, 1088, 67, now - 5L * 24 * 3600 * 1000, 0, 1));

        topics.add(buildTopic("第一次面试，紧张到说话都在抖",
                "昨天面了一家小公司，问的都是基础问题，\n" +
                        "但我一紧张连 HashMap 底层都说不利索了。\n\n" +
                        "面完感觉凉了，大家第一次面试都这样吗？",
                4, 8, "周浩", 3, 286, 18, now - 6L * 24 * 3600 * 1000, 0, 0));

        topics.add(buildTopic("整理了 50 个免费的编程学习资源",
                "包括视频课、电子书、练习平台，按语言分类整理好了。\n" +
                        "链接有点多就不贴了，评论区留邮箱我发网盘。\n\n" +
                        "都是我自己用过觉得靠谱的，不是随便找的。",
                5, 6, "赵磊", 2, 1876, 112, now - 7L * 24 * 3600 * 1000, 0, 1));

        topics.add(buildTopic("关于加班这件事，大家怎么看",
                "我们组最近连着两个月 996，项目不是特别急，\n" +
                        "就是领导觉得\"大家都这样\"。\n\n" +
                        "效率反而下降了，白天都在等晚上。\n" +
                        "你们公司什么情况？",
                1, 7, "孙琪", 5, 1420, 88, now - 8L * 24 * 3600 * 1000, 0, 0));

        topics.add(buildTopic("用 Java 写了一个简单的论坛 App",
                "就是本 App 的原型。功能有登录注册、发帖、评论、个人中心。\n" +
                        "代码放在 GitHub 上了，边写边记 commit，\n" +
                        "有兴趣的可以一起看看，欢迎提 issue。\n\n" +
                        "技术栈：Java + XML + Room + Material Components，没上 MVVM，\n" +
                        "先把手写 View 的路子走通再说。",
                2, 1, "管理员", 4, 1650, 74, now - 9L * 24 * 3600 * 1000, 0, 1));

        return topics;
    }

    private static TopicEntity buildTopic(String title, String content, int categoryId,
                                          long authorId, String authorName, int viewCount,
                                          int likeCount, int replyCount, long createTime,
                                          int isTop, int isEssence) {
        TopicEntity topic = new TopicEntity();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setCategoryId(categoryId);
        topic.setAuthorId(authorId);
        topic.setAuthorName(authorName);
        topic.setAuthorAvatar("");
        topic.setViewCount(viewCount);
        topic.setLikeCount(likeCount);
        topic.setReplyCount(replyCount);
        topic.setCreateTime(createTime);
        topic.setUpdateTime(createTime);
        topic.setIsTop(isTop);
        topic.setIsEssence(isEssence);
        topic.setLiked(0);
        return topic;
    }

    /**
     * 给前几篇帖子配一些评论，不然详情页空荡荡的
     */
    public static List<ReplyEntity> buildReplies(List<TopicEntity> topics) {
        List<ReplyEntity> replies = new ArrayList<>();
        if (topics == null || topics.isEmpty()) {
            return replies;
        }

        String[][] replyTexts = new String[][]{
                {"沙发！这个问题我也想过", "我是大二开始自学的，现在工作两年了", "关键是要动手，光看教程没用"},
                {"看下 gradle-wrapper.properties 里的版本对不对", "换阿里云镜像试试 compileOnly 那个", "清一下 ~/.gradle/caches 目录"},
                {"Snipaste 确实好用，推荐一个，你说的这几个我也都在用", "Bandizip 现在也加广告了，可以换 7-Zip"},
                {"深有同感，我们现在就在改历史项目", "规范这东西，定的时候没人看，改的时候都想起来"},
                {"建议先找工作，经验比学历重要，除非想进大厂", "看你自己想要什么，两条路都没错"},
                {"这个排查过程记录得很清楚，学到了", "本地缓存真的不能随便乱加"},
        };

        long now = System.currentTimeMillis();
        int replyId = 1;

        for (int i = 0; i < topics.size() && i < replyTexts.length; i++) {
            TopicEntity topic = topics.get(i);
            String[] texts = replyTexts[i];
            for (int j = 0; j < texts.length; j++) {
                ReplyEntity reply = new ReplyEntity();
                reply.setTopicId(topic.getId());
                reply.setContent(texts[j]);
                reply.setFloorNo(j + 1);
                reply.setCreateTime(topic.getCreateTime() + (j + 1) * 20L * 60 * 1000);

                // 轮流用几个用户身份，不用真是随机，保证每次跑结果一样
                int authorIndex = (i + j + 1) % 8 + 1;
                reply.setAuthorId(authorIndex);
                reply.setAuthorName(userNameOf(authorIndex));
                reply.setAuthorAvatar("");
                reply.setLikeCount(RANDOM.nextInt(8));
                reply.setReplyToName(null);
                reply.setId(replyId++);
                replies.add(reply);
            }
        }

        return replies;
    }

    private static String userNameOf(int index) {
        String[] names = {"管理员", "张伟", "刘洋", "陈静", "王芳", "赵磊", "孙琪", "周浩"};
        if (index < 1 || index > names.length) {
            return "游客";
        }
        return names[index - 1];
    }
}
