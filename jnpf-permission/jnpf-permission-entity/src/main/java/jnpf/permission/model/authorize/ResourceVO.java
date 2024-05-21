package jnpf.permission.model.authorize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源
 */
@Data
public class ResourceVO {
    @Schema(description ="资源主键")
    private String id;
    @Schema(description ="资源名称")
    private String fullName;
    @Schema(description ="资源编码")
    private String enCode;
    @Schema(description ="条件规则")
    private String conditionJson;
    @Schema(description ="规则描述")
    private String conditionText;
    @Schema(description ="功能主键")
    private String moduleId;
}
