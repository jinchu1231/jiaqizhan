package jnpf.engine.util;

import lombok.Data;

/**
 * 在线工作流开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
public class FlowNature {

    /**
     * 驳回开始
     **/
    public static String START = "0";

    /**
     * 驳回上一节点
     **/
    public static String UP = "1";

    /**
     * 自选节点
     **/
    public static String Reject = "2";

    /**
     * 候选人
     **/
    public static Integer Candidates = 1;

    /**
     * 异常人
     **/
    public static Integer CandidatesError = 2;

    /**
     * 重新审批
     **/
    public static Integer RestartType = 1;

    /**
     * 当前审批
     **/
    public static Integer PresentType = 2;

    /**
     * 待办事宜
     **/
    public static String WAIT = "1";

    /**
     * 已办事宜
     **/
    public static String TRIAL = "2";

    /**
     * 抄送事宜
     **/
    public static String CIRCULATE = "3";

    /**
     * 抄送事宜
     **/
    public static String BATCH = "4";

    /**
     * 工作流完成
     **/
    public static String CompletionEnd = "100";

    /**
     * 流程父节点
     **/
    public static String ParentId = "0";

    /**
     * 加签人
     **/
    public static String RecordStatus = "0";

    /**
     * 审批开始
     **/
    public static String NodeStart = "start";

    /**
     * 审批结束
     **/
    public static String NodeEnd = "end";

    /**
     * 子流程
     **/
    public static String NodeSubFlow = "subFlow";

    /**
     * 结束节点
     **/
    public static String EndRound = "endround";

    /**
     * 或签
     **/
    public static Integer FixedApprover = 0;

    /**
     * 会签
     **/
    public static Integer FixedJointlyApprover = 1;

    /**
     * 依次
     **/
    public static Integer ImproperApprover = 2;

    /**
     * 通过
     **/
    public static Integer AuditCompletion = 1;

    /**
     * 拒绝
     **/
    public static Integer RejectCompletion = -1;

    /**
     * 进行
     **/
    public static Integer ProcessCompletion = 0;

    /**
     * 子流程同步
     **/
    public static Integer ChildSync = 0;

    /**
     * 子流程异步
     **/
    public static Integer ChildAsync = 1;

    /**
     * 完成情况
     */
    public static Integer Progress = 100;

    /**
     * 流程标题
     */
    public static Integer TitleType = 0;

    /**
     * 加签回流
     */
    public static Integer Reflux = 0;

    /**
     * 加签前
     */
    public static Integer Before = 1;

    /**
     * 加签后
     */
    public static Integer Later = 2;

    /**
     * 发起消息
     */
    public static Integer StartMsg = 0;

    /**
     * 审批消息
     */
    public static Integer ApproveMsg = 1;

    /**
     * 结束消息
     */
    public static Integer EndMsg = 2;

    /**
     * 复活取最后数据
     */
    public static Integer ResurgenceLast = 0;

    /**
     * 复活取当时数据
     */
    public static Integer ResurgenceThis = 1;

    /**
     * 事件失败流程继续
     */
    public static Integer FuncResume = 0;

    /**
     * 事件失败流程终止
     */
    public static Integer FuncTerminate = 1;

    /**
     * 发起撤回终止
     */
    public static Integer RevokeTerminate = 1;

    /**
     * 发起撤回提交
     */
    public static Integer RevokeSubmit = 2;

    /**
     * 无
     **/
    public static Integer RejectNo = 0;

    /**
     * 百分比
     **/
    public static Integer RejectPercent = 1;

    /**
     * 人数
     **/
    public static Integer RejectNumber = 2;


}
