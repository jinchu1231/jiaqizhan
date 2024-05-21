package jnpf.base.model.printdev.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页列表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-11-20
 */
@Data
public class PrintDevListVO {

    @Schema(description ="主键_id")
    private String id;

    @Schema(description ="名称")
    private String fullName;

    @Schema(description ="编码",required = true)
    private String enCode;

    @Schema(description ="有效标志")
    private Integer enabledMark;

    @Schema(description ="创建用户_id")
    private String creatorUser;

    @Schema(description ="创建时间")
    private Long creatorTime;

    @Schema(description ="修改用户_id")
    private String lastModifyUser;

    @Schema(description ="修改时间")
    private Long lastModifyTime;

    @Schema(description ="排序码")
    private Long sortCode;

    @Schema(description ="分类")
    private String category;
}
