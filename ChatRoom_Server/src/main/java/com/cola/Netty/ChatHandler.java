package com.cola.Netty;

import com.cola.SpringUtil;
import com.cola.enums.MsgActionEnum;
import com.cola.services.UserServices;
import com.cola.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于处理消息的handler
 *      由于它的传输数据的载体是frame, 这个 frame 在netty中，
 *      是用于websocket专门处理文本对象的，frame是消息的载体，此类叫：TextWebSocket
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 用于记录和管理所有客户端的channel
    public static ChannelGroup users =new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取客户端所传输的消息
        String content = msg.text();
        // 1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        Channel channel = ctx.channel();
        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if(action == MsgActionEnum.CONNECT.type){
            // 2.1 当websocket 第一次open时候，初始化channel，把用的channel 和 userid 关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId,channel);

            // 测试
            for (Channel user : users) {
                System.out.println(user.id().asLongText());
            }
            UserChannelRel.output();

        }else if(action == MsgActionEnum.CHAT.type){
            // 2.2 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态【为签收】
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgContent = chatMsg.getMsg();
            String senderId = chatMsg.getSenderId();
            String receiverId = chatMsg.getReceiverId();
            // 保存消息到数据库，并且标记为未签收
            UserServices userServices = (UserServices)SpringUtil.getBean("userServicesImpl");
            String msgId = userServices.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

//            UserChannelRel.output();

            // 发送消息
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null){
                // 离线用户
            }else {
                // 当 receiverChannel 不为空的时候, 从 ChannelGroup 查找对应的channel 是否存在
                Channel findChannel = users.find(receiverChannel.id());
                if(findChannel!=null){
                    // 用户在线
                    findChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContentMsg)
                            )
                    );
                }else {
                    // 离线用户
                }
            }


        }else if(action == MsgActionEnum.SIGNED.type){
            // 2.3 签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态【已签收】
            UserServices userServices = (UserServices)SpringUtil.getBean("userServicesImpl");
            // 扩展字段在signed 类型消息中，代表需要去签收的消息id，逗号间隔
            String msgIdsStr = dataContent.getExtand();
            String[] msgsId = msgIdsStr.split(",");
            List<String> msgIdList = new ArrayList<>();
            for (String mid : msgsId) {
                if(StringUtils.isNotBlank(mid)){
                    msgIdList.add(mid);
                }
            }
            System.out.println(msgIdList.toString());
            if(msgIdList != null && !msgIdList.isEmpty() && msgIdList.size()>0){
                // 批量签收
                userServices.updateMsgSigned(msgIdList);
            }

        }else if(action == MsgActionEnum.KEEPALIVE.type){
            // 2.4 心跳类型的消息
            System.out.println("收到来之channel 为【"+channel+"】的心跳包");
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("客户端开启：");
        users.add(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生了异常后关闭连接，同时从ChannelGroup移除
        ctx.channel().close();
        users.remove(ctx.channel());

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("客户端被移除: channel id 为:"+channelId);
        users.remove(ctx.channel());
    }
}
