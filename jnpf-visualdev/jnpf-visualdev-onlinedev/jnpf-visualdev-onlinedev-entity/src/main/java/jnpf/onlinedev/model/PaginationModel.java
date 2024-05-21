package jnpf.onlinedev.model;


import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
@Schema(description="查询条件模型")
public class PaginationModel extends Pagination {
    @Schema(description = "查询条件json")
    private String queryJson;
    @Schema(description = "菜单id")
    private String menuId;
    @Schema(description = "关联字段")
    private String relationField;
    @Schema(description = "字段对象")
    private String columnOptions;
    @Schema(description = "数据类型")
    private String dataType;
    @Schema(description = "高级查询条件json")
    private String superQueryJson;
    @Schema(description = "异步查询父id")
    private String parentId;
}
