package jnpf.permission.model.position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 通过组织id获取岗位列表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-12-21
 */
@Data
public class PositionVo implements Serializable {
    private String id;

    @Schema(description ="名称")
    private String  fullName;
}
