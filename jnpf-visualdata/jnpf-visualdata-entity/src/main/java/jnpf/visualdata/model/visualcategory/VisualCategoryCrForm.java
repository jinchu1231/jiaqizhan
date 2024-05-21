package jnpf.visualdata.model.visualcategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
public class VisualCategoryCrForm {
    @Schema(description ="分类键值")
    private String categoryKey;
    @Schema(description ="分类名称")
    private String categoryValue;
    @Schema(description ="是否已删除")
    private Integer isDeleted;
}
