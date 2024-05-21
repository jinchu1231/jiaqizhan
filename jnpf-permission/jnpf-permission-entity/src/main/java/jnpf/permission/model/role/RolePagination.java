package jnpf.permission.model.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

@Data
public class RolePagination extends Pagination {
    private String organizeId;
    @Schema(description = "状态")
    private Integer enabledMark;
    @Schema(description = "类型")
    private Integer type;
}
