package jnpf.base.model.dictionarydata;


import jnpf.util.treeutil.SumTree;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictionaryDataModel extends SumTree {
    @Schema(description = "主键")
    private String  id;
    @Schema(description = "父级主键")
    private String parentId;
    @Schema(description = "名称")
    private String  fullName;
    @Schema(description = "编码")
    private String  enCode;
    @Schema(description = "有效标志")
    private Integer  enabledMark;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "排序码")
    private long sortCode;
}
