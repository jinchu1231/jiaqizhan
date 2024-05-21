package jnpf.engine.model.flowengine.shuntjson.childnode;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.engine.util.FlowNature;
import lombok.Data;

/**
 * 解析引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class CounterSign {
    /**
     * 通过类型 0.无 1.百分比 2.人数
     */
    @Schema(description = "通过类型")
    private Integer auditType = FlowNature.RejectPercent;
    /**
     * 通过百分比
     */
    @Schema(description = "通过百分比")
    private Integer auditRatio = 100;
    /**
     * 通过人数
     */
    @Schema(description = "通过人数")
    private Integer auditNum = 1;
    /**
     * 拒绝类型 0.无 1.百分比 2.人数
     */
    @Schema(description = "拒绝类型")
    private Integer rejectType = FlowNature.RejectNo;
    /**
     * 拒绝百分比
     */
    @Schema(description = "拒绝百分比")
    private Integer rejectRatio = 10;
    /**
     * 拒绝人数
     */
    @Schema(description = "拒绝人数")
    private Integer rejectNum = 1;
}
