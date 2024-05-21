package jnpf.base.model.dictionarydata;


import jnpf.base.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PageDictionaryData extends Page {
    @Schema(description = "是否树形")
    private String isTree;
}
