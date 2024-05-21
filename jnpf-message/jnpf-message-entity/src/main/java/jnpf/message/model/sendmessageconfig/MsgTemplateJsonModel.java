package jnpf.message.model.sendmessageconfig;

import lombok.Data;

/**
 * 解析引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class MsgTemplateJsonModel {

    public String field;
    public String fieldName;
    public String relationField;
    private String id;
    private Boolean isSubTable = false;
    private String msgTemplateId;

}
