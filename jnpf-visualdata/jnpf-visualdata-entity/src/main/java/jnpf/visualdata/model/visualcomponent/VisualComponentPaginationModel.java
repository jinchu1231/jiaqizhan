package jnpf.visualdata.model.visualcomponent;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.visualdata.model.VisualPagination;
import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司
 * @date 2023年7月7日
 */
@Data
public class VisualComponentPaginationModel extends VisualPagination {
    @Schema(description = "组件类型(0,1)")
    private Integer type;

}
