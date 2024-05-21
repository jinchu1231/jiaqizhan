package jnpf.engine.model.flowengine;

import lombok.Data;

import java.util.Date;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/11/10 10:53:15
 */
@Data
public class FlowTaskOperatorRejectModel {
    private String id;
    private Integer state;
    private Integer handleStatus;
    private Date handleTime;
    //    private Integer completion;
    private String draftData;
}
