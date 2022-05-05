package com.cola.services;

import com.cola.Netty.ChatMsg;
import com.cola.pojo.FriendsRequest;
import com.cola.pojo.User;
import com.cola.vo.FriendsRequestVo;
import com.cola.vo.MyFriendsVo;

import java.util.List;

public interface UserServices {

    User getUserById(String id);

    // 根据用户名查找指定用户对象
    User queryUserNameIsExit(String username);

    // 保存
    User insert(User user);

    // 修改用户
    User updateUserInfo(User user);

    // 搜索好友的前置条件接口
    Integer preconditionSearchFriends(String myUserId, String friendUserName);

    // 发送好友请求
    void sendFriendRequest(String myUserId,String friendUserName);

    // 好友请求列表查询
    List<FriendsRequestVo> queryFriendRequestList(String acceptUserId);

    // 处理好友请求 —— 忽略好友请求
    void deleteFriendRequest(FriendsRequest friendsRequest);

    // 处理好友请求 —— 通过好友请求
    void passFriendRequest(String sendUserId,String acceptUserId);

    // 好友列表查询
    List<MyFriendsVo> queryMyFriend(String userId);

    // 保存用户聊天消息
    String saveMsg(ChatMsg chatMsg);

    //
    void updateMsgSigned(List<String> msgIdList);

    // 获取未签收的消息列表
    List<com.cola.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
