package jnpf.base.model.cacheManage;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CacheManageListVO {
    @Schema(description ="名称")
    private String name;
    @Schema(description ="过期时间",example = "1")
    private long overdueTime;
    @Schema(description ="大小")
    private Integer cacheSize;
}
