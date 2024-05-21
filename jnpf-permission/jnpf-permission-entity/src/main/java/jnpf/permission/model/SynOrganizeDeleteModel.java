package jnpf.permission.model;

import jnpf.permission.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-05
 */
@Data
@NoArgsConstructor
public class SynOrganizeDeleteModel {
    private Boolean isBatch;
    private String organizeId;
    private String accessToken;
    public SynOrganizeDeleteModel(Boolean isBatch, String organizeId, String accessToken) {
        this.isBatch = isBatch;
        this.organizeId = organizeId;
        this.accessToken = accessToken;
    }
}
