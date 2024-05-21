package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 操作日志模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 10:10
 */
@Data
public class HandleLogVO implements Serializable {

    /**
     * id
     */
    public String id;

    /**
     * 请求时间
     */
    public Long creatorTime;

    /**
     * 请求用户名
     */
    public String userName;

    /**
     * 请求IP
     */
    public String ipAddress;

    /**
     * 请求设备
     */
    public String platForm;

    /**
     * 操作模块
     */
    public String moduleName;

    /**
     * 操作类型
     */
    public String requestMethod;

    /**
     * 请求耗时
     */
    public int requestDuration;
    @Schema(description = "地点")
    private String ipAddressName;
    @Schema(description = "浏览器")
    private String browser;
    @Schema(description = "请求地址")
    private String requestUrl;

}
