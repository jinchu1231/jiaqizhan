package jnpf.base.model.dictionarytype;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictionaryTypeInfoVO {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "父级主键")
    private String parentId;
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "编码")
    private String enCode;
    @Schema(description = "是否树形")
    private Integer isTree;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "排序码")
    private long sortCode;
    @Schema(description = "类型")
    private Integer category;
}
