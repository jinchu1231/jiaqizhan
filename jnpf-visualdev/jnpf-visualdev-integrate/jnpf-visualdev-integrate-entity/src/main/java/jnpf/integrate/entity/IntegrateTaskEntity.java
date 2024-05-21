package jnpf.integrate.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

import java.util.Date;

/**
 *
 * @version V3.4.5
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 */
@Data
@TableName("base_integrate_task")
public class IntegrateTaskEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 集成主键
     */
    @TableField("F_INTEGRATE_ID")
    private String integrateId;

    /**
     * 实例进程
     */
    @TableField("F_PROCESS_ID")
    private String processId;

    /**
     * 集成类型(1-事件，2-定时 )
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 集成模板
     */
    @TableField("F_TEMPLATE_JSON")
    private String templateJson;


    /**
     * 数据主键
     */
    @TableField("F_DATA_ID")
    private String dataId;

    /**
     * 数据
     */
    @TableField("F_DATA")
    private String data;

    /**
     * 父节点id
     */
    @TableField("F_PARENT_ID")
    private String parentId;

    /**
     * 父节点时间
     */
    @TableField("F_PARENT_TIME")
    private Date parentTime;

    /**
     * 执行时间
     */
    @TableField("F_EXECUTION_TIME")
    private Date executionTime;

    /**
     * 结果 (0.失败 1.成功)
     */
    @TableField("F_RESULT_TYPE")
    private Integer resultType;
}
