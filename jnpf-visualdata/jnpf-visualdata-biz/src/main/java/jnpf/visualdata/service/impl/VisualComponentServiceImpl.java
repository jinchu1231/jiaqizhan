package jnpf.visualdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.RandomUtil;
import jnpf.visualdata.entity.VisualComponentEntity;
import jnpf.visualdata.mapper.VisualComponentMapper;
import jnpf.visualdata.model.visualcomponent.VisualComponentPaginationModel;
import jnpf.visualdata.service.VisualComponentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 大屏组件库
 *
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司
 * @date 2023年7月7日
 */
@Service
public class VisualComponentServiceImpl extends SuperServiceImpl<VisualComponentMapper, VisualComponentEntity> implements VisualComponentService {

    @Override
    public List<VisualComponentEntity> getList(VisualComponentPaginationModel pagination) {
        QueryWrapper<VisualComponentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualComponentEntity::getType, pagination.getType());
        Page page = new Page(pagination.getCurrent(), pagination.getSize());
        IPage<VisualComponentEntity> iPages = this.page(page, queryWrapper);
        return pagination.setData(iPages);
    }

    @Override
    public List<VisualComponentEntity> getList() {
        QueryWrapper<VisualComponentEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public VisualComponentEntity getInfo(String id) {
        QueryWrapper<VisualComponentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualComponentEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualComponentEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualComponentEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualComponentEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }


}
