package jnpf.engine.model.flowengine.shuntjson.childnode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/3/31 14:53
 */
@Data
public class FuncConfig {

    @Schema(description = "类型")
    private Boolean on = false;
    @Schema(description = "消息主键")
    private String msgId;
    @Schema(description = "接口主键")
    private String interfaceId;
    @Schema(description = "名称")
    private String msgName;
    @Schema(description = "数据")
    private List<TemplateJsonModel> templateJson = new ArrayList<>();
}
