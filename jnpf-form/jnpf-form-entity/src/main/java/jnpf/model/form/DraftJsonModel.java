package jnpf.model.form;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="流程表单草稿模型")
public class DraftJsonModel {
    @Schema(description = "是否必填")
    private Boolean required;
    @Schema(description = "字段id")
    private String filedId;
    @Schema(description = "字段名称")
    private String filedName;
    @Schema(description = "jnpfkey")
    private String jnpfKey;
    @Schema(description = "是否多选")
    private boolean multiple;
}
