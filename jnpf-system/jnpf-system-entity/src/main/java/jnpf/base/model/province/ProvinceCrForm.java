package jnpf.base.model.province;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 新建
 *
 */

@Data
public class ProvinceCrForm {
    @Schema(description = "编码")
    @NotBlank(message = "必填")
    private String enCode;

    @Schema(description = "有效标志")
    private Integer enabledMark;

    @Schema(description = "名称")
    @NotBlank(message = "必填")
    private String fullName;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "上级id")
    @NotBlank(message = "必填")
    private String parentId;

    @Schema(description = "分类")
    private String type;

    @Schema(description = "排序码")
    private long sortCode;
}
