package jnpf.message.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MessageRecordForm {
    @Schema(description = "id集合")
    private String ids;
}
