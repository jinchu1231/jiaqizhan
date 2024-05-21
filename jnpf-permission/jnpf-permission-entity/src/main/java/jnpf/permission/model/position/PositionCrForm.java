package jnpf.permission.model.position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class PositionCrForm {
    @NotBlank(message = "必填")
    @Schema(description = "岗位编码")
    private String enCode;
    @NotBlank(message = "必填")
    @Schema(description = "所属部门(id)")
    private String organizeId;
    @NotNull(message = "必填")
    @Schema(description = "岗位状态")
    private Integer enabledMark;
    @NotBlank(message = "必填")
    @Schema(description = "岗位名称")
    private String fullName;

    private String description;
    @NotNull(message = "必填")
    @Schema(description = "岗位类型(id)")
    private Integer type;
    @Schema(description ="排序")
    private Long sortCode;
}
