package jnpf.permission.model.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.permission.model.permission.PermissionVoBase;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class RoleInfoVO extends PermissionVoBase {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="编码")
    private String enCode;
    @Schema(description ="组织id数组树")
    private List<LinkedList<String>> organizeIdsTree;
    @Schema(description ="全局标识")
    private Integer globalMark;
    @Schema(description ="类型")
    private String type;
    @Schema(description ="状态")
    private Integer enabledMark;
    @Schema(description ="备注")
    private String description;
    @Schema(description ="排序")
    private Long sortCode;


}
