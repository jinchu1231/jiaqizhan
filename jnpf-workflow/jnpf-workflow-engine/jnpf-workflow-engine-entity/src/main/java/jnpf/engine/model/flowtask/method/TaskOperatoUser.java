package jnpf.engine.model.flowtask.method;

import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import lombok.Data;

import java.util.Date;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-08-26
 */
@Data
public class TaskOperatoUser {
    /**
     * 审批人id
     */
    private String handLeId;
    /**
     * 审批日期
     */
    private Date date;
    /**
     * 当前节点数据
     */
    private ChildNodeList childNode;
    /**
     * 经办id
     */
    private String id;
    /**
     * 回流id
     */
    private String rollbackId;
    /**
     * 父级id
     */
    private String parentId;
    /**
     * 是否冻结审批
     */
    private Boolean rejectUser = false;
    /**
     * 自动审批
     */
    private String automation;
    /**
     * 第几条数据
     */
    private long sortCode;
}

