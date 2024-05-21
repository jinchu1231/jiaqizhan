package jnpf.permission.model.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class WorkHandoverModel implements Serializable {
    @NotNull(message = "工作移交人不能为空")
    private String fromId;
    @NotNull(message = "工作交接人不能为空")
    private String toId;
    private List<String> waitList = new ArrayList<>();
    private List<String> chargeList  = new ArrayList<>();
    private List<String> flowList  = new ArrayList<>();
    private List<String> circulateList  = new ArrayList<>();
    private List<String> permissionList  = new ArrayList<>();
}
