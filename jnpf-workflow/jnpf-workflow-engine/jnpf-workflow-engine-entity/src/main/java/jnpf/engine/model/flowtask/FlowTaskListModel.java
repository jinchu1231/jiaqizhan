package jnpf.engine.model.flowtask;

import lombok.Data;

import java.util.Date;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class FlowTaskListModel {
    private String id;
    private String processId;
    private String enCode;
    private String fullName;
    private Integer flowUrgent;
    private String flowId;
    private String flowCode;
    private String flowName;
    private String flowCategory;
    private String flowType;
    private Date startTime;
    private Date endTime;
    private String thisStep;
    private String thisStepId;
    private Integer status;
    private Integer completion;
    private String creatorUserId;
    private Date creatorTime;
    private String handleId;
    private String nodeName;
    private String lastModifyUserId;
    private Date lastModifyTime;
    private String approversProperties;
    private String description;
    private String flowVersion;
    private Integer taskStatus;
    private String sortCode;
    private String counterSign;
    private String templateId;
    private String delegateUser;
    private String icon;
    private String circulateId;
}
