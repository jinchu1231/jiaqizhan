package jnpf.base.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import jnpf.base.Pagination;
import jnpf.base.entity.ModuleButtonEntity;
import jnpf.base.mapper.ModuleButtonMapper;
import jnpf.base.service.ModuleButtonService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 按钮权限
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ModuleButtonServiceImpl extends SuperServiceImpl<ModuleButtonMapper, ModuleButtonEntity> implements ModuleButtonService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ModuleButtonEntity> getListByModuleIds() {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        // 排序
        queryWrapper.lambda().orderByAsc(ModuleButtonEntity::getSortCode)
                .orderByDesc(ModuleButtonEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleButtonEntity> getEnabledMarkList(String enabledMark) {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getEnabledMark,enabledMark);
        // 排序
        queryWrapper.lambda().orderByAsc(ModuleButtonEntity::getSortCode)
                .orderByDesc(ModuleButtonEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleButtonEntity> getListByModuleIds(String moduleId) {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getModuleId, moduleId).orderByAsc(ModuleButtonEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleButtonEntity> getListByModuleIds(String moduleId, Pagination pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getModuleId, moduleId);
        //关键字查询
        if(!StringUtil.isEmpty(pagination.getKeyword())){
            flag = true;
            queryWrapper.lambda().and(
                    t->t.like(ModuleButtonEntity::getFullName,pagination.getKeyword())
                            .or().like(ModuleButtonEntity::getEnCode,pagination.getKeyword())
            );
        }
        // 排序
        queryWrapper.lambda().orderByAsc(ModuleButtonEntity::getSortCode)
                .orderByDesc(ModuleButtonEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(ModuleButtonEntity::getLastModifyTime);
        }
        return this.list(queryWrapper);
    }

    @Override
    public ModuleButtonEntity getInfo(String id) {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public ModuleButtonEntity getInfo(String id, String moduleId) {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getId, id);
        queryWrapper.lambda().eq(ModuleButtonEntity::getModuleId, moduleId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String moduleId, String fullName, String id) {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getFullName, fullName).eq(ModuleButtonEntity::getModuleId, moduleId);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(ModuleButtonEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String moduleId, String enCode, String id) {
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleButtonEntity::getEnCode, enCode).eq(ModuleButtonEntity::getModuleId, moduleId);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(ModuleButtonEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void create(ModuleButtonEntity entity) {
//        if (entity.getId() == null) {
            entity.setId(RandomUtil.uuId());
//        }
        this.save(entity);
    }

    @Override
    public boolean update(String id, ModuleButtonEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
       return this.updateById(entity);
    }

    @Override
    public void delete(ModuleButtonEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public List<ModuleButtonEntity> getListByModuleIds(List<String> ids) {
        if (ids.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        List<List<String>> lists = Lists.partition(ids, 1000);
        for (List<String> list : lists) {
            queryWrapper.lambda().or().in(ModuleButtonEntity::getModuleId, list);
        }
        queryWrapper.lambda().eq(ModuleButtonEntity::getEnabledMark, 1);
        queryWrapper.lambda().orderByAsc(ModuleButtonEntity::getSortCode).orderByDesc(ModuleButtonEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleButtonEntity> getListByIds(List<String> ids) {
        if (ids.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleButtonEntity> queryWrapper = new QueryWrapper<>();
        List<List<String>> lists = Lists.partition(ids, 1000);
        for (List<String> list : lists) {
            queryWrapper.lambda().or().in(ModuleButtonEntity::getId, list);
        }
        queryWrapper.lambda().eq(ModuleButtonEntity::getEnabledMark, 1);
        return this.list(queryWrapper);
    }


}
