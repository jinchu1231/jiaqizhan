package jnpf.integrate.model.nodeJson;

import jnpf.base.UserInfo;
import jnpf.integrate.entity.IntegrateEntity;
import jnpf.integrate.entity.IntegrateNodeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrateChildNodeModel {
    private Map<String, Object> data = new HashMap<>();
    private List<Map<String, Object>> dataListAll = new ArrayList<>();
    private List<IntegrateNodeEntity> nodeList = new ArrayList<>();
    private String node;
    private IntegrateEntity entity;
    private String retryNodeCode;
    private UserInfo userInfo;

}
