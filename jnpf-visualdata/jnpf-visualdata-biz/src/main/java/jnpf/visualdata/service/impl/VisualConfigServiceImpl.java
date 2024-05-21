package jnpf.visualdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.RandomUtil;
import jnpf.visualdata.entity.VisualConfigEntity;
import jnpf.visualdata.mapper.VisualConfigMapper;
import jnpf.visualdata.service.VisualConfigService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 大屏基本配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Service
public class VisualConfigServiceImpl extends SuperServiceImpl<VisualConfigMapper, VisualConfigEntity> implements VisualConfigService {

    @Override
    public List<VisualConfigEntity> getList() {
        QueryWrapper<VisualConfigEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public VisualConfigEntity getInfo(String id) {
        QueryWrapper<VisualConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualConfigEntity::getVisualId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(VisualConfigEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, VisualConfigEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(VisualConfigEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
}
