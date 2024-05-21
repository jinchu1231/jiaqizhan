package jnpf.base.model.button;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ButtonListVO {
    @Schema(description = "排序码")
    private Long sortCode;
    @Schema(description = "主键")
    private String  id;
    @Schema(description = "父级id")
    private String parentId;
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "编码")
    private String enCode;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "有效标志")
    private Integer enabledMark;
    @Schema(description = "说明")
    private String description;
}
