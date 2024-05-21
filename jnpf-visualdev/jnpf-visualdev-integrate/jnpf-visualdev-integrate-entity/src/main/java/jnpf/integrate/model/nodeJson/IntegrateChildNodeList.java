package jnpf.integrate.model.nodeJson;

import jnpf.integrate.model.childnode.IntegrateProperties;
import lombok.Data;

import java.util.Date;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
public class IntegrateChildNodeList {
    private String nodeId;
    private String prevId;
    private String nextId;
    private String type;
    private Integer integrateType;
    private Date startTime = new Date();
    private Date endTime= new Date();
    private IntegrateProperties properties = new IntegrateProperties();
}
