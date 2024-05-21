package jnpf.model.form;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/10/19 14:22:18
 */
@Data
@Accessors(chain = true)
@Schema(description="表单草稿存储对象模型")
public class FormDraftJsonModel {
    @Schema(description = "草稿json")
    private String draftJson;
    @Schema(description = "表json")
    private String tableJson;
}
