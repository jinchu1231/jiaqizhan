package jnpf.model.tableexample;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 更新标签
 */
@Data
public class TableExampleSignUpForm {
    @NotBlank(message = "必填")
    @Schema(description ="项目标记")
    private String sign;
}
