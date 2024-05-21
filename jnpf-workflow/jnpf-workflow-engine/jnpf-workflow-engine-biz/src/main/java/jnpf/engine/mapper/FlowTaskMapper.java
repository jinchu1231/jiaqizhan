package jnpf.engine.mapper;

import jnpf.base.mapper.SuperMapper;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowtask.FlowTaskListModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 流程任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskMapper extends SuperMapper<FlowTaskEntity> {
    /**
     * 已办事宜
     *
     * @return
     */
    List<FlowTaskListModel> getTrialList(@Param("map") Map<String, Object> map);

    /**
     * 抄送事宜
     *
     * @return
     */
    List<FlowTaskListModel> getCirculateList(@Param("map") Map<String, Object> map);

    /**
     * 待办事宜
     *
     * @return
     */
    List<FlowTaskListModel> getWaitList(@Param("map") Map<String, Object> map);

}
