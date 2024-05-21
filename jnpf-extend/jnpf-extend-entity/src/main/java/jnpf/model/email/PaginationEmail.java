package jnpf.model.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.PaginationTime;
import lombok.Data;

@Data
public class PaginationEmail extends PaginationTime {
    @Schema(description ="类型")
    private String type;
}
