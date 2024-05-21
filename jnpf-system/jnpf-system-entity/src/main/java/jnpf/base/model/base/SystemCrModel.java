package jnpf.base.model.base;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/21 16:00
 */
@Data
public class SystemCrModel implements Serializable {

    @Schema(description = "名称")
    @NotBlank(message = "系统名称不能为空")
    private String fullName;

    @Schema(description = "编码")
    @NotBlank(message = "系统编码不能为空")
    private String enCode;

    @Schema(description = "图标")
    @NotBlank(message = "系统图标不能为空")
    private String icon;

    @NotBlank(message = "排序码")
    private Long sortCode;

    @NotBlank(message = "有效标志")
    private Integer enabledMark;

    @NotBlank(message = "说明")
    private String description;

    @NotBlank(message = "扩展属性")
    private String propertyJson;

    @Schema(description = "导航图片")
    private String navigationIcon;
    @Schema(description = "Logo图片")
    private String workLogoIcon;
    @Schema(description = "已启用工作流")
    private Integer workflowEnabled;
}
