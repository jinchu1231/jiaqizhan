package jnpf.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.engine.entity.FlowRejectDataEntity;
import jnpf.engine.mapper.FlowRejectDataMapper;
import jnpf.engine.service.FlowRejectDataService;
import jnpf.util.RandomUtil;
import org.springframework.stereotype.Service;

/**
 * 冻结审批
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
@Service
public class FlowRejectDataDataServiceImpl extends SuperServiceImpl<FlowRejectDataMapper, FlowRejectDataEntity> implements FlowRejectDataService {

    @Override
    public void createOrUpdate(FlowRejectDataEntity rejectEntity) {
        this.saveOrUpdate(rejectEntity);
    }

    @Override
    public void create(FlowRejectDataEntity rejectEntity) {
        if (rejectEntity.getId() == null) {
            rejectEntity.setId(RandomUtil.uuId());
        }
        this.save(rejectEntity);
    }

    @Override
    public void update(String id, FlowRejectDataEntity rejectEntity) {
        rejectEntity.setId(id);
        this.updateById(rejectEntity);
    }

    @Override
    public FlowRejectDataEntity getInfo(String id) {
        QueryWrapper<FlowRejectDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowRejectDataEntity::getId, id);
        return this.getOne(queryWrapper);
    }

}
