package jnpf.base.model.datainterface;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class FieldModel implements Serializable {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "参数名称")
    private String field;

    @Schema(description = "默认值")
    private String defaultValue;
}
