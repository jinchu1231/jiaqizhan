package jnpf.portal.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @author 管理员/admin
 * @date 2020-10-21 14:23:30
 */
@Data
public class PortalInfoVO extends PortalCrForm {

    private String id;

    @Schema(description = "pc发布标识")
    Integer pcIsRelease;
    @Schema(description = "app发布标识")
    Integer appIsRelease;

    @Schema(description = "pc是否发布门户" )
    private Integer pcPortalIsRelease;
    @Schema(description = "app是否发布门户" )
    private Integer appPortalIsRelease;

}
