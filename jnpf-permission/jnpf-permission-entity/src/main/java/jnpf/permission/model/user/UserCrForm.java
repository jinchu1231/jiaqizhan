package jnpf.permission.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserCrForm {
    @NotBlank(message = "必填")
    @Schema(description = "账户")
    private String account;
    @NotBlank(message = "必填")
    @Schema(description = "户名")
    private String realName;
    @NotBlank(message = "必填")
    @Schema(description = "部门")
    private String organizeId;
    @Schema(description = "主管")
    private String managerId;
    @Schema(description = "岗位")
    private String positionId;
    @Schema(description = "角色")
    private String roleId;
    @Schema(description = "说明")
    private String description;
    @NotNull(message = "性别不能为空")
    @Schema(description = "性别")
    private int gender;
    private String nation;
    private String nativePlace;
    private String certificatesType;
    private String certificatesNumber;
    private String education;
    private String birthday;
    private String telePhone;
    private String landline;
    private String mobilePhone;
    private String email;
    private String urgentContacts;
    private String urgentTelePhone;
    private String postalAddress;
    private String headIcon;
    @Schema(description ="排序")
    private Long sortCode;
    private long entryDate;
    @Schema(description ="状态")
    private Integer enabledMark;

    @Schema(description ="分组id")
    private String groupId;

    @Schema(description = "职级")
    private String ranks;
}
