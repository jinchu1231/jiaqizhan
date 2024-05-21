package jnpf.model.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Page;
import lombok.Data;

@Data
public class PageDocument extends Page {
    @Schema(description ="父级主键")
    private String parentId;
}
