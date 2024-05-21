package jnpf.base.model.base;

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
public class SystemServiceByIdsModel implements Serializable {
    private List<String> ids = new ArrayList<>();
    private List<String> moduleAuthorize;

    public <R> SystemServiceByIdsModel(R collect) {
    }
}
