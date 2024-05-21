package jnpf.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.permission.connector.UserInfoService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.Md5Util;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户信息保存
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/7/28 14:38
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserService userService;

    @Override
    public Boolean create(Map<String, Object> map) {
        UserEntity entity = JsonUtil.getJsonToBean(map, UserEntity.class);
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, entity.getAccount());
        UserEntity entity1 = userService.getOne(queryWrapper);
        if (entity1 != null) {
            entity.setId(entity1.getId());
            return userService.updateById(entity);
        } else {
            if (StringUtil.isEmpty(entity.getId())) {
                String userId = RandomUtil.uuId();
                entity.setId(userId);
            }
            entity.setSecretkey(RandomUtil.uuId());
            entity.setPassword(Md5Util.getStringMd5(entity.getPassword().toLowerCase() + entity.getSecretkey().toLowerCase()));
            entity.setIsAdministrator(0);
            return userService.save(entity);
        }
    }

    @Override
    public Boolean update(Map<String, Object> map) {
        return create(map);
    }

    @Override
    public Boolean delete(Map<String, Object> map) {
        UserEntity entity = JsonUtil.getJsonToBean(map, UserEntity.class);
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, entity.getAccount());
        UserEntity entity1 = userService.getOne(queryWrapper);
        if (entity1 != null) {
            entity.setId(entity1.getId());
        }
        return userService.removeById(entity.getId());
    }

    @Override
    public Map<String, Object> getInfo(String id) {
        UserEntity entity = userService.getInfo(id);
        return JsonUtil.entityToMap(entity);
    }
}
