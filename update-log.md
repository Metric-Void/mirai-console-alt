# 更新日志

### 0.5.4.1-ALT
- 完成了ComplexRouting.
- 修复了许多bug，包括一些乱七八糟的错误（捂脸

### 0.5.4-ALT
- 加入了mirai-router并自动加载。Java用户可以直接使用Router订阅消息事件。
目前router提供的功能有限，kotlin api在subscribe中已经有了各种filter，可以完成此处的功能。

    将来会加入```ComplexRouting```，使用网络流进行消息解析，减少匹配次数 提供更好的性能。

[Router的使用方法详见这里](docs/router.md)

### 0.5.3-ALT
这个版本加入的大多数是QoL(Quality of Life)提升。
- stop指令。给不喜欢Ctrl+C的人一些心理安慰
- 启动时自动读取根目录（不是contents）下的config.txt。可以用于登录，通知主人之类
  - ```#``` 开头的原样输出
  - ```//``` 开头的是注释
  - 其余的作为指令处理
  - 保存config.txt时请选择没有BOM的格式（测试使用的是UTF-8），否则第一行会无法识别。<s>懒得多写代码了</s>