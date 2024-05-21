package jnpf.engine.util;

import jnpf.base.UserInfo;
import jnpf.emnus.SearchMethodEnum;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.GroupsModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.ProperCond;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.Custom;
import jnpf.engine.model.flowengine.shuntjson.nodejson.DateProperties;
import jnpf.engine.model.flowtask.FlowConditionModel;
import jnpf.permission.entity.UserEntity;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 在线工作流开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
public class FlowJsonUtil {

    /**
     * 外层节点
     **/
    private static String cusNum = "0";

    /**
     * 获取下一节点
     **/
    public static String getNextNode(FlowConditionModel conditionModel) {
        String next = nextNodeId(conditionModel);
        return next;
    }

    /**
     * 下一节点id
     **/
    private static String nextNodeId(FlowConditionModel conditionModel) {
        List<ChildNodeList> childNodeListAll = conditionModel.getChildNodeListAll();
        String nodeId = conditionModel.getNodeId();
        String nextId = "";
        boolean flag = false;
        ChildNodeList childNode = childNodeListAll.stream().filter(t -> t.getCustom().getNodeId().equals(nodeId)).findFirst().orElse(null);
        String contextType = childNode.getConditionType();
        //条件、分流的判断
        if (StringUtils.isNotEmpty(contextType)) {
            if (FlowCondition.CONDITION.equals(contextType)) {
                List<String> nextNodeId = new ArrayList<>();
                getContionNextNode(conditionModel, nextNodeId);
                nextId = String.join(",", nextNodeId);
                if (StringUtils.isNotEmpty(nextId)) {
                    flag = true;
                }
            } else {
                nextId = childNode.getCustom().getFlowId();
                flag = true;
            }
        }
        //子节点
        if (!flag) {
            if (childNode.getCustom().getFlow()) {
                nextId = childNode.getCustom().getFlowId();
            } else {
                //不是外层的下一节点
                if (!cusNum.equals(childNode.getCustom().getNum())) {
                    nextId = childNode.getCustom().getFirstId();
                    if (childNode.getCustom().getChild()) {
                        nextId = childNode.getCustom().getChildNode();
                    }
                } else {
                    //外层的子节点
                    if (childNode.getCustom().getChild()) {
                        nextId = childNode.getCustom().getChildNode();
                    }
                }
            }
        }
        return nextId;
    }

    //---------------------------------------------------递归获取当前的上节点和下节点----------------------------------------------

    /**
     * 获取当前已完成节点
     **/
    private static void upList(List<FlowTaskNodeEntity> flowTaskNodeList, String node, Set<String> upList, String[] tepId) {
        FlowTaskNodeEntity entity = flowTaskNodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (entity != null) {
            List<String> list = flowTaskNodeList.stream().filter(t -> t.getSortCode() != null && t.getSortCode() < entity.getSortCode()).map(t -> t.getNodeCode()).collect(Collectors.toList());
            list.removeAll(Arrays.asList(tepId));
            upList.addAll(list);
        }
    }

    /**
     * 获取当前未完成节点
     **/
    private static void nextList(List<FlowTaskNodeEntity> flowTaskNodeList, String node, Set<String> nextList, String[] tepId) {
        FlowTaskNodeEntity entity = flowTaskNodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (entity != null) {
            List<String> list = flowTaskNodeList.stream().filter(t -> t.getSortCode() != null && t.getSortCode() > entity.getSortCode()).map(t -> t.getNodeCode()).collect(Collectors.toList());
            list.removeAll(Arrays.asList(tepId));
            nextList.addAll(list);
        }
    }

    //---------------------------------------------------条件----------------------------------------------

    /**
     * 递归条件
     **/
    private static void getContionNextNode(FlowConditionModel conditionModel, List<String> nextNodeId) {
        String nodeId = conditionModel.getNodeId();
        List<ConditionList> conditionListAll = conditionModel.getConditionListAll();
        List<ConditionList> conditionAll = conditionListAll.stream().filter(t -> t.getPrevId().equals(nodeId)).collect(Collectors.toList());
        for (ConditionList condition : conditionAll) {
            List<ProperCond> conditions = condition.getConditions();
            String matchLogic = condition.getMatchLogic();
            boolean flag = nodeConditionDecide(conditionModel, conditions, matchLogic);
            //判断条件是否成立或者其他情况条件
            if (flag || condition.getIsDefault()) {
                String conditionId = condition.getNodeId();
                List<ConditionList> childCondition = conditionListAll.stream().filter(t -> t.getPrevId().equals(conditionId)).collect(Collectors.toList());
                if (childCondition.size() > 0) {
                    conditionModel.setNodeId(conditionId);
                    getContionNextNode(conditionModel, nextNodeId);
                }
                if (nextNodeId.size() == 0) {
                    //先获取条件下的分流节点
                    if (condition.getFlow()) {
                        nextNodeId.add(condition.getFlowId());
                    } else {
                        //条件的子节点
                        if (condition.getSwerve()) {
                            nextNodeId.add(condition.getSwerveNode());
                        } else if (condition.getChild()) {
                            nextNodeId.add(condition.getChildNodeId());
                        } else {
                            nextNodeId.add(condition.getFirstId());
                        }
                    }
                }
            }
        }
    }

    /**
     * 节点条件判断
     **/
    private static boolean nodeConditionDecide(FlowConditionModel conditionModel, List<ProperCond> conditionList, String matchLogic) {
        String data = conditionModel.getData();
        boolean flag = false;
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("js");
        Map<String, Object> map = JsonUtil.stringToMap(data);
        List<String> expressionAll = new ArrayList<>();
        StringBuilder condition = new StringBuilder();
        for (int k = 0; k < conditionList.size(); k++) {
            StringBuilder expression = new StringBuilder();
            expression.append("(");
            ProperCond properCond = conditionList.get(k);
            String logic = properCond.getLogic();
            List<GroupsModel> groups = properCond.getGroups();
            for (int i = 0; i < groups.size(); i++) {
                GroupsModel groupsModel = groups.get(i);
                String contain = "!=-1";
                String field = groupsModel.getField();
                String jnpfKey = groupsModel.getJnpfKey();
                int fieldType = groupsModel.getFieldType();
                Object form = fieldType == 1 ? formValue(conditionModel, jnpfKey, map.get(field)) : formula(groupsModel, map);
                Object formValue = form;
                String symbol = groupsModel.getSymbol();
                boolean include = ("like".equals(symbol) || "notLike".equals(symbol));
                if ("<>".equals(symbol)) {
                    symbol = "!=";
                }
                int fieldValueType = groupsModel.getFieldValueType();
                String valueJnpfKey = StringUtils.isNotEmpty(groupsModel.getFieldValueJnpfKey()) ? groupsModel.getFieldValueJnpfKey() : jnpfKey;
                Object filedData = groupsModel.getFieldValue();
                Object value = fieldValueType == 2 ? filedValue(conditionModel, filedData, valueJnpfKey, form) : filedData(conditionModel, filedData, valueJnpfKey, form);
                Object fieldValue = value;
                String pression = formValue + symbol + fieldValue;
                if (include) {
                    if ("notLike".equals(symbol)) {
                        contain = "==-1";
                    }
                    symbol = ".indexOf";
                    formValue = formValue == null ? "''" : formValue;
                    pression = formValue + ".toString()" + symbol + "(" + fieldValue + ")" + contain;
                }
                expression.append(pression);
                if (!StringUtils.isEmpty(logic) && i != groups.size() - 1) {
                    expression.append(" " + search(logic) + " ");
                }
            }
            expression.append(")");
            expressionAll.add(expression.toString());
        }
        for (int i = 0; i < expressionAll.size(); i++) {
            String script = expressionAll.get(i);
            String search = i != expressionAll.size() - 1 ? search(matchLogic) : "";
            condition.append(script + " " + search + " ");
        }
        try {
            flag = (Boolean) scriptEngine.eval(condition.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return flag;
    }

    /**
     * 条件表达式
     *
     * @param logic
     */
    private static String search(String logic) {
        return SearchMethodEnum.And.getSymbol().equalsIgnoreCase(logic) ? "&&" : "||";
    }

    /**
     * 条件数据修改
     *
     * @param conditionModel
     * @param value
     */
    private static Object filedValue(FlowConditionModel conditionModel, Object value, String jnpfKey, Object form) {
        UserInfo userInfo = conditionModel.getUserInfo();
        if ("currentUser".equals(value)) {
            value = userInfo.getUserId();
        }
        try {
            try {
                List<List<String>> dataAll = JsonUtil.getJsonToBean(String.valueOf(value), List.class);
                List<String> id = new ArrayList<>();
                for (List<String> data : dataAll) {
                    id.addAll(data);
                }
                value = String.join(",", id);
            } catch (Exception e) {
                try {
                    List<String> id = new ArrayList<>();
                    List<String> dataAll = JsonUtil.getJsonToList(String.valueOf(value), String.class);
                    if (JnpfKeyConsts.CURRORGANIZE.equals(jnpfKey)) {
                        value = dataAll.stream().filter(t -> t.equals(form)).findFirst().orElse(null);
                    } else {
                        for (String data : dataAll) {
                            id.add(data);
                        }
                        value = String.join(",", id);
                    }
                } catch (Exception e1) {

                }
            }
        } catch (Exception e) {

        }
        if (value instanceof CharSequence) {
            value = "'" + value + "'";
        }
        return value;
    }

    /**
     * 条件数据修改
     *
     * @param conditionModel
     * @param value
     */
    private static Object filedData(FlowConditionModel conditionModel, Object value, String jnpfKey, Object form) {
        Map<String, Object> map = JsonUtil.stringToMap(conditionModel.getData());
        value = map.get(value);
        UserEntity userEntity = conditionModel.getUserEntity();
        FlowTaskEntity flowTaskEntity = conditionModel.getFlowTaskEntity();
        try {
            try {
                List<List<String>> dataAll = JsonUtil.getJsonToBean(String.valueOf(form), List.class);
                List<String> id = new ArrayList<>();
                for (List<String> data : dataAll) {
                    id.addAll(data);
                }
                value = String.join(",", id);
            } catch (Exception e) {
                try {
                    List<String> id = new ArrayList<>();
                    List<String> dataAll = JsonUtil.getJsonToList(String.valueOf(form), String.class);
                    if (JnpfKeyConsts.CURRORGANIZE.equals(jnpfKey)) {
                        value = dataAll.stream().filter(t -> t.equals(form)).findFirst().orElse(null);
                    } else {
                        for (String data : dataAll) {
                            id.add(data);
                        }
                        value = String.join(",", id);
                    }
                } catch (Exception e1) {

                }
            }
            if (JnpfKeyConsts.CREATETIME.equals(jnpfKey)) {
                value = flowTaskEntity.getCreatorTime().getTime() + "";
            } else if (JnpfKeyConsts.CREATEUSER.equals(jnpfKey)) {
                value = flowTaskEntity.getCreatorUserId();
            } else if (JnpfKeyConsts.CURRORGANIZE.equals(jnpfKey)) {
                value = userEntity.getOrganizeId();
            } else if (JnpfKeyConsts.CURRPOSITION.equals(jnpfKey)) {
                value = userEntity.getPositionId();
            } else if (JnpfKeyConsts.MODIFYTIME.equals(jnpfKey)) {
                value = flowTaskEntity.getLastModifyTime().getTime() + "";
            } else if (JnpfKeyConsts.MODIFYUSER.equals(jnpfKey)) {
                value = flowTaskEntity.getLastModifyUserId();
            }
        } catch (Exception e) {

        }
        if (value instanceof CharSequence) {
            value = "'" + value + "'";
        }
        return value;
    }

    /**
     * 表单数据修改
     *
     * @param form
     */
    private static Object formValue(FlowConditionModel conditionModel, String jnpfKey, Object form) {
        Object result = form;
        UserEntity userEntity = conditionModel.getUserEntity();
        FlowTaskEntity flowTaskEntity = conditionModel.getFlowTaskEntity();
        try {
            try {
                List<List<String>> dataAll = JsonUtil.getJsonToBean(String.valueOf(form), List.class);
                List<String> id = new ArrayList<>();
                for (List<String> data : dataAll) {
                    id.addAll(data);
                }
                result = String.join(",", id);
            } catch (Exception e) {
                try {
                    List<String> id = new ArrayList<>();
                    List<String> dataAll = JsonUtil.getJsonToList(String.valueOf(form), String.class);
                    for (String data : dataAll) {
                        id.add(data);
                    }
                    result = String.join(",", id);
                } catch (Exception e1) {
                }
            }
            if (JnpfKeyConsts.CREATETIME.equals(jnpfKey)) {
                result = flowTaskEntity.getCreatorTime().getTime() + "";
            } else if (JnpfKeyConsts.CREATEUSER.equals(jnpfKey)) {
                result = flowTaskEntity.getCreatorUserId();
            } else if (JnpfKeyConsts.CURRORGANIZE.equals(jnpfKey)) {
                result = userEntity.getOrganizeId();
            } else if (JnpfKeyConsts.CURRPOSITION.equals(jnpfKey)) {
                result = userEntity.getPositionId();
            } else if (JnpfKeyConsts.MODIFYTIME.equals(jnpfKey)) {
                result = flowTaskEntity.getLastModifyTime().getTime() + "";
            } else if (JnpfKeyConsts.MODIFYUSER.equals(jnpfKey)) {
                result = flowTaskEntity.getLastModifyUserId();
            }
        } catch (Exception e) {
        }
        if (result instanceof CharSequence) {
            result = "'" + result + "'";
        }
        return result;
    }

    /**
     * 表达式
     */
    private static Object formula(GroupsModel properCond, Map<String, Object> data) {
        String result = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("function getNum(val) {\n" +
                    "  return isNaN(val) ? 0 : Number(val)\n" +
                    "};\n" +
                    "// 求和\n" +
                    "function SUM() {\n" +
                    "  var value = 0\n" +
                    "  for (var i = 0; i < arguments.length; i++) {\n" +
                    "    value += getNum(arguments[i])\n" +
                    "  }\n" +
                    "  return value\n" +
                    "};\n" +
                    "// 求差\n" +
                    "function SUBTRACT(num1, num2) {\n" +
                    "  return getNum(num1) - getNum(num2)\n" +
                    "};\n" +
                    "// 相乘\n" +
                    "function PRODUCT() {\n" +
                    "  var value = 1\n" +
                    "  for (var i = 0; i < arguments.length; i++) {\n" +
                    "    value = value * getNum(arguments[i])\n" +
                    "  }\n" +
                    "  return value\n" +
                    "};\n" +
                    "// 相除\n" +
                    "function DIVIDE(num1, num2) {\n" +
                    "  return getNum(num1) / (getNum(num2) === 0 ? 1 : getNum(num2))\n" +
                    "};\n" +
                    "// 获取参数的数量\n" +
                    "function COUNT() {\n" +
                    "  var value = 0\n" +
                    "  for (var i = 0; i < arguments.length; i++) {\n" +
                    "    value ++\n" +
                    "  }\n" +
                    "  return value\n" +
                    "};\n");
            String field = field(properCond.getField(), data, null);
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("js");
            String eval = builder.toString() + " var result = " + field + ";";
            scriptEngine.eval(eval);
            double d = (double) scriptEngine.get("result");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setRoundingMode(RoundingMode.UP);
            result = nf.format(d);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    /**
     * 赋值
     */
    private static Map<String, List<String>> data(Matcher matcher, Map<String, Object> dataAll) {
        Map<String, List<String>> map = new HashMap<>();
        Map<String, String> keyAll = new HashMap<>();
        while (matcher.find()) {
            String group = matcher.group().replaceAll("\\{", "").replaceAll("}", "");
            keyAll.put(group, group);
        }
        for (String id : keyAll.keySet()) {
            List<String> valueData = new ArrayList<>();
            String valueAll[] = id.split("-");
            String key = valueAll[0];
            Object childDataAll = dataAll.get(key) != null ? dataAll.get(key) : "";
            if (valueAll.length > 1) {
                String data = valueAll[1];
                if (childDataAll instanceof List) {
                    List<Map<String, Object>> childData = (List<Map<String, Object>>) childDataAll;
                    for (Map<String, Object> childDatum : childData) {
                        Object childDatas = childDatum.get(data);
                        valueData.add(childDatas + "");
                    }
                }
            } else if (valueAll.length == 1) {
                valueData.add(childDataAll + "");
            }
            map.put(id, valueData);
        }
        return map;
    }


    //---------------------------------------------------------------解析--------------------------------------------------------------------------

    /**
     * 递归外层的节点
     **/
    public static void childListAll(ChildNode childNode, List<ChildNode> chilNodeList) {
        if (childNode != null) {
            chilNodeList.add(childNode);
            boolean haschildNode = childNode.getChildNode() != null;
            if (haschildNode) {
                ChildNode nextNode = childNode.getChildNode();
                childListAll(nextNode, chilNodeList);
            }
        }
    }

    /**
     * 最外层的json
     **/
    public static void getTemplateAll(ChildNode childNode, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll) {
        List<ChildNode> chilNodeList = new ArrayList<>();
        childListAll(childNode, chilNodeList);
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            Properties properties = childNode.getProperties();
            ChildNodeList childNodeList = new ChildNodeList();
            childNodeList.setProperties(properties);
            //定时器
            DateProperties model = JsonUtil.getJsonToBean(properties, DateProperties.class);
            childNodeList.setTimer(model);
            //自定义属性
            Custom customModel = new Custom();
            customModel.setType(childNode.getType());
            customModel.setNum("0");
            customModel.setFirstId("");
            customModel.setChild(haschildNode);
            customModel.setNodeId(nodeId);
            customModel.setPrevId(prevId);
            customModel.setChildNode(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                childNodeList.setConditionType(FlowCondition.CONDITION);
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null || t.getIsBranchFlow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    customModel.setFlow(isFlow);
                    boolean branchFlow = conditionNodes.stream().filter(t -> t.getIsBranchFlow() != null).count() > 0;
                    customModel.setBranchFlow(branchFlow);
                    childNodeList.setConditionType(branchFlow ? FlowCondition.ISBRANCHFLOW : FlowCondition.INTERFLOW);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    customModel.setFlowId(String.join(",", flowIdAll));
                }
            }
            childNodeList.setCustom(customModel);
            childNodeListAll.add(childNodeList);
            String firstId = "";
            if (haschildNode) {
                firstId = childNode.getChildNode().getNodeId();
            }
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 递归子节点的子节点
     **/
    private static void getchildNode(ChildNode parentChildNodeTest, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        ChildNode childNode = parentChildNodeTest.getChildNode();
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            Properties properModel = childNode.getProperties();
            ChildNodeList childNodeList = new ChildNodeList();
            childNodeList.setProperties(properModel);
            //定时器
            DateProperties model = JsonUtil.getJsonToBean(properModel, DateProperties.class);
            childNodeList.setTimer(model);
            //自定义属性
            Custom customModel = new Custom();
            customModel.setType(childNode.getType());
            boolean isFirst = chilNodeList.stream().filter(t -> t.getNodeId().equals(nodeId)).count() > 0;
            customModel.setNum(isFirst ? "0" : "1");
            customModel.setFirstId(firstId);
            if (isFirst) {
                customModel.setFirstId(haschildNode ? childNode.getChildNode().getNodeId() : "");
            }
            customModel.setChild(haschildNode);
            customModel.setNodeId(nodeId);
            customModel.setPrevId(prevId);
            customModel.setChildNode(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                childNodeList.setConditionType(FlowCondition.CONDITION);
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null || t.getIsBranchFlow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    customModel.setFlow(isFlow);
                    boolean branchFlow = conditionNodes.stream().filter(t -> t.getIsBranchFlow() != null).count() > 0;
                    customModel.setBranchFlow(branchFlow);
                    childNodeList.setConditionType(branchFlow ? FlowCondition.ISBRANCHFLOW : FlowCondition.INTERFLOW);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    customModel.setFlowId(String.join(",", flowIdAll));
                }
            }
            childNodeList.setCustom(customModel);
            childNodeListAll.add(childNodeList);
            //条件或者分流递归
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            //子节点递归
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 条件、分流递归
     **/
    private static void conditionList(ChildNode childNode, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        List<ChildNode> conditionNodes = childNode.getConditionNodes();
        if (conditionNodes.size() > 0) {
            //判断是条件还是分流
            //判断父节点是否还有子节点,有的话替换子节点数据
            ChildNode childNodeModel = childNode.getChildNode();
            if (childNodeModel != null) {
                firstId = childNodeModel.getNodeId();
            } else {
                ChildNode nodes = chilNodeList.stream().filter(t -> t.getNodeId().equals(childNode.getNodeId())).findFirst().orElse(null);
                if (nodes != null) {
                    if (nodes.getChildNode() != null) {
                        firstId = childNode.getChildNode().getNodeId();
                    } else {
                        firstId = "";
                    }
                }
            }
            for (ChildNode node : conditionNodes) {
                boolean conditionType = (node.getIsInterflow() == null && node.getIsBranchFlow() == null) ? true : false;
                if (conditionType) {
                    getCondition(node, firstId, childNodeListAll, conditionListAll, chilNodeList);
                } else {
                    getConditonFlow(node, firstId, childNodeListAll, conditionListAll, chilNodeList);
                }
            }
        }
    }

    /**
     * 条件递归
     **/
    private static void getCondition(ChildNode childNode, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean hasChildNode = childNode.getChildNode() != null;
            boolean hasConditionNodes = childNode.getConditionNodes() != null;
            boolean hasSwerveNodeNodes = StringUtil.isNotEmpty(childNode.getProperties().getSwerveNode());
            boolean isDefault = childNode.getProperties().getIsDefault() != null ? childNode.getProperties().getIsDefault() : false;
            ConditionList conditionList = JsonUtil.getJsonToBean(childNode.getProperties(), ConditionList.class);
            conditionList.setNodeId(nodeId);
            conditionList.setPrevId(prevId);
            conditionList.setChild(hasChildNode);
            conditionList.setSwerve(hasSwerveNodeNodes);
            conditionList.setChildNodeId(hasChildNode == true ? childNode.getChildNode().getNodeId() : "");
            conditionList.setIsDefault(isDefault);
            conditionList.setFirstId(firstId);
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasConditionNodes) {
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null || t.getIsBranchFlow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    conditionList.setFlow(isFlow);
                    boolean branchFlow = conditionNodes.stream().filter(t -> t.getIsBranchFlow() != null).count() > 0;
                    conditionList.setBranchFlow(branchFlow);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    conditionList.setFlowId(String.join(",", flowIdAll));
                }
            }
            conditionListAll.add(conditionList);
            //递归条件、分流
            if (hasConditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            //递归子节点
            if (hasChildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 条件递归
     **/
    private static void getConditonFlow(ChildNode childNode, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            Properties properties = childNode.getProperties();
            ChildNodeList childNodeList = new ChildNodeList();
            childNodeList.setProperties(properties);
            //定时器
            DateProperties model = JsonUtil.getJsonToBean(properties, DateProperties.class);
            childNodeList.setTimer(model);
            //自定义属性
            Custom customModel = new Custom();
            customModel.setType(childNode.getType());
            customModel.setNum("1");
            customModel.setFirstId(firstId);
            customModel.setChild(haschildNode);
            customModel.setChildNode(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            customModel.setNodeId(nodeId);
            customModel.setPrevId(prevId);
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                childNodeList.setConditionType(FlowCondition.CONDITION);
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null || t.getIsBranchFlow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    customModel.setFlow(isFlow);
                    boolean branchFlow = conditionNodes.stream().filter(t -> t.getIsBranchFlow() != null).count() > 0;
                    customModel.setBranchFlow(branchFlow);
                    childNodeList.setConditionType(branchFlow ? FlowCondition.ISBRANCHFLOW : FlowCondition.INTERFLOW);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    customModel.setFlowId(String.join(",", flowIdAll));
                }
            }
            childNodeList.setCustom(customModel);
            childNodeListAll.add(childNodeList);
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 替换文本值
     *
     * @param content
     * @param data
     * @return
     */
    public static String field(String content, Map<String, Object> data, String type) {
        String pattern = "[{]([^}]+)[}]";
        Pattern patternList = Pattern.compile(pattern);
        Matcher matcher = patternList.matcher(content);
        Map<String, List<String>> parameterMap = data(matcher, data);
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotEmpty(type)) {
            Map<String, String> datas = new HashMap<>();
            for (String key : parameterMap.keySet()) {
                datas.put(key, data.get(key) != null ? String.valueOf(data.get(key)) : "");
            }
            result.putAll(datas);
        } else {
            Map<String, Object> dataAll = new HashMap<>();
            for (String key : parameterMap.keySet()) {
                StringJoiner joiner = new StringJoiner(",");
                List<String> list = parameterMap.get(key);
                for (String id : list) {
                    joiner.add("'" + id + "'");
                }
                String value = joiner.toString();
                if (list.size() > 1) {
                    value = "SUM(" + joiner.toString() + ")";
                }
                dataAll.put(key, value);
            }
            result.putAll(dataAll);
        }
        StringSubstitutor strSubstitutor = new StringSubstitutor(result, "{", "}");
        String field = strSubstitutor.replace(content);
        return field;
    }

}
