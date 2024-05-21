package jnpf.model.tableexample;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.PaginationTime;
import lombok.Data;

@Data
public class PaginationTableExample extends PaginationTime {
    @Schema(description ="标签")
    private String F_Sign;
}
