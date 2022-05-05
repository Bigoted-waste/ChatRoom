package com.cola.services.impl;

import com.cola.Netty.ChatMsg;
import com.cola.Netty.DataContent;
import com.cola.Netty.UserChannelRel;
import com.cola.enums.MsgActionEnum;
import com.cola.enums.MsgSignFlagEnum;
import com.cola.enums.SearchFriendsStatusEnum;
import com.cola.mapper.*;
import com.cola.pojo.FriendsRequest;
import com.cola.pojo.MyFriends;
import com.cola.pojo.User;
import com.cola.services.UserServices;
import com.cola.utils.FastDFSClient;
import com.cola.utils.FileUtils;
import com.cola.utils.JsonUtils;
import com.cola.utils.QRCodeUtils;
import com.cola.vo.FriendsRequestVo;
import com.cola.vo.MyFriendsVo;
import com.idworker.Sid;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServicesImpl implements UserServices {

    // 注入mapper
    @Autowired
    UserMapper userMapper;

    @Autowired
    MyFriendsMapper myFriendsMapper;

    @Autowired
    FriendsRequestMapper friendsRequestMapper;

    @Autowired
    UserMapperCustom userMapperCustom;

    @Autowired
    ChatMsgMapper chatMsgMapper;

    @Autowired
    Sid sid;

    @Autowired
    QRCodeUtils qrCodeUtils;

    @Autowired
    FastDFSClient fastDFSClient;

    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User queryUserNameIsExit(String username) {
        return userMapper.queryUserNameIsExit(username);
    }

    @Override
    public User insert(User user) {
        // 为每个用户生成唯一的二维码
        String qeCodePath = "E://user"+user.getId()+"qrcode.png";
        // 创建二维码对象信息
        qrCodeUtils.createQRCode(qeCodePath,"bird_qrcode:"+user.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qeCodePath);
        String qrCodeURL="";
        try {
            qrCodeURL = fastDFSClient.uploadQRCode(qrcodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setId(sid.nextShort());
        user.setQrcode(qrCodeURL);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User userResult = userMapper.selectByPrimaryKey(user.getId());
        return userResult;
    }

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        // 1、搜索的用户如果不存在，则返回【无此用户】
        if(user==null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        // 2、搜索的账号是你自己，则返回【不能添加自已】
        if(myUserId.equals(user.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        // 3、搜索的好友已经是你好友，则返回【该用户已经是你的好友】
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(myUserId);
        myFriends.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myFriends);
        if(myF!=null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        // 判断是否是我的好友
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(myUserId);
        myFriends.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myFriends);
        // 如果不是则添加
        if(myF == null){
            FriendsRequest friendsRequest = new FriendsRequest();
            friendsRequest.setId(sid.nextShort());
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(user.getId());
            friendsRequest.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRequest);
        }
    }

    @Override
    public List<FriendsRequestVo> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequest(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        // 进行双向好友保存
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);

        // 删除好友请求中的数据
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        deleteFriendRequest(friendsRequest);


        Channel channel = UserChannelRel.get(sendUserId);
        if(sendUserId!=null){
            // 使用 webSocket 主动推送消息到请求发起者，更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            // 消息推送
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }
    }

    @Override
    public List<MyFriendsVo> queryMyFriend(String userId) {
        return userMapperCustom.queryMyFriends(userId);
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.cola.pojo.ChatMsg msgDB = new com.cola.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());
        chatMsgMapper.insert(msgDB);
        return msgId;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Override
    public List<com.cola.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        List<com.cola.pojo.ChatMsg> result = chatMsgMapper.getUnReadMsgListByAcceptUid(acceptUserId);
        return result;
    }

    // 通过好友请求并保存数据到 my_friends 表中
    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriends.setId(sid.nextShort());
        myFriendsMapper.insert(myFriends);
    }
}
