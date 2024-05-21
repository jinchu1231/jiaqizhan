package jnpf.permission.model.organize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class OrganizeByAuthModel extends OrganizeModel {

    @Schema(description = "是否可选")
    private Boolean disabled = false;

}
