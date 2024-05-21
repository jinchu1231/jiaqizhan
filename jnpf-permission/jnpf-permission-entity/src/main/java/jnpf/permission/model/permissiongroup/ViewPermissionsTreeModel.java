package jnpf.permission.model.permissiongroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.io.Serializable;

@Data
public class ViewPermissionsTreeModel extends SumTree implements Serializable {
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "图标")
    private String icon;
    private String moduleId;
    private Long creatorTime;
    private Long sortCode;
}
