package jnpf.engine.model.flowmessage;

import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.model.flowengine.shuntjson.childnode.TemplateJsonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/3/31 16:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowEventModel {

    //数据
    private String dataJson;
    //表单数据
    private Map<String, Object> data;
    //系统匹配
    private TemplateJsonModel templateJson;
    //操作对象
    private FlowTaskOperatorRecordEntity record;

}
