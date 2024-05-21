package jnpf.base.model.province;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProvinceSelectListVO {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String fullName;

//    private String icon;

    @Schema(description = "是否显示下级可点击按钮")
    private Boolean isLeaf;
}
