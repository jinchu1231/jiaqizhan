package jnpf.permission.model.organize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrganizeDepartListVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="父主键")
    private String parentId;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="编码")
    private String enCode;
    @Schema(description ="备注")
    private String description;
    @Schema(description ="创建时间")
    private Long creatorTime;
    @Schema(description ="部门经理")
    private String manager;
    @Schema(description ="状态")
    private int enabledMark;
    @Schema(description ="是否有下级菜单")
    private boolean hasChildren = true;
    @Schema(description ="下级菜单列表")
    private List<OrganizeDepartListVO> children = new ArrayList<>();
    @Schema(description ="排序")
    private long sortCode;
}
