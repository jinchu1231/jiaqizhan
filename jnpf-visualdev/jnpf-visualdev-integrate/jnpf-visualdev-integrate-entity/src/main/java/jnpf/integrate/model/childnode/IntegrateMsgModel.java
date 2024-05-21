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
public class IntegrateMsgModel {
    //0.关闭  1.自定义  3.默认
    private Integer on = 0;
    private String msgId;
    private List<IntegrateTemplateModel> templateJson = new ArrayList<>();
}
