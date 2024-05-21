package jnpf.base.model.column;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 列表
 */
@Data
public class ColumnListVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="列表名称")
    private String fullName;
    @Schema(description ="编码")
    private String enCode;
    @Schema(description ="表名")
    private String bindTable;
    @Schema(description ="是否启用")
    private Integer enabledMark;
    @Schema(description ="排序码")
    private Long sortCode;
}
