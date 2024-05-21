package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowEventLogEntity;

import java.util.List;

/**
 * 流程事件日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
public interface FlowEventLogService extends SuperService<FlowEventLogEntity> {

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(FlowEventLogEntity entity);

    /**
     * 获取日志列表
     *
     * @param taskNodeId
     * @return
     */
    List<FlowEventLogEntity> getList(List<String> taskNodeId);

}
