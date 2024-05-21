package jnpf.permission.model;

import jnpf.permission.entity.OrganizeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 组织或部门同步到第三方
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-05
 */
@Data
@NoArgsConstructor
public class SynOrganizeModel {
    private Boolean isBatch;
    private OrganizeEntity deptEntity;
    private String accessToken;

    public SynOrganizeModel(Boolean isBatch, OrganizeEntity deptEntity, String accessToken) {
        this.isBatch = isBatch;
        this.deptEntity = deptEntity;
        this.accessToken = accessToken;
    }
}
