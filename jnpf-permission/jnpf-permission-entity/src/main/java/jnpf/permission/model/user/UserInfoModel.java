package jnpf.permission.model.user;

import jnpf.base.user.UserTenantModel;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息模型
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/5/10 16:17
 */
@Data
public class UserInfoModel extends UserTenantModel implements Serializable {
    private String userId;

    public UserInfoModel() {
    }

    public UserInfoModel(String userId, String tenantId) {
        super(tenantId);
        this.userId = userId;
    }
}
