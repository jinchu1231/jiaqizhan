package jnpf.permission.model.authorize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DataValuesQuery {

    @Schema(description = "类型")
    private String type;
    @Schema(description = "菜单id集合")
    private String moduleIds;
    @Schema(description = "分类")
    private String category;
}
