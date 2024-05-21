package jnpf.permission.model.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * IM获取用户接口
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Data
public class ImUserListVo {
    @Schema(description ="主键")
    private String id;
    @Schema(description ="名称")
    private String realName;
    @Schema(description ="用户头像")
    private String headIcon;
    @Schema(description ="部门")
    private String department;
    @Schema(description ="账号")
    private String account;
}
