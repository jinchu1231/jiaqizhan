package jnpf.permission.model.user;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

@Data
public class UserSelectorModel extends SumTree {
    @JSONField(name="category")
    private String type;
    private String fullName;
    @Schema(description ="状态")
    private Integer enabledMark;
    @Schema(description ="图标")
    private String icon;
}
