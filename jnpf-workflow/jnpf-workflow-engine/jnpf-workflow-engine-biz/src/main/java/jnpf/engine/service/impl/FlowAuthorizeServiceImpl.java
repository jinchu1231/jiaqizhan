package jnpf.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import jnpf.base.service.SuperServiceImpl;
import jnpf.engine.entity.FlowAuthorizeEntity;
import jnpf.engine.mapper.FlowAuthorizeMapper;
import jnpf.engine.service.FlowAuthorizeService;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程权限表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Service
public class FlowAuthorizeServiceImpl extends SuperServiceImpl<FlowAuthorizeMapper, FlowAuthorizeEntity> implements FlowAuthorizeService {

    @Override
    public List<FlowAuthorizeEntity> getList(String taskId, String nodeCode, SFunction<FlowAuthorizeEntity, ?>... columns) {
        QueryWrapper<FlowAuthorizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowAuthorizeEntity::getTaskId, taskId);
        if (StringUtil.isNotEmpty(nodeCode)) {
            queryWrapper.lambda().eq(FlowAuthorizeEntity::getNodeCode, nodeCode);
        }
        queryWrapper.lambda().select(columns);
        return this.list(queryWrapper);
    }

    @Override
    public void create(List<FlowAuthorizeEntity> list) {
        for (FlowAuthorizeEntity entity : list) {
            entity.setId(RandomUtil.uuId());
            this.save(entity);
        }
    }
}
