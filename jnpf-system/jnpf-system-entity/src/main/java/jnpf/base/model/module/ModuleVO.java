package jnpf.base.model.module;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 功能
 */
@Data
public class ModuleVO {
    @Schema(description ="功能主键")
    private String id;
    @Schema(description ="功能上级")
    private String parentId;
    @Schema(description ="功能类别【1-类别、2-页面】")
    private int type;
    @Schema(description ="功能名称")
    private String fullName;
    @Schema(description ="功能编码")
    private String enCode;
    @Schema(description ="功能地址")
    private String urlAddress;
}
