package jnpf.base.model.module;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ModuleInfoVO extends ModuleCrForm{
    @Schema(description ="主键")
    private String id;
}
