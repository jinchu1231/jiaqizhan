package jnpf.model.document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class DocumentUploader implements Serializable {
    @Schema(description ="父级id")
    private String parentId;
    @Schema(description ="文件")
    private MultipartFile file;
}
