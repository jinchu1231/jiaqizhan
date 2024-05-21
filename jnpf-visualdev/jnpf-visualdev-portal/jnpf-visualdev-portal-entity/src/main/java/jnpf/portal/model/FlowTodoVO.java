package jnpf.portal.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class FlowTodoVO {
    public String id;

    public String fullName;

    public String enCode;

    public String flowId;

    public Integer formType;

    public Integer status;

    public String processId;

    public String taskNodeId;

    public String taskOperatorId;

    public Long creatorTime;

    public Integer type;

}
