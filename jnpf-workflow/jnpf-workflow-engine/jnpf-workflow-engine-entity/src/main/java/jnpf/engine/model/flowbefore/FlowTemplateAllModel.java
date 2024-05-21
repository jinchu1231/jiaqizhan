package jnpf.engine.model.flowbefore;

import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:18
 */
@Data
public class FlowTemplateAllModel {
    private FlowTemplateJsonEntity templateJson = new FlowTemplateJsonEntity();
    private FlowTemplateEntity template = new FlowTemplateEntity();
    private List<FlowTemplateJsonEntity> templateJsonList = new ArrayList<>();
}
