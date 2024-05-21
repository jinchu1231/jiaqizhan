package jnpf.base.model.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemApiModel implements Serializable {
    private int mark;
    private String mainSystemCode;
    private List<String> moduleAuthorize;
}
