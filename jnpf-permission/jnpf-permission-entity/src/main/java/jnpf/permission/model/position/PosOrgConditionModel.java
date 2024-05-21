package jnpf.permission.model.position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PosOrgConditionModel extends PosOrgModel {

    private String organizeIdTree;

    private String organizeId;

    @Schema(description ="前端解析唯一标识")
    private String onlyId;

}
