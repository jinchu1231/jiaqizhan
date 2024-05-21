package jnpf.base.model.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemApiListModel implements Serializable {
    private String keyword;
    private Boolean filterEnableMark;
    private Boolean verifyAuth = false;
    private Boolean filterMain;
    private Boolean isList = false;
    private List<String> moduleAuthorize;
}
