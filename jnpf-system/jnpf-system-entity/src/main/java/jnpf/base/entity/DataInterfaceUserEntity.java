package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.4.7
 * @copyright 引迈信息技术有限公司
 * @date 2021/9/20 9:22
 */
@Data
@TableName("base_data_interface_user")
public class DataInterfaceUserEntity extends SuperBaseEntity.SuperCBaseEntity<String> {
    /**
     * 用户主键
     */
    @TableField("f_user_id")
    private String userId;
    /**
     * 用户密钥
     */
    @TableField("f_user_key")
    private String userKey;
    /**
     * 接口认证主键
     */
    @TableField("f_oauth_id")
    private String oauthId;
    /**
     * 排序
     */
    @TableField("f_sort_code")
    private Long sortCode;
}
