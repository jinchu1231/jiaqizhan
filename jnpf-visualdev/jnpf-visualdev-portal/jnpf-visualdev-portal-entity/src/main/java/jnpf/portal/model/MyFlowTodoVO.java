package jnpf.portal.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MyFlowTodoVO {
    private String id;
    private Integer enabledMark;
    private Long startTime;
    private Long endTime;
    private String content;
}
