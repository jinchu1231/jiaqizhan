package jnpf.permission.model.organize;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Page;
import lombok.Data;

@Data
public class PaginationOrganize extends Page {

    @Schema(description = "状态")
    private Integer enabledMark;

    private String type;

}
