package jnpf.engine.model.flowtasknode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskNodeListModel {
    private String id;
    private Integer state;
    private Integer completion;
    private String notNodeCode;
}
