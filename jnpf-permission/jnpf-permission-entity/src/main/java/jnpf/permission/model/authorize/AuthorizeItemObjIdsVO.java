package jnpf.permission.model.authorize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AuthorizeItemObjIdsVO {
   @Schema(description = "id集合")
   private List<String> ids;
}
