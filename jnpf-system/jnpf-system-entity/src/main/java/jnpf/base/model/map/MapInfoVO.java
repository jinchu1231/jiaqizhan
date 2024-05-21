package jnpf.base.model.map;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MapInfoVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="地图名称")
    private String fullName;
    @Schema(description ="地图编码")
    private String enCode;
    @Schema(description ="地图数据")
    private String data;
    @Schema(description ="排序")
    private long sortCode;
    @Schema(description ="状态")
    private Integer enabledMark;
}
