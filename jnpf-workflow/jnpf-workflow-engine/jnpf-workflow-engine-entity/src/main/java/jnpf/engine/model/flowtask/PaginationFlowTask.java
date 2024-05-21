package jnpf.engine.model.flowtask;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.PaginationTime;
import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class PaginationFlowTask extends PaginationTime {
    /**
     * 流程模板id
     **/
    @Schema(description ="流程模板主键")
    private String templateId;
    @Schema(description ="流程主键")
    private String flowId;
    /**
     * 所属分类
     **/
    @Schema(description ="所属分类")
    private String flowCategory;
    @Schema(description ="创建用户主键")
    private String creatorUserId;
    @Schema(description ="状态")
    private Integer status;
    @Schema(description ="批量审批",hidden = true)
    @JsonIgnore
    private Integer isBatch;
    @Schema(description ="编码")
    private String nodeCode;
    @Schema(description ="紧急程度")
    private Integer flowUrgent;
    @Schema(description ="是否委托",hidden = true)
    @JsonIgnore
    private Boolean delegateType = false;
    @Schema(description ="用户主键",hidden = true)
    @JsonIgnore
    private String userId;
    @Schema(description ="是否分页",hidden = true)
    @JsonIgnore
    private Boolean isPage = true;
    @Schema(description ="是否定时",hidden = true)
    @JsonIgnore
    private Boolean isTime = true;
}
