package jnpf.base.model.read;


import jnpf.util.StringUtil;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/8/20
 */
public enum ReadEnum {
    /**
     * 模型
     */
    model("model"),
    /**
     * 控制器
     */
    controller("controller"),
    /**
     * 业务层
     */
    service("service"),
    /**
     * 数据层
     */
    mapper("mapper"),
    /**
     * 实体
     */
    entity("entity"),
    /**
     * web
     */
    web("web"),
    /**
     * json
     */
    json("json"),
    /**
     * app
     */
    app("app");

    private String message;

    ReadEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态code获取枚举名称
     *
     * @return
     */
    public static ReadEnum getMessage(String path) {
        if (StringUtil.isNotEmpty(path)) {
            for (ReadEnum status : ReadEnum.values()) {
                if (path.contains(status.getMessage())) {
                    return status;
                }
            }
        }
        return null;
    }

}
