package jnpf.base.model.dbbackup;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DbBackupListVO {

    @Schema(description ="备份主键")
    private String id;

    @Schema(description ="文件名称")
    private String fileName;

    @Schema(description ="文件大小")
    private String fileSize;

    @Schema(description ="创建时间",example = "1")
    private long creatorTime;

    @Schema(description ="文件访问地址")
    private String fileUrl;

}
