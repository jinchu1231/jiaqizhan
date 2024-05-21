package jnpf.message.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.message.entity.UserDeviceEntity;
import jnpf.message.mapper.UserDeviceMapper;
import jnpf.message.service.UserDeviceService;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 消息模板（新）
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-18
 */
@Service
public class UserDeviceServiceImpl extends SuperServiceImpl<UserDeviceMapper, UserDeviceEntity> implements UserDeviceService {



    @Autowired
    private UserProvider userProvider;


    @Override
    public UserDeviceEntity getInfoByUserId(String userId){
        QueryWrapper<UserDeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDeviceEntity::getUserId,userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<String> getCidList(String userId){
        List<String> cidList = new ArrayList<>();
        QueryWrapper<UserDeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDeviceEntity::getUserId,userId);
        if(this.list(queryWrapper) != null && this.list(queryWrapper).size()>0) {
            cidList = this.list(queryWrapper).stream().map(t -> t.getClientId()).distinct().collect(Collectors.toList());
        }
        return cidList;
    }

    @Override
    public UserDeviceEntity getInfoByClientId(String clientId){
        QueryWrapper<UserDeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDeviceEntity::getClientId,clientId);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(UserDeviceEntity entity) {
        this.save(entity);
    }

    @Override
    public boolean update(String id, UserDeviceEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(UserDeviceEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

}