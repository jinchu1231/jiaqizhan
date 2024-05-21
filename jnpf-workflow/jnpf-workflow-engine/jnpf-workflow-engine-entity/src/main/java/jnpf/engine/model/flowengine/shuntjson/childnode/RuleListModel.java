package jnpf.engine.model.flowengine.shuntjson.childnode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 解析引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:12
 */
@Data
public class RuleListModel {
    /**
     * 父字段
     **/
    @Schema(description = "父字段")
    private String parentField;
    /**
     * 子字段
     **/
    @Schema(description = "子字段")
    private String childField;
}
