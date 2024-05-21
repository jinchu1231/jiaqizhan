package jnpf.integrate.model.integratetask;

import lombok.Data;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
public class IntegrateTaskListVO {
    private String id;
    private String processId;
    private String parentId;
    private Integer isRetry;
    private Long parentTime;
    private Long executionTime;
    private Integer resultType;
}
