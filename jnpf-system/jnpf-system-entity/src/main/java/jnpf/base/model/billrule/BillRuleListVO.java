package jnpf.base.model.billrule;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BillRuleListVO {
    @Schema(description ="id")
    private String id;
    @Schema(description ="业务名称")
    private String fullName;
    @Schema(description ="业务编码")
    private Integer digit;
    @Schema(description ="流水位数")
    private String enCode;
    @Schema(description ="流水起始")
    private String startNumber;
    @Schema(description ="当前流水号")
    private String outputNumber;
    @Schema(description ="状态(0-禁用，1-启用)")
    private Integer enabledMark;
    @Schema(description ="排序码")
    private long sortCode;
    @Schema(description ="创建时间")
    private Long creatorTime;
    @Schema(description ="创建用户")
    private String creatorUser;
    @Schema(description ="修改时间")
    private Long lastModifyTime;
    @Schema(description ="业务分类")
    private String category;
}
