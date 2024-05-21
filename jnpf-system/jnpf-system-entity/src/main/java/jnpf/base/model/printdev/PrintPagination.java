package jnpf.base.model.printdev;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-11-20
 */
@Data
public class PrintPagination extends Pagination {
    private String category;
    @Schema(description = "状态")
    private Integer enabledMark;
}
