package jnpf.permission.model.usergroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/3/11 9:28
 */
@Data
public class GroupCrForm {
    /**
     * 名称
     **/
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String fullName;

    /**
     * 编码
     **/
    @Schema(description = "编码")
    @NotBlank(message = "编码不能为空")
    private String enCode;

    /**
     * 说明
     **/
    @Schema(description = "说明")
    private String description;

    /**
     * 类型
     **/
    @Schema(description = "类型")
    @NotBlank(message = "类型不能为空")
    private String type;

    /**
     * 排序
     **/
    @Schema(description = "排序")
    private String sortCode;


    @Schema(description = "状态")
    private Integer enabledMark;
}
