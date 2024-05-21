package jnpf.engine.model.flowtask;

import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/25 13:57
 */
@Data
public class FlowCountersignModel {
    private FlowTaskNodeEntity taskNode;
    private List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
    private Boolean fixed = false;
    private double pass = 100;
    private List<FlowTaskOperatorEntity> passNumList = new ArrayList<>();
}
