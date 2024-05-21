package jnpf.engine.model.flowengine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "分页模型")
public class FlowPagination extends Pagination {

    @Schema(description = "分类")
    private String category;
    @Schema(description = "类型")
    private Integer flowType;
    @Schema(description = "标志")
    private Integer enabledMark;
    @Schema(description = "类型")
    private Integer type;
    @JsonIgnore
    private List<String> templateIdList = new ArrayList<>();
}
