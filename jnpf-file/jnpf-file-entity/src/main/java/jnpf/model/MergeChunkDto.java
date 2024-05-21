package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @date 2020/6/10 20:39
 */
@Data
public class MergeChunkDto implements Serializable {

    @Schema(description = "名称")
    private String fileName;

    @Schema(description = "分片")
    private String identifier;

    @Schema(description = "文件大小")
    private Long filesize;

    @Schema(description = "扩展")
    private String extension;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "父级id")
    private String parentId;

    /**
     * 文件上传路径类型
     */
    @Schema(description = "文件上传路径类型")
    private String pathType;

    /**
     * 是否分用户存储 1：是 0：否
     */
    @Schema(description = "是否分用户存储")
    private String isAccount;

    /**
     * 文件路径，子级文件用“/”隔开，如：文件1/文件1-1
     */
    @Schema(description = "文件路径")
    private String folder;
}
