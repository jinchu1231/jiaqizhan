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
@TableName("base_integrate_queue")
public class IntegrateQueueEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 状态
     */
    @TableField("f_state")
    private Integer state;

    /**
     * 集成主键
     */
    @TableField("F_INTEGRATE_ID")
    private String integrateId;

    /**
     * 执行时间
     */
    @TableField("F_EXECUTION_TIME")
    private Date executionTime;

    /**
     * 名称
     */
    @TableField("F_FULL_NAME")
    private String fullName;

}
