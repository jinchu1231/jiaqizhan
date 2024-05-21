package jnpf.integrate.model.childnode;

import jnpf.database.model.superQuery.SuperQueryJsonModel;
import jnpf.emnus.SearchMethodEnum;
import jnpf.model.visualJson.FieLdsModel;
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
public class IntegrateProperties {
    private String title;
    private String formId;
    private String flowId;
    private Integer formType = 1;
    private List<FieLdsModel> formFieldList = new ArrayList<>();
    private List<TransferModel> transferList = new ArrayList<>();
    private List<SuperQueryJsonModel> ruleList = new ArrayList<>();
    private Integer triggerEvent;
    //0.不新增 1.新增
    private Integer addRule = 0;
    //0-不更新 1-新增
    private Integer unFoundRule = 0;
    //0-删除未找到 1-删除已找到
    private Integer deleteRule = 0;
    private String ruleMatchLogic = SearchMethodEnum.And.getSymbol();


    private String msgId;
    private List<String> msgUserType = new ArrayList<>();
    private List<String> msgUserIds = new ArrayList<>();
    private List<IntegrateTemplateModel> templateJson = new ArrayList<>();
    private List<IntegrateTemplateModel> interfaceTemplateJson = new ArrayList<>();
    private IntegrateMsgModel startMsgConfig = new IntegrateMsgModel();
    private IntegrateMsgModel failMsgConfig= new IntegrateMsgModel();



    private Long startTime;
    private String cron;
    private Integer endTimeType = 1;
    //次数
    private Integer endLimit = 1;
    //结束时间
    private Long endTime;
    //类型
    private Integer integrateType = 2;

    private List<String> initiator;



}
