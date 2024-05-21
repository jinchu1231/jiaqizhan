package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//@Builder
public class LoginVO {
    @Schema(description ="token")
    private String token;
    @Schema(description ="主题")
    private String theme;
}
