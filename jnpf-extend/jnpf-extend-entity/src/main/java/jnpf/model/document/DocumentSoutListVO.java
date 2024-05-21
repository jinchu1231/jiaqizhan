package jnpf.model.document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DocumentSoutListVO {
    @Schema(description ="主键id")
    private String id;
    @Schema(description ="文件夹名称")
    private String fullName;
    @Schema(description ="文档分类(0-文件夹，1-文件)")
    private Integer type;
    @Schema(description ="共享日期")
    private String shareTime;
    @Schema(description ="大小")
    private String fileSize;

}
