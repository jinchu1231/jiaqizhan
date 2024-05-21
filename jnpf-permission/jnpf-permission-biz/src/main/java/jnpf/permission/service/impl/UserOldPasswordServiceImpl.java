package jnpf.permission.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.permission.entity.UserOldPasswordEntity;
import jnpf.permission.mapper.UserOldPasswordMapper;
import jnpf.permission.service.UserOldPasswordService;
import jnpf.permission.service.UserRelationService;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
@DSTransactional
public class UserOldPasswordServiceImpl extends SuperServiceImpl<UserOldPasswordMapper, UserOldPasswordEntity> implements UserOldPasswordService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserRelationService userRelationService;

    @Override
    public List<UserOldPasswordEntity> getList(String userId) {
        QueryWrapper<UserOldPasswordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserOldPasswordEntity::getUserId,userId);
        queryWrapper.lambda().orderByDesc(UserOldPasswordEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean create(UserOldPasswordEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorTime(DateUtil.getNowDate());
        this.save(entity);
        return true;
    }

}
