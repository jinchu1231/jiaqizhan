package jnpf.permission.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.permission.entity.SignEntity;
import jnpf.permission.mapper.SignMapper;
import jnpf.permission.service.SignService;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 个人签名
 *
 * @author JNPF开发平台组
 * @copyright 引迈信息技术有限公司
 * @date 2022年9月2日 上午9:18
 */
@Service
public class SignServiceImpl extends SuperServiceImpl<SignMapper, SignEntity> implements SignService {

    @Autowired
    private UserProvider userProvider;


    @Override
    public List<SignEntity> getList() {
        QueryWrapper<SignEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SignEntity::getCreatorUserId, userProvider.get().getUserId())
                .orderByDesc(SignEntity::getCreatorTime);

        return this.list(queryWrapper);
    }


    @Override
    public boolean create(SignEntity entity) {
        QueryWrapper<SignEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SignEntity::getIsDefault, 1)
                .eq(SignEntity::getCreatorUserId, userProvider.get().getUserId());
        SignEntity signEntity = this.getOne(queryWrapper);
        if (entity.getIsDefault() == 0) {
            if (signEntity == null) {
                entity.setIsDefault(1);
            }
        } else {
            if (signEntity != null) {
                signEntity.setIsDefault(0);
                this.updateById(signEntity);
            }
        }
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        return this.save(entity);
    }


    @Override
    public boolean delete(String id) {
        return this.removeById(id);
    }


    @Override
    public boolean updateDefault(String id) {
        QueryWrapper<SignEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SignEntity::getIsDefault, 1).eq(SignEntity::getCreatorUserId, userProvider.get().getUserId());
        SignEntity signEntity = this.getOne(queryWrapper);
        if (signEntity != null) {
            signEntity.setIsDefault(0);
            this.updateById(signEntity);
        }
        SignEntity entity = this.getById(id);
        if (entity != null) {
            entity.setIsDefault(1);
            return this.updateById(entity);
        }
        return false;
    }


    @Override
    public SignEntity getDefaultByUserId(String id) {
        QueryWrapper<SignEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SignEntity::getIsDefault, 1).eq(SignEntity::getCreatorUserId, id);
        return this.getOne(queryWrapper);
    }


}
