package jnpf.permission.model.user;

import jnpf.base.user.UserTenantModel;
import jnpf.permission.entity.UserEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户模型
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/5/13 14:02
 */
@Data
public class UserUpdateModel extends UserTenantModel implements Serializable {
    private UserEntity entity;

    public UserUpdateModel(UserEntity entity, String tenantId) {
        super(tenantId);
        this.entity = entity;
    }

    public UserUpdateModel() {
    }
}
