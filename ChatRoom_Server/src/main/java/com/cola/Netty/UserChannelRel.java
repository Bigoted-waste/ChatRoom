package com.cola.Netty;


import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户id 和 channel 的关联关系处理
 */
public class UserChannelRel {
    private static HashMap<String, Channel> manage = new HashMap<>();

    public static void put(String senderId,Channel channel){
        manage.put(senderId,channel);
    }

    public static Channel get(String senderId){
        return manage.get(senderId);
    }

    public static void output(){
        for (Map.Entry<String, Channel> entry : manage.entrySet()) {
            System.out.println("UserId："+entry.getKey()
                +",ChannelId："+entry.getValue().id().asLongText()
            );
        }
    }
}
