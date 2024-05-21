package jnpf.integrate.model.childnode;

import lombok.Data;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
public class IntegrateChildNode {
    private String type;
    private String content;
    private String nodeId;
    private String prevId;
    private IntegrateChildNode childNode;
    private IntegrateProperties properties = new IntegrateProperties();
}
