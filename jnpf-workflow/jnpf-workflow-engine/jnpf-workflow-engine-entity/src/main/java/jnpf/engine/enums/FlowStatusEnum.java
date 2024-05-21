package jnpf.engine.enums;

/**
 * 提交状态
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public enum FlowStatusEnum {
    //不操作
    none("-1"),
    //保存
    save("1"),
    // 提交
    submit("0");

    private String message;

    FlowStatusEnum(String message) {
        this.message = message;
    }

    /**
     * 根据状态code获取枚举名称
     *
     * @return
     */
    public static FlowStatusEnum getByCode(Integer code) {
        for (FlowStatusEnum status : FlowStatusEnum.values()) {
            if (status.getMessage().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
