package jnpf.permission.model.position;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.permission.model.permission.PermissionVoBase;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PositionInfoVO extends PermissionVoBase {
    @Schema(description ="id")
    private String id;
    @Schema(description ="上级id")
    private String organizeId;
    @Schema(description ="岗位名称")
    private String fullName;
    @Schema(description ="岗位编码")
    private String enCode;
    @Schema(description ="岗位类型")
    private String type;
    @Schema(description ="岗位状态")
    private Integer enabledMark;
    @Schema(description ="岗位说明")
    private String description;
    @Schema(description ="排序")
    private Long sortCode;

    @Schema(description ="组织id树")
    private List<String> organizeIdTree;

}
