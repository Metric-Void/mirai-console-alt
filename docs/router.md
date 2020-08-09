# Router的使用方法
```$xslt
Router.getInstance().addGroupRouting(
                Routing.serialRoute()
                    .thenMatch(new PrefixMatcher("router测试")) // 可以有多个thenMatch
                    .setTarget((rr) -> {  // 全部匹配之后执行的Consumer
                        rr.getGroupEventSource().getGroup().sendMessage("Route OK");
                    })
        );
```
没错就是这么简单暴力。

### [Matcher](../mirai-console/src/main/java/com/metricv/mirai/matcher/Matcher.java)
Matcher是一个消息匹配器。它可以匹配单个消息元素。消息元素包括以下几种：
- PlainText：就是普通的文字。
- At：@圈人
- AtAll：@全体成员
- Image：图片
- Face：表情

目前有以下几种基础的消息匹配器：
- PlainText
  - [RegexMatcher](../mirai-console/src/main/java/com/metricv/mirai/matcher/RegexMatcher.java) ：正则匹配器，使用正则表达式匹配文字
  - [PrefixMatcher](../mirai-console/src/main/java/com/metricv/mirai/matcher/PrefixMatcher.java) ：前缀匹配器。匹配消息的开头
- At/AtAll
  - [AtMatcher](../mirai-console/src/main/java/com/metricv/mirai/matcher/AtMatcher.java) ：匹配@。-1代表圈任何人，0代表圈所有人。

也可以通过继承Matcher，写自己的消息匹配器。

### [Routing](../mirai-console/src/main/java/com/metricv/mirai/router/Routing.java)
一个Routing是一系列Matcher的组合，通过一系列Matcher来匹配一条消息。
匹配出来的内容会被存储到一个[RoutingResult](../mirai-console/src/main/java/com/metricv/mirai/router/RoutingResult.java)中，供你的函数读取。你可以给匹配出的结果打上标签 方便读取。

各个Matcher匹配出的内容是不一样的，详见各自的JavaDoc。便利起见，你也可以往RoutingResult里面加自己的参数。

### [Router](../mirai-console/src/main/java/com/metricv/mirai/router/Router.java)
一个消息路由器，包含众多的Routing。在收到消息时，使用线程池异步执行各个Routing。
