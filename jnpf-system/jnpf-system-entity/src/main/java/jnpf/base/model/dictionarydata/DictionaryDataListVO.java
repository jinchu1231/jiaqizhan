package jnpf.base.model.dictionarydata;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
public class DictionaryDataListVO {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "编码")
    private String enCode;
    @Schema(description = "有效标志")
    private Integer enabledMark;
    @Schema(description = "是否有子集")
    private Boolean hasChildren;
    @Schema(description = "父级主键")
    private String parentId;
    @Schema(description = "子集集合")
    private List<DictionaryDataListVO> children;
    @Schema(description = "排序码")
    private long sortCode;

}
