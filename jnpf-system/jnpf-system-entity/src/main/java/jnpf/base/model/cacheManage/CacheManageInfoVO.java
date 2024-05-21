package jnpf.base.model.cacheManage;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CacheManageInfoVO {
    @Schema(description ="名称")
    private String name;
    @Schema(description ="值")
    private String value;
}
