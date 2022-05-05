package com.cola.Netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 用于检测 channel 的心跳handler
 * 继承 ChannelInboundHandlerAdapter ，目的是不需要实现ChannelRead0 这个方法
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event =(IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                System.out.println("进入读空闲状态........");
            }else if(event.state() == IdleState.WRITER_IDLE){
                System.out.println("进入写空闲状态........");
            }else if(event.state() == IdleState.ALL_IDLE){
                System.out.println("channel 关闭之前：users 的数值为："+ ChatHandler.users.size());
                Channel channel = ctx.channel();
                // 资源释放
                channel.close();
                System.out.println("channel 关闭之后：users 的数值为："+ ChatHandler.users.size());
            }
        }
    }
}
