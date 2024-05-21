package jnpf.engine.enums;

/**
 * 附件条件
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/14 16:50
 */
public enum FlowExtraRuleEnum {
    /**
     * 无条件
     */
    unconditional(1, "无条件"),
    /**
     * 同一部门
     */
    organize(2, "同一部门"),
    /**
     * 同一岗位
     */
    position(3, "同一岗位"),
    /**
     * 发起人上级
     */
    manager(4, "发起人上级"),
    /**
     * 发起人下属
     */
    subordinate(5, "发起人下属"),
    /**
     * 同一公司
     */
    department(6, "同一公司");

    private int code;
    private String message;

    FlowExtraRuleEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态code获取枚举名称
     *
     * @return
     */
    public static FlowExtraRuleEnum getByCode(Integer code) {
        for (FlowExtraRuleEnum status : FlowExtraRuleEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
