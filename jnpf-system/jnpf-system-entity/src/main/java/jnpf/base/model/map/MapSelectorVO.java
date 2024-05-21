package jnpf.base.model.map;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MapSelectorVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="地图名称")
    private String fullName;
}
