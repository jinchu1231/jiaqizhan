package jnpf.integrate.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.integrate.entity.IntegrateNodeEntity;
import jnpf.integrate.mapper.IntegrateNodeMapper;
import jnpf.integrate.service.IntegrateNodeService;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class IntegrateNodeServiceImpl extends SuperServiceImpl<IntegrateNodeMapper, IntegrateNodeEntity> implements IntegrateNodeService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<IntegrateNodeEntity> getList(List<String> id, String nodeCode) {
       return getList(id,nodeCode,null);
    }

    @Override
    public List<IntegrateNodeEntity> getList(List<String> id, String nodeCode, Integer resultType) {
        List<IntegrateNodeEntity> list = new ArrayList<>();
        QueryWrapper<IntegrateNodeEntity> queryWrapper = new QueryWrapper<>();
        if (id.size() > 0) {
            queryWrapper.lambda().in(IntegrateNodeEntity::getTaskId, id);
            if (StringUtil.isNotEmpty(nodeCode)) {
                queryWrapper.lambda().eq(IntegrateNodeEntity::getNodeCode, nodeCode);
            }
            if(ObjectUtil.isNotEmpty(resultType)){
                queryWrapper.lambda().eq(IntegrateNodeEntity::getResultType, resultType);
            }
            queryWrapper.lambda().orderByAsc(IntegrateNodeEntity::getSortCode);
            list.addAll(this.list(queryWrapper));
        }
        return list;
    }

    @Override
    public IntegrateNodeEntity getInfo(String id) {
        QueryWrapper<IntegrateNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IntegrateNodeEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(IntegrateNodeEntity entity) {
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(RandomUtil.uuId());
        }
        entity.setIsRetry(1);
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setEnabledMark(1);
        this.save(entity);
    }

    @Override
    public void update(String taskId, String nodeCode) {
        UpdateWrapper<IntegrateNodeEntity> queryWrapper = new UpdateWrapper<>();
        queryWrapper.lambda().eq(IntegrateNodeEntity::getTaskId,taskId);
        queryWrapper.lambda().eq(IntegrateNodeEntity::getNodeCode,nodeCode);
        queryWrapper.lambda().set(IntegrateNodeEntity::getIsRetry,0);
        this.update(queryWrapper);
    }

    @Override
    public Boolean update(String id, IntegrateNodeEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        return this.updateById(entity);
    }

    @Override
    public void delete(IntegrateNodeEntity entity) {
        if(entity!=null) {
            this.removeById(entity.getId());
        }
    }

    @Override
    public void delete(String taskId) {
        QueryWrapper<IntegrateNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IntegrateNodeEntity::getTaskId,taskId);
        this.remove(queryWrapper);
    }

}
