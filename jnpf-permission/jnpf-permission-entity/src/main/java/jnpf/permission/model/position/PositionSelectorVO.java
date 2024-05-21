package jnpf.permission.model.position;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PositionSelectorVO {
    private String id;
    @Schema(description ="父级ID")
    private String  parentId;
    @Schema(description ="名称")
    private String  fullName;
    @Schema(description ="是否有下级菜单")
    private boolean hasChildren = true;
    @Schema(description ="状态")
    private Integer enabledMark;
    @Schema(description ="下级菜单列表")
    private List<PositionSelectorVO> children = new ArrayList<>();
    @JSONField(name="category")
    private String  type;
    @Schema(description ="图标")
    private String icon;


    private String organize;
    @Schema(description ="组织id树")
    private List<String> organizeIds;

    @Schema(description = "组织id树")
    @JsonIgnore
    private String organizeIdTree;

    @Schema(description ="前端解析唯一标识")
    private String onlyId;
}
