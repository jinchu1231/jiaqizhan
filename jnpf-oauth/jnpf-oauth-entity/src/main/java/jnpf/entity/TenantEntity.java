package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 租户信息
 *
 * @author JNPF开发平台组
 * @version V1.2.191207
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
@TableName("base_tenant")
public class TenantEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 编码
     */
    @TableField("F_ENCODE")
    private String enCode;

    /**
     * 姓名
     */
    @TableField("F_FULLNAME")
    private String fullName;

    /**
     * 密码
     */
    @TableField("F_PASSWORD")
    private String password;

    /**
     * 公司
     */
    @TableField("F_COMPANYNAME")
    private String companyName;

    /**
     * 过期时间
     */
    @TableField("F_EXPIRESTIME")
    private String expiresTime;

    /**
     * 连接驱动
     */
    @TableField("F_DBTYPE")
    private String dbType;

    /**
     * 主机地址
     */
    @TableField("F_DBHOST")
    private String dbHost;

    /**
     * 端口
     */
    @TableField("F_DBPORT")
    private String dbPort;

    /**
     * 用户
     */
    @TableField("F_DBUSERNAME")
    private String dbUserName;

    /**
     * 密码
     */
    @TableField("F_DBPASSWORD")
    private String dbPassword;

    /**
     * 服务名
     */
    @TableField("F_DBSERVICENAME")
    private String dbServiceName;

    /**
     * ip
     */
    @TableField("F_IPADDRESS")
    private String ipAddress;

    /**
     * ip城市
     */
    @TableField("F_IPADDRESSNAME")
    private String ipAddressName;

    /**
     * 来源
     */
    @TableField("F_SOURCEWEBSITE")
    private String sourceWebsite;

}
