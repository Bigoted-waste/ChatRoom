package com.cola.mapper;

import com.cola.vo.FriendsRequestVo;
import com.cola.vo.MyFriendsVo;

import java.util.List;

public interface UserMapperCustom {

    List<FriendsRequestVo> queryFriendRequest(String acceptUserId);

    List<MyFriendsVo> queryMyFriends(String userId);

    void batchUpdateMsgSigned(List<String> msgIdList);

}
