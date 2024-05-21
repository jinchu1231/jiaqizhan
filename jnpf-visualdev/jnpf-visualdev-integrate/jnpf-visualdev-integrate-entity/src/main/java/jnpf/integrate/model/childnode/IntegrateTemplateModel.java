package jnpf.integrate.model.childnode;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
public class IntegrateTemplateModel {
    //远端接口
    private String field;
    private Boolean required = false;
    private String sourceType;
    private String relationField;


    //发送配置
    private String templateId;
    private String sendConfigId;
    private String msgTemplateName;
    private List<IntegrateParamModel> paramJson = new ArrayList<>();
}
