package com.cola;

import com.cola.Netty.WebSocketServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 在IOC的容器的启动过程，当所有的bean都已经处理完成之后，spring ioc 容器都会有一个发布事件的动作
 * 让我们的bean实现 ApplicationListener 接口，这样当发布事件时，【spring】 的ioc容器就会以容器的实力对象作为事件源类，
 * 并从中找到事件的监听者，此时ApplicationListener 接口实力中 onApplicationEvent(E event) 方法就会被调用。
 */

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext().getParent() == null){
            try{
                WebSocketServer.getInstance().start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
