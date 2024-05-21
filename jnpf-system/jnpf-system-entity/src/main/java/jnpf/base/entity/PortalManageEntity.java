package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 门户管理
 * </p>
 *
 * @author YanYu
 * @since 2023-02-16
 */
@TableName("base_portal_manage")
@Schema(description = "PortalManage对象")
@Data
public class PortalManageEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    @Schema(description = "门户_id")
    @TableField("F_PORTAL_ID")
    private String portalId;

    @Schema(description = "系统_id")
    @TableField("F_SYSTEM_ID")
    private String systemId;

    @Schema(description = "平台")
    @NotNull(message = "必填")
    @TableField("F_PLATFORM")
    private String platform;

}
