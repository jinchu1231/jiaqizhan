package jnpf.permission.model.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class UserThemeForm {
    @NotBlank(message = "必填")
    @Schema(description ="系统主题")
    private String theme;
}
