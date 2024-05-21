package jnpf.base.model.monitor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DiskModel {
    @Schema(description ="硬盘总容量")
    private String total;
    @Schema(description ="空闲硬盘")
    private String available;
    @Schema(description ="已使用")
    private String used;
    @Schema(description ="已使用百分比")
    private String usageRate;
}
