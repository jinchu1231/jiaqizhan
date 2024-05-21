package jnpf.permission.model.userrelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class UserRelationIdsVO {
    List<String>  ids;
}
