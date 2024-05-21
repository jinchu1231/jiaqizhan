package jnpf.base.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * json格式化对象（在线开发对象）
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 */
@Data
public class VisualDevListVO {
    @Schema(description = "主键" )
    private String id;
    @Schema(description = "名称" )
    private String fullName;
    @Schema(description = "编码" )
    private String enCode;
    @Schema(description = "是否启用流程" )
    private Integer enableFlow;
}
