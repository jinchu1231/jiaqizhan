package jnpf.engine.model.flowengine.shuntjson.childnode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/17 17:54
 */
@Data
public class TimeModel {

    //----------------------限时------------------------
    /**
     * 开始时间 0-接收时间，1-发起时间，2-表单变量
     */
    @Schema(description = "开始类型")
    private Integer nodeLimit = 0;
    /**
     * 表单字段key
     */
    @Schema(description = "表单字段key")
    private String formField = "";
    /**
     * 限定时长 默认24 （小时）
     */
    @Schema(description = "限定时长")
    private Integer duringDeal = 24;

    //--------------------超时-------------------------
    /**
     * 超时自动审批
     */
    @Schema(description = "超时自动审批")
    private Boolean overAutoApprove = false;
    /**
     * 超时自动审批次数
     */
    @Schema(description = "超时次数")
    private Integer overAutoApproveTime = 5;

    //---------------------公共----------------------------------
    /**
     * 超时设置 0.关闭  1.自定义  2.同步发起配置
     */
    @Schema(description = "超时设置")
    private Integer on = 0;
    /**
     * 事件(提醒、超时)
     */
    @Schema(description = "事件")
    private Boolean overEvent = false;
    /**
     * 次数(提醒、超时)
     */
    @Schema(description = "次数")
    private Integer overEventTime = 5;
    /**
     * 第一次时间
     * （小时）第一次超时时间默认值0=第一次触发超时事件时间=节点限定时长起始值+节点处理限定时长+设定的第一次超时时间
     */
    @Schema(description = "第一次时间")
    private Integer firstOver = 0;
    /**
     * 时间间隔(提醒、超时)
     */
    @Schema(description = "时间间隔")
    private Integer overTimeDuring = 2;
    /**
     * 通知(提醒、超时)
     */
    @Schema(description = "通知")
    private Boolean overNotice = false;
}
