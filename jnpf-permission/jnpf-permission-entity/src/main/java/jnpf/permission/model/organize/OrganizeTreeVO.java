package jnpf.permission.model.organize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrganizeTreeVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="父主键")
    private String parentId;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="图标")
    private String icon;
    @Schema(description ="是否有下级菜单")
    private boolean hasChildren = true;
    @Schema(description ="下级菜单列表")
    private List<OrganizeTreeVO> children = new ArrayList<>();
}
