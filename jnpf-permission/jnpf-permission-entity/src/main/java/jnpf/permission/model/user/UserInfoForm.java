package jnpf.permission.model.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserInfoForm {
    private String signature;
    private String gender;
    private String nation;
    private String nativePlace;
    private String entryDate;
    private String certificatesType;
    private String certificatesNumber;
    private String education;
    @Schema(description = "生日")
    private Long birthday;
    @Schema(description = "电话")
    private String telePhone;
    private String landline;
    @Schema(description = "手机")
    private String mobilePhone;
    @Schema(description = "邮箱")
    private String email;
    private String urgentContacts;
    private String urgentTelePhone;
    private String PostalAddress;
    @Schema(description = "真实姓名")
    private String realName;
}
