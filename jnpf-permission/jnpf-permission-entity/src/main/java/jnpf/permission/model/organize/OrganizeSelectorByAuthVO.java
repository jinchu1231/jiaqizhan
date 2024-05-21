package jnpf.permission.model.organize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 组织树模型
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/28 9:10
 */
@Data
public class OrganizeSelectorByAuthVO extends OrganizeSelectorVO implements Serializable {

    @Schema(description = "是否可选")
    private Boolean disabled = false;

}
