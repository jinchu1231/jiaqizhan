package jnpf.permission.model.organize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class OrganizeCrForm {

    @Schema(description = "公司上级")
    @NotBlank(message = "公司上级不能为空")
    private String parentId;
    @Schema(description = "公司名称")
    @NotBlank(message = "公司名称不能为空")
    private String fullName;
    @Schema(description = "公司编码")
    @NotBlank(message = "公司编码不能为空")
    private String enCode;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "公司状态")
    @NotNull(message = "公司状态不能为空")
    private Integer enabledMark;
    @Schema(description = "扩展属性")
    private OrganizeCrModel propertyJson;
    @Schema(description ="排序")
    private Long sortCode;
}
