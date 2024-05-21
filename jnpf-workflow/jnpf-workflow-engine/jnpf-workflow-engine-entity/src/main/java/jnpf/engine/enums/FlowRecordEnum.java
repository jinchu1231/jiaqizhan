package jnpf.engine.enums;

/**
 * 工作流开发
 *
 * @author JNPF开发平台组
 * @version V3.0.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public enum FlowRecordEnum {

    //拒绝
    reject(0, "拒绝"),
    //同意
    audit(1, "同意"),
    //提交
    submit(2, "提交"),
    //撤回
    revoke(3, "撤回"),
    //终止
    cancel(4, "终止"),
    //指派
    assign(5, "指派"),
    //加签
    copyId(6, "加签"),
    //转办
    transfer(7, "转办"),
    //变更
    change(8, "变更"),
    //复活
    resurrection(9, "复活"),
    //前加签
    befoCopyId(10, "前加签"),
    //挂起
    suspend(11, "挂起"),
    //恢复
    restore(12, "恢复"),
    //转向
    swerve(13, "转向"),
    //结束
    end(100, "结束"),
    //节点撤回
    recall(-1, "节点撤回");

    private Integer code;
    private String message;

    FlowRecordEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
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
