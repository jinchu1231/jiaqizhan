package jnpf.model.document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DocumentTrashListVO {
    @Schema(description ="主键id")
    private String id;
    @Schema(description ="文件夹名称")
    private String fullName;
    @Schema(description ="删除日期")
    private String deleteTime;
    @Schema(description ="大小")
    private String fileSize;
}
