package jnpf.base.model.portalManage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortalManageSelectModel {
    private List<String> systemIds;
    private List<String> collect;
}
