package jnpf.base.model.portalManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.entity.PortalManageEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotNull;

/**
 * 门户管理表单更新对象
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.6
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-02-18
 */
@Data
public class PortalManageUpForm {

    @Schema(description = "主键_id")
    @NotNull(message = "必填")
    private String id;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "门户_id")
    @NotNull(message = "必填")
    private String portalId;

    @Schema(description = "默认首页")
    private Integer homePageMark;

    @Schema(description = "排序码")
    @NotNull(message = "必填")
    private Long sortCode;

    @Schema(description = "有效标志")
    @NotNull(message = "必填")
    private Integer enabledMark;

    @Schema(description = "平台")
    @NotNull(message = "必填")
    private String platform;

    public PortalManageEntity convertEntity(){
        PortalManageEntity portalManageEntity = new PortalManageEntity();
        BeanUtils.copyProperties(this, portalManageEntity);
        portalManageEntity.setPlatform(portalManageEntity.getPlatform());
        return portalManageEntity;
    }

}
