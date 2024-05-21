package jnpf.permission.model.userrelation;

import com.alibaba.fastjson.annotation.JSONField;
import jnpf.util.treeutil.SumTree;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserRelationTreeModel extends SumTree {

    @Schema(description ="主键")
    private String id;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="是否有子节点")
    private Boolean hasChildren;
//    @Schema(description ="子节点")
//    private List<UserRelationTreeModel> children = new ArrayList<>();
    @JSONField(name="category")
    private String type;
}
