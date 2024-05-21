package gas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
/**
 * 路线记录
 *
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
@TableName("gas_base_route")
public class GasBaseRouteEntity  {
    @TableId(value ="F_ID"  )
    private String id;
    @TableField(value = "F_NAME" , updateStrategy = FieldStrategy.IGNORED)
    private String name;
    @TableField(value = "F_CODE" , updateStrategy = FieldStrategy.IGNORED)
    private String code;
    @TableField("F_DELETE_MARK")
    private Integer deleteMark;
    @TableField("F_DELETE_TIME")
    private Date deleteTime;
    @TableField("F_DELETE_USER_ID")
    private String deleteUserId;
    @TableField("F_FLOW_TASK_ID")
    private String flowTaskId;
    @TableField("F_FLOW_ID")
    private String flowId;

}
