package com.cola.Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServer {

    private static class SingletonWSServer{
        static final WebSocketServer instance = new WebSocketServer();
    }

    public static WebSocketServer getInstance(){
        return SingletonWSServer.instance;
    }

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture future;

    public WebSocketServer(){
        boss = new NioEventLoopGroup();
        worker =new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitializer());
    }

    public void start(){
        this.future = serverBootstrap.bind(8088);
        System.out.println("netty webSocket server 启动完毕.....");
    }

//    public static void main(String[] args) {
//        // 创建主从线程池
//        NioEventLoopGroup boss = new NioEventLoopGroup();
//        NioEventLoopGroup worker = new NioEventLoopGroup();
//        try {
//            // 创建服务器类
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            serverBootstrap.group(boss,worker)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new WSServerInitializer());
//            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();
//            channelFuture.channel().closeFuture().sync();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//          boss.shutdownGracefully();
//          worker.shutdownGracefully();
//        }
//
//    }
}
