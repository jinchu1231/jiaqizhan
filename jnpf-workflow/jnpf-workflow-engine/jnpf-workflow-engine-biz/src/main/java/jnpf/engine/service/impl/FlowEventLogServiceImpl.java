package jnpf.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.engine.entity.FlowEventLogEntity;
import jnpf.engine.mapper.FlowEventLogMapper;
import jnpf.engine.service.FlowEventLogService;
import jnpf.util.RandomUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程事件日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Service
public class FlowEventLogServiceImpl extends SuperServiceImpl<FlowEventLogMapper, FlowEventLogEntity> implements FlowEventLogService {


    @Override
    public void create(FlowEventLogEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public List<FlowEventLogEntity> getList(List<String> nodeIdList) {
        QueryWrapper<FlowEventLogEntity> queryWrapper = new QueryWrapper<>();
        if (nodeIdList.size() > 0) {
            queryWrapper.lambda().in(FlowEventLogEntity::getTaskNodeId, nodeIdList);
        }
        return this.list(queryWrapper);
    }
}
