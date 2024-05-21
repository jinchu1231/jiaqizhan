package jnpf.engine.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程评论
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
@TableName("flow_comment")
public class FlowCommentEntity extends SuperExtendEntity.SuperExtendEnabledEntity<String> {

    /**
     * 任务主键
     */
    @TableField("F_TASK_ID")
    private String taskId;

    /**
     * 文本
     */
    @TableField("F_TEXT")
    private String text;

    /**
     * 图片
     */
    @TableField("F_IMAGE")
    private String image;

    /**
     * 附件
     */
    @TableField("F_FILE")
    @JSONField(name = "file")
    private String fileName;

}
