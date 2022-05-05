# ChatRoom

# 聊天工具

chatRoom 是由 SpringBoot + MyBatis + HTML5 + Netty（WebSocket）等技术实现的聊天工具，其中聊天功能用Netty中WebSocket长连接来实现

如果觉得项目不错，请帮忙`Star`支持一下

## 技术栈

#### 后端技术栈

后端主要采用了：

* 核心框架：SpringBoot
* 持久层框架：MyBatis
* 存储框架： Nginx+FastDFS
* Netty
  * 心跳检测
  * WebSocket 长连接

#### 前端技术栈

* HTML5
* ajax
* Mui

### 未实现功能

* 手机号登录（验证码功能）
* 添加Redis缓存
* 把全局ID改成Redis的全局自增主键
* 添加Kafka消息队列