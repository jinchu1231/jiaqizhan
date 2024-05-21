package jnpf.permission.model.organize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OrganizeDepartCrForm {

    @Schema(description ="主管")
    private String managerId;
    @NotBlank(message = "必填")
    @Schema(description ="上级ID")
    private String parentId;
    @NotBlank(message = "必填")
    @Schema(description ="部门名称")
    private String fullName;
    @NotBlank(message = "必填")
    @Schema(description ="部门编码")
    private String enCode;
    @Schema(description ="状态")
    private int enabledMark;
    @Schema(description ="描述")
    private String description;
    @Schema(description ="排序")
    private Long sortCode;
}
