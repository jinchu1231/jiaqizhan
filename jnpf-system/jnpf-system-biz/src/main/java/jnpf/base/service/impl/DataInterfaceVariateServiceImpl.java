package jnpf.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.Page;
import jnpf.base.entity.DataInterfaceVariateEntity;
import jnpf.base.mapper.DataInterfaceVariateMapper;
import jnpf.base.service.DataInterfaceVariateService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DataInterfaceVariateServiceImpl extends SuperServiceImpl<DataInterfaceVariateMapper, DataInterfaceVariateEntity> implements DataInterfaceVariateService {

    @Override
    public List<DataInterfaceVariateEntity> getList(String id, Page page) {
        QueryWrapper<DataInterfaceVariateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(DataInterfaceVariateEntity::getCreatorTime);
        if (StringUtil.isNotEmpty(id)) {
            queryWrapper.lambda().eq(DataInterfaceVariateEntity::getInterfaceId, id);
        }
        if (page != null && StringUtil.isNotEmpty(page.getKeyword())) {
            queryWrapper.lambda().like(DataInterfaceVariateEntity::getFullName, page.getKeyword());
//            queryWrapper.lambda().orderByDesc(DataInterfaceVariateEntity::getLastModifyTime);
        }
        return this.list(queryWrapper);
    }

    @Override
    public DataInterfaceVariateEntity getInfo(String id) {
        QueryWrapper<DataInterfaceVariateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceVariateEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(DataInterfaceVariateEntity entity) {
        QueryWrapper<DataInterfaceVariateEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(entity.getId())) {
            queryWrapper.lambda().ne(DataInterfaceVariateEntity::getId, entity.getId());
        }
        queryWrapper.lambda().eq(DataInterfaceVariateEntity::getFullName, entity.getFullName());
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean create(DataInterfaceVariateEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(UserProvider.getLoginUserId());
        entity.setCreatorTime(DateUtil.getNowDate());
        return this.save(entity);
    }

    @Override
    public boolean update(DataInterfaceVariateEntity entity) {
        return this.updateById(entity);
    }

    @Override
    public boolean delete(DataInterfaceVariateEntity entity) {
        return this.removeById(entity);
    }

    @Override
    public List<DataInterfaceVariateEntity> getListByIds(List<String> ids) {
        if (ids.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        QueryWrapper<DataInterfaceVariateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(DataInterfaceVariateEntity::getId, ids);
        return this.list(queryWrapper);
    }

    @Override
    public boolean update(Map<String, String> map, List<DataInterfaceVariateEntity> variateEntities) {
        if (map == null || map.size() == 0) {
            return true;
        }
        variateEntities.forEach(t -> {
            t.setValue(map.get(t.getInterfaceId()));
        });
        return this.updateBatchById(variateEntities);
    }

    @Override
    public DataInterfaceVariateEntity getInfoByFullName(String fullName) {
        QueryWrapper<DataInterfaceVariateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceVariateEntity::getFullName, fullName);
        return this.getOne(queryWrapper);
    }
}
