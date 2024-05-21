package jnpf.base.model.printdev.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 打印模板-数据传输对象
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Data
public class PrintDevFormDTO {

    @Schema(description = "主键_id")
    private String id;

    @NotBlank(message = "必填")
    @Schema(description = "名称")
    private String fullName;

    @NotBlank(message = "必填")
    @Schema(description = "编码",required = true)
    private String enCode;

    @NotBlank(message = "必填")
    @Schema(description = "分类")
    private String category;

    @NotNull(message = "必填")
    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "描述")
    private String description;

    @NotNull(message = "必填")
    @Schema(description = "排序码")
    private Long sortCode;

    @NotNull(message = "必填")
    @Schema(description = "有效标志")
    private Integer enabledMark;

    @NotBlank(message = "必填")
    @Schema(description = "连接数据_id")
    private String dbLinkId;

    @Schema(description = "sql语句")
    private String sqlTemplate;

    @Schema(description = "左侧字段")
    private String leftFields;

    @Schema(description = "打印模板")
    private String printTemplate;

    @Schema(description = "纸张参数")
    private String pageParam;
}
