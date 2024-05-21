package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowTaskCirculateEntity;

import java.util.List;

/**
 * 流程传阅
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskCirculateService extends SuperService<FlowTaskCirculateEntity> {

    /**
     * 删除（根据实例Id）
     *
     * @param taskId 任务主键
     * @return
     */
    void deleteByTaskId(String taskId);

    /**
     * 创建
     *
     * @param entitys 实体对象
     * @return
     */
    void create(List<FlowTaskCirculateEntity> entitys);

    /**
     * 获取列表
     *
     * @param taskId
     * @return
     */
    List<FlowTaskCirculateEntity> getList(String taskId);
}
