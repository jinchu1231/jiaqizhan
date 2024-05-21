package jnpf.permission.model.organizerelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/5/11 17:27
 */
@Data
public class AutoGetMajorOrgIdModel implements Serializable {
    private String userId;
    private List<String> orgIds;
    private String organizeId;
    private String systemId;

    public AutoGetMajorOrgIdModel() {
    }

    public AutoGetMajorOrgIdModel(String userId, List<String> orgIds, String organizeId, String systemId) {
        this.userId = userId;
        this.orgIds = orgIds;
        this.organizeId = organizeId;
        this.systemId = systemId;
    }
}
