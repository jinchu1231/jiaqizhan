package jnpf.mapper;

import jnpf.base.mapper.SuperMapper;
import jnpf.entity.FlowFormEntity;
import jnpf.model.flow.FlowTempInfoModel;
import org.apache.ibatis.annotations.Param;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 18:00
 */
public interface FlowFormMapper extends SuperMapper<FlowFormEntity> {

    FlowTempInfoModel findFLowInfo(@Param("tempId") String tempId);

}
