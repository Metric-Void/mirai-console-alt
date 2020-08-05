# mirai-console-alt
这是一个[mirai-console](https://github.com/mamoe/mirai-console) 的分支。大部分mirai插件仍在使用0.5.2的API，而1.0(reborn)的文档非常缺少。
因此，本分支意在保留对0.5.2 API的兼容性，并添加一些新功能及接口。版本号将以ALT结尾。

## 构建
```> .\gradlew shadowJar``` 或 ```.\gradlew shadowJvmJar``` 即可

## Bug
在项目根目录(```mirai-console-alt```)中直接```shadowJar```时，```mirai-console```中构建出的文件不包含依赖。

只要进入mirai-console的子目录，并执行```..\gradlew shadowJar```即可。如果构建的是包含Kotlin库的版本(```gradle shadowJvmJar```)则不存在此问题。

<s>这个问题在原仓库中就有，不知道是不是我打开方式不对？</s>

## 模块说明 (同[mirai-console@423858](https://github.com/mamoe/mirai-console/tree/42385895cda605730a50344eb968b1402828477b))

console 由后端和前端一起工作. 使用时必须选择一个前端.

- `mirai-console`: console 的后端, 包含插件管理, 指令系统, 配置系统. 还包含一个轻量命令行的前端 (因此可以独立启动 `mirai-console`).
- `mirai-console-graphical`: console 的 JavaFX 图形化界面前端.
- `mirai-console-terminal`: console 的 Unix 终端界面前端. (实验性)

[`mirai-console-wrapper`](https://github.com/mamoe/mirai-console-wrapper): console 启动器. 可根据用户选择从服务器下载 console 后端, mirai-core, 和指定的前端并启动.
