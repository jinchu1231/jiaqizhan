package jnpf.base.model.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleServiceByIdsModel implements Serializable {
    private List<String> ids = new ArrayList<>();
    private List<String> moduleAuthorize;
    private List<String> moduleUrlAddressAuthorize;
    private Boolean singletonOrg = false;

    public <R> ModuleServiceByIdsModel(R collect) {
    }
}
