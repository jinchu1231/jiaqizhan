package jnpf.permission.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 系统角色
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
@TableName("base_role")
public class RoleEntity extends PermissionEntityBase{

    /**
     * 角色类型
     */
    @TableField("F_TYPE")
    private String type;



    /**
     * 全局标识
     */
    @TableField("F_GLOBAL_MARK")
    private Integer globalMark;

}
