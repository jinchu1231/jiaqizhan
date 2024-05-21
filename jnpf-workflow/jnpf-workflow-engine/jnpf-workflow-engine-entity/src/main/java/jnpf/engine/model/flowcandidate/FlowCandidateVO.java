package jnpf.engine.model.flowcandidate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class FlowCandidateVO {

    @Schema(description = "节点")
    private List<FlowCandidateListModel> list;
    /**
     * 1.有分支 //2.没有分支有候选人 //3.没有分支也没有候选人
     */
    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "会签是否完成")
    private Boolean countersignOver = true;
}
