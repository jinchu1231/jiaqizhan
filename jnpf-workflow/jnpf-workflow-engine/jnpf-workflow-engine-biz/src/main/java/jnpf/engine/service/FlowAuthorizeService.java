package jnpf.engine.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowAuthorizeEntity;

import java.util.List;


/**
 * 流程权限表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
public interface FlowAuthorizeService extends SuperService<FlowAuthorizeEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<FlowAuthorizeEntity> getList(String taskId, String nodeCode, SFunction<FlowAuthorizeEntity, ?>... columns);

    /**
     * 创建
     *
     * @param list 实体对象
     */
    void create(List<FlowAuthorizeEntity> list);

}
