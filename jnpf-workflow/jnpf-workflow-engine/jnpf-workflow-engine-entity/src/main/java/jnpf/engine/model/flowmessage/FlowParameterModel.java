package jnpf.engine.model.flowmessage;

import lombok.Data;

import java.util.Map;

/**
 * 事件对象
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2022/8/20 8:49
 */
@Data
public class FlowParameterModel {
    private String interId;
    private Map<String, String> parameterMap;
}
