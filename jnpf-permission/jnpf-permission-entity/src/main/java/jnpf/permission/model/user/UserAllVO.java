package jnpf.permission.model.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserAllVO {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="账号")
    private String account;
    @Schema(description ="名称")
    private String realName;
    @Schema(description ="用户头像")
    private String headIcon;
    /**
     * //1,男。2女
     */
    @Schema(description ="性别")
    private String gender;
    //    @Schema(description ="部门")
//    private String department;
    @Schema(description ="快速搜索")
    private String quickQuery;
}
