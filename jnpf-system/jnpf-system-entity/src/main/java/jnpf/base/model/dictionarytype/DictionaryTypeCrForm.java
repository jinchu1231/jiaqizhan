package jnpf.base.model.dictionarytype;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class DictionaryTypeCrForm {
    @Schema(description = "父级主键")
    @NotBlank(message = "必填")
    private String parentId;
    @Schema(description = "名称")
    @NotBlank(message = "必填")
    private String fullName;
    @Schema(description = "编码")
    @NotBlank(message = "必填")
    private String enCode;
    @Schema(description = "是否树形")
    @NotNull(message = "必填")
    private Integer isTree;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "排序码")
    private long sortCode;
    @Schema(description = "类型")
    private Integer category;
}
