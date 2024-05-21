package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.database.model.dto.ModelDTO;
import jnpf.database.model.interfaces.JdbcGetMod;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 打印模板-流程经办记录
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Data
@TableName("flow_task_operator_record")
public class OperatorRecordEntity extends SuperExtendEntity<String> implements JdbcGetMod {

    /**
     * 节点编码
     */
    @TableField("F_NODECODE")
    private String nodeCode;

    /**
     * 节点名称
     */
    @TableField("F_NODENAME")
    private String nodeName;

    /**
     * 经办状态 0-拒绝、1-同意、2-提交、3-撤回、4-终止、5-指派、6-加签、7-转办
     */
    @TableField("F_HANDLESTATUS")
    private Integer handleStatus;

    /**
     * 经办人员
     */
    @TableField("F_HANDLEID")
    private String handleId;

    /**
     * 经办人员
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 经办时间
     */
    @TableField("F_HANDLETIME")
    private Date handleTimeOrigin;

    /**
     * 经办时间（时间戳）
     */
    private Long handleTime;

    /**
     * 经办理由
     */
    @TableField("F_HANDLEOPINION")
    private String handleOpinion;

    /**
     * 流转操作人
     */
    @TableField("F_OPERATORID")
    private String operatorId;

    /**
     * 经办主键
     */
    @TableField(value="F_TASKOPERATORID",fill = FieldFill.UPDATE)
    private String taskOperatorId;

    /**
     * 节点主键
     */
    @TableField(value="F_TASKNODEID",fill = FieldFill.UPDATE)
    private String taskNodeId;

    /**
     * 任务主键
     */
    @TableField("F_TASKID")
    private String taskId;

    /**
     * 签名图片
     */
    @TableField("F_SIGNIMG")
    private String signImg;

    /**
     * 0.进行数据 -1.作废数据 1.加签数据 3.已办不显示数据
     */
    @TableField("F_STATUS")
    private String status;

    @Override
    public void setMod(ModelDTO modelDTO){
        try{
            ResultSet resultSet = modelDTO.getResultSet();
            this.setId(resultSet.getString("f_id"));
            this.setNodeCode(resultSet.getString("f_node_code"));
            this.setNodeName(resultSet.getString("f_node_name"));
            this.setHandleStatus(resultSet.getInt("f_handle_status"));
            this.setHandleId(resultSet.getString("f_handle_id"));
            this.setHandleTimeOrigin(resultSet.getTimestamp("f_handle_time"));
            this.setHandleOpinion(resultSet.getString("f_handle_opinion"));
            this.setOperatorId(resultSet.getString("f_operator_id"));
            this.setTaskOperatorId(resultSet.getString("f_task_operator_id"));
            this.setTaskNodeId(resultSet.getString("f_task_node_id"));
            this.setTaskId(resultSet.getString("f_task_id"));
            this.setSignImg(resultSet.getString("f_sign_img"));
            this.setStatus(resultSet.getString("f_status"));
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
