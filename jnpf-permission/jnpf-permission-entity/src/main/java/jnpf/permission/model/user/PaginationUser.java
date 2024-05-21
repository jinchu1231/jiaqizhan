package jnpf.permission.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PaginationUser extends Pagination {

    @Schema(description = "组织id")
    private String organizeId;

    @Schema(description = "角色id")
    private String roleId;
    @Schema(description = "状态")
    private Integer enabledMark;
    @Schema(description = "性别")
    private String gender;

}
