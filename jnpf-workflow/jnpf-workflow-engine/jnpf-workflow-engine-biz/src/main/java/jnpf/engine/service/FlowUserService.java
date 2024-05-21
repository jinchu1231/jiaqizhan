package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowUserEntity;

/**
 * 流程发起用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
public interface FlowUserService extends SuperService<FlowUserEntity> {

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowUserEntity getInfo(String id);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowUserEntity getTaskInfo(String id);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(FlowUserEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    void update(String id, FlowUserEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     * @return
     */
    void delete(FlowUserEntity entity);

    /**
     * 删除
     *
     * @param taskId
     */
    void deleteByTaskId(String taskId);
}
