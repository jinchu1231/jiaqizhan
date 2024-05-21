package jnpf.base.model.dictionarydata;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryDataAllVO {
    private String  id;
    private String  fullName;
    private String  enCode;
    private String parentId;
    private List<DictionaryDataAllVO> children;
}
