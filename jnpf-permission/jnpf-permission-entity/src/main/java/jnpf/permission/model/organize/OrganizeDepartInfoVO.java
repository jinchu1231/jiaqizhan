package jnpf.permission.model.organize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class OrganizeDepartInfoVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="父主键")
    private String parentId;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="编码")
    private String enCode;
    @Schema(description ="状态")
    private int enabledMark;
    @Schema(description ="备注")
    private String description;
    @Schema(description ="主管id")
    private String managerId;
    @Schema(description ="排序码")
    private long sortCode;

    @Schema(description ="组织id树")
    private List<String> organizeIdTree;
}
