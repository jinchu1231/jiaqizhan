package jnpf.base.model.monitor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SystemModel {
    @Schema(description ="系统")
    private String os;
    @Schema(description ="服务器IP")
    private String ip;
    @Schema(description ="运行时间")
    private String day;
}
