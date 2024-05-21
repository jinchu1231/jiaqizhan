package jnpf.permission.model.position;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

@Data
public class PaginationPosition extends Pagination {
   @Schema(description = "组织id")
   private String organizeId;
   @Schema(description = "状态")
   private Integer enabledMark;
   @Schema(description = "类型")
   private String type;
   @JsonIgnore
   private String enCode;
}
