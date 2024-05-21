package jnpf.base.model.monitor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MonitorListVO {
    @Schema(description ="系统信息")
    private SystemModel system;
    @Schema(description ="CPU信息")
    private CpuModel cpu;
    @Schema(description ="内存信息")
    private MemoryModel memory;
    @Schema(description ="硬盘信息")
    private DiskModel disk;
    @Schema(description ="当前时间")
    private long time;
}
