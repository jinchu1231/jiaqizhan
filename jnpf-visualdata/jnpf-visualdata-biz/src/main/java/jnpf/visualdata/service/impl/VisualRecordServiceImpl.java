package jnpf.visualdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.RandomUtil;
import jnpf.visualdata.entity.VisualRecordEntity;
import jnpf.visualdata.mapper.VisualRecordMapper;
import jnpf.visualdata.model.VisualPagination;
import jnpf.visualdata.service.VisualRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 大屏数据集
 *
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司
 * @date 2023年7月7日
 */
@Service
public class VisualRecordServiceImpl extends SuperServiceImpl<VisualRecordMapper, VisualRecordEntity> implements VisualRecordService {

    @Override
    public List<VisualRecordEntity> getList(VisualPagination pagination) {
        QueryWrapper<VisualRecordEntity> queryWrapper = new QueryWrapper<>();
        Page page = new Page(pagination.getCurrent(), pagination.getSize());
        IPage<VisualRecordEntity> iPages = this.page(page, queryWrapper);
        return pagination.setData(iPages);
    }

    @Override
    public List<VisualRecordEntity> getList() {
        QueryWrapper<VisualRecordEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public VisualRecordEntity getInfo(String id) {
        QueryWrapper<VisualRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualRecordEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualRecordEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualRecordEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualRecordEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }


}
