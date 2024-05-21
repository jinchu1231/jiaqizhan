package jnpf.engine.enums;

/**
 * 异常规则
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/17 10:57
 */
public enum FlowErrorRuleEnum {
    /**
     * 1.超级管理员
     */
    administrator(1, "超级管理员"),
    /**
     * 2.指定人员
     */
    initiator(2, "指定人员"),
    /**
     * 3.上一节点审批人指定处理人
     */
    node(3, "上一节点审批人指定处理人"),
    /**
     * 4.默认审批通过
     */
    pass(4, "默认审批通过"),
    /**
     * 5.无法提交
     */
    notSubmit(5, "无法提交"),
    /**
     * 6.发起者本人处理
     */
    creatorUserId(6, "发起者本人处理");

    private int code;
    private String message;

    FlowErrorRuleEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态code获取枚举名称
     *
     * @return
     */
    public static FlowErrorRuleEnum getByCode(Integer code) {
        for (FlowErrorRuleEnum status : FlowErrorRuleEnum.values()) {
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
