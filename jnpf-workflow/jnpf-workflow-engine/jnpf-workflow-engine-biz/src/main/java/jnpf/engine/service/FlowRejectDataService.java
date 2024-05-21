package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowRejectDataEntity;

/**
 * 冻结审批
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
public interface FlowRejectDataService extends SuperService<FlowRejectDataEntity> {

    /**
     * 新增
     *
     * @param rejectEntity
     */
    void createOrUpdate(FlowRejectDataEntity rejectEntity);

    /**
     * 新增
     *
     * @param rejectEntity
     */
    void create(FlowRejectDataEntity rejectEntity);

    /**
     * 更新
     *
     * @param rejectEntity
     */
    void update(String id, FlowRejectDataEntity rejectEntity);

    /**
     * 获取信息
     *
     * @param id
     * @return
     */
    FlowRejectDataEntity getInfo(String id);

}
