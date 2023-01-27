### 通过 groovy 爬取幼儿园App照片视频

此项目作为基于groovy 接口爬虫的案例，基于多线程+groovy+hutool+logback实现的爬虫。

![](images/5a5accf0456ee4758a5ae4d58a91ac6.jpg)

上图为掌通家园App的版本信息。


作为孩子的家长，娃娃在幼儿园的一些珍贵照片老师都通过掌通家园发布在网络上。这些照片我们只有通过APP才能看到。

由于本人有强大的NAS系统做存储支持，于是产生了一个项目就是爬取这些照片信息到本地NAS存储。

### 整体思路

1、通过http（s）代理监听app发送的http请求信息；

![](images/20230127195922.jpg)

2、通过PostMan测试这些获取token和用户信息查询掌通家园在园时光列表；


3、新建java maven结构项目，加入groovy-all + hutool-all + logback；


4、开始编码利用groovy的模板语法构建参数、hutool提供的http工具请求api接口+download文件；


通过代码爬取了47GB的照片和图片，从孩子入园到转学后两年多时间的珍贵照片。

### 发现的问题

掌通家园app部分接口不需要登录也可以访问，如果不法分子了解到这些schoolId和classId后，可以批量拉取儿童们的照片。

当然本人只拉取了我们家孩子的照片，作为留念使用，未来孩子长大了给孩子看看小时候的照片。


