package jnpf.engine.model.flowtemplatejson;

import jnpf.base.PaginationTime;
import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class FlowTemplateJsonPage extends PaginationTime {
    private String templateId;
    private String groupId;
    private String flowId;
    private Integer enabledMark;
}
