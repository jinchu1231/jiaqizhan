package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jnpf.base.service.SuperServiceImpl;
import jnpf.entity.FlowFormRelationEntity;
import jnpf.mapper.FlowFormRelationMapper;
import jnpf.service.FlowFormRelationService;
import jnpf.util.RandomUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程表单关联
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 18:01
 */
@Service
public class FlowFormRelationServiceImpl extends SuperServiceImpl<FlowFormRelationMapper, FlowFormRelationEntity> implements FlowFormRelationService {

    @Override
    public void saveFlowIdByFormIds(String flowId, List<String> formIds) {
        QueryWrapper<FlowFormRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormRelationEntity::getFlowId, flowId);
        List<FlowFormRelationEntity> list = this.list(queryWrapper);
        this.removeBatchByIds(list);
        if(CollectionUtils.isNotEmpty(formIds)){
            for(String formId:formIds){
                FlowFormRelationEntity entity=new FlowFormRelationEntity();
                entity.setFlowId(flowId);
                entity.setId(RandomUtil.uuId());
                entity.setFormId(formId);
                this.save(entity);
            }
        }
    }

    @Override
    public List<FlowFormRelationEntity> getListByFormId(String formId) {
        QueryWrapper<FlowFormRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormRelationEntity::getFormId, formId);
        List<FlowFormRelationEntity> list = this.list(queryWrapper);
        return list;
    }
}
