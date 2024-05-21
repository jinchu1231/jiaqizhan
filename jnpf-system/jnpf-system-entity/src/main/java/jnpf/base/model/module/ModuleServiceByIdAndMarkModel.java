package jnpf.base.model.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleServiceByIdAndMarkModel implements Serializable {
    private int mark;
    private String id;
    private List<String> moduleAuthorize;
    private List<String> moduleUrlAddressAuthorize;
}
