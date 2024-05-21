package jnpf.base.model.module;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MenuListVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="是否有下级菜单")
    private boolean hasChildren;
    @Schema(description ="上级ID")
    private String parentId;
    @Schema(description ="状态")
    private Integer enabledMark;
    @Schema(description ="菜单名称")
    private String fullName;
    @Schema(description =" 图标")
    private String icon;
    @Schema(description ="链接地址")
    private String urlAddress;
    @Schema(description ="菜单类型",example = "1")
    private Integer type;
    @Schema(description ="下级菜单列表")
    private List<MenuListVO> children;
    @Schema(description ="是否按钮权限")
    private Integer isButtonAuthorize;
    @Schema(description ="是否列表权限")
    private Integer isColumnAuthorize;
    @Schema(description ="是否数据权限")
    private Integer isDataAuthorize;
    @Schema(description ="是否表单权限")
    private Integer isFormAuthorize;
    @Schema(description ="排序码")
    private Long sortCode;

    private String systemId;
    @Schema(description = "编码")
    private String enCode;

}
