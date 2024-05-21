package jnpf.visualdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.RandomUtil;
import jnpf.visualdata.entity.VisualGlobEntity;
import jnpf.visualdata.mapper.VisualGlobMapper;
import jnpf.visualdata.model.visualglob.VisualGlobPaginationModel;
import jnpf.visualdata.service.VisualGlobService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 全局变量
 *
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司
 * @date 2023年7月7日
 */
@Service
public class VisualGlobServiceImpl extends SuperServiceImpl<VisualGlobMapper, VisualGlobEntity> implements VisualGlobService {

    @Override
    public List<VisualGlobEntity> getList(VisualGlobPaginationModel pagination) {
        QueryWrapper<VisualGlobEntity> queryWrapper = new QueryWrapper<>();
        Page page = new Page(pagination.getCurrent(), pagination.getSize());
        IPage<VisualGlobEntity> iPages = this.page(page, queryWrapper);
        return pagination.setData(iPages);
    }

    @Override
    public List<VisualGlobEntity> getList() {
        QueryWrapper<VisualGlobEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public VisualGlobEntity getInfo(String id) {
        QueryWrapper<VisualGlobEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualGlobEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualGlobEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualGlobEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualGlobEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }


}
