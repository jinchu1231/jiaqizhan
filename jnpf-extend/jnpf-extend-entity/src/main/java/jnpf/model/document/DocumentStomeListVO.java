package jnpf.model.document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DocumentStomeListVO {
    @Schema(description ="共享日期")
    private long shareTime;
    @Schema(description ="文件json")
    private String fileExtension;
    @Schema(description ="大小")
    private String fileSize;
    @Schema(description ="名称")
    private String fullName;
    @Schema(description ="主键")
    private String id;
    @Schema(description ="创建用户")
    private String creatorUserId;
}
