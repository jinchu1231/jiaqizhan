package jnpf.model.worklog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class WorkLogCrForm {
    @NotBlank(message = "必填")
    @Schema(description ="标题")
    private String title;
    @NotBlank(message = "必填")
    @Schema(description ="内容")
    private String question;
    @NotBlank(message = "必填")
    @Schema(description ="内容")
    private String todayContent;
    @NotBlank(message = "必填")
    @Schema(description ="内容")
    private String tomorrowContent;
    @NotBlank(message = "必填")
    @Schema(description ="用户id")
    private String toUserId;
}
