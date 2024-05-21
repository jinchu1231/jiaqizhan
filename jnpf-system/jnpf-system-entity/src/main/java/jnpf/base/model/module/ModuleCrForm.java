package jnpf.base.model.module;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ModuleCrForm {
    @Schema(description ="父主键")
    private String parentId;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="是否按钮权限")
    private Integer isButtonAuthorize;
    @Schema(description ="是否列表权限")
    private Integer isColumnAuthorize;
    @Schema(description ="是否数据权限")
    private Integer isDataAuthorize;
    @Schema(description ="是否表单权限")
    private Integer isFormAuthorize;
    @Schema(description ="编码")
    private String enCode;
    @Schema(description ="图标")
    private String icon;
    @Schema(description ="分类")
    private Integer type;
    @Schema(description ="URL地址")
    private String urlAddress;
    @Schema(description ="外链")
    private String linkTarget;
    @Schema(description ="分类")
    private String category;
    @Schema(description ="说明")
    private String description;
    @Schema(description ="有效标志")
    private Integer enabledMark;
    @Schema(description ="排序码")
    private long sortCode;
    @Schema(description ="扩展json")
    private String propertyJson;

    @Schema(description ="系统id")
    private String systemId;
}
