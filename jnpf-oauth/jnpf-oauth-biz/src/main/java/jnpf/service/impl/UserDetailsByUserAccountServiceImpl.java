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
 * 默认使用用户名获取用户信息
 */
@Service(AuthConsts.USERDETAIL_ACCOUNT)
public class UserDetailsByUserAccountServiceImpl implements UserDetailService {

    @Autowired
    private UserService userService;

    @Override
    public UserEntity loadUserEntity(UserInfo userInfo) throws LoginException {
        UserEntity userEntity = userService.getInfoByAccount(userInfo.getUserAccount(), userInfo.getTenantId());
        if (userEntity == null) {
            throw new LoginException(MsgCode.LOG101.get());
        }
        return userEntity;
    }


    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

}
