package jnpf.base.model.province;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2023/3/17 14:09:11
 */
@Schema(description="省市区下拉参数模型")
@Data
public class ProvinceSelectModel {
    @Schema(description = "父级id")
    @NotBlank(message = "必填")
    private String pid;
    @Schema(description = "选中id列表/查询子集时不传值")
    private List<List<String>> ids;
}
