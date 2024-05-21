package jnpf.base.model.dictionarydata;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictionaryDataInfoVO {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "父级主键")
    private String parentId;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "编码")
    private String enCode;
    @Schema(description = "有效标志")
    private Integer enabledMark;
    @Schema(description = "分类id")
    private String dictionaryTypeId;
    @Schema(description = "排序码")
    private long sortCode;
}
