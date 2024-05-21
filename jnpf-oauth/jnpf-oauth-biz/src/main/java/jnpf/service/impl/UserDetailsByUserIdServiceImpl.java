package jnpf.service.impl;

import jnpf.base.UserInfo;
import jnpf.constant.MsgCode;
import jnpf.consts.AuthConsts;
import jnpf.exception.LoginException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * 使用用户ID获取用户信息
 */
@Service(AuthConsts.USERDETAIL_USER_ID)
public class UserDetailsByUserIdServiceImpl implements UserDetailService {

    private static final Integer ORDER = 1;

    @Autowired
    private UserService userService;

    @Override
    public UserEntity loadUserEntity(UserInfo userInfo) throws LoginException {
        UserEntity userEntity = userService.getInfoByAccount(userInfo.getUserId(), userInfo.getTenantId());
        if (userEntity == null) {
            throw new LoginException(MsgCode.LOG101.get());
        }
        return userEntity;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

}
