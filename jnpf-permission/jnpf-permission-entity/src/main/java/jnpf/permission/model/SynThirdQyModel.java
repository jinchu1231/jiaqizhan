package jnpf.permission.model;

import jnpf.permission.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 同步到企业微信model
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-04
 */
@Data
@NoArgsConstructor
public class SynThirdQyModel {
    private Boolean isBatch;
    private UserEntity userEntity;
    private String accessToken;

    public SynThirdQyModel(Boolean isBatch, UserEntity userEntity, String accessToken) {
        this.isBatch = isBatch;
        this.userEntity = userEntity;
        this.accessToken = accessToken;
    }
}
