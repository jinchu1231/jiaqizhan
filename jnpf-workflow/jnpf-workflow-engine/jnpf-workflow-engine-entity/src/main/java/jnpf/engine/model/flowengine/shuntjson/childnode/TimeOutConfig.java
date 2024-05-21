package jnpf.engine.model.flowengine.shuntjson.childnode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 解析引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class TimeOutConfig {
    /**
     * 开关
     **/
    @Schema(description = "开关")
    private Boolean on = false;
    /**
     * 数量
     **/
    @Schema(description = "数量")
    private Integer quantity;
    /**
     * 类型 day、 hour、 minute
     **/
    @Schema(description = "类型")
    private String type;
    /**
     * 同意1 拒绝2
     **/
    @Schema(description = "类型")
    private Integer handler;
}
