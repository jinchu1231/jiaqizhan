package jnpf.integrate.util;

import cn.hutool.core.util.ObjectUtil;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.model.datainterface.DataInterfaceInvokeModel;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.SentMessageUtil;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.database.model.superQuery.SuperQueryJsonModel;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.integrate.entity.IntegrateEntity;
import jnpf.integrate.entity.IntegrateNodeEntity;
import jnpf.integrate.entity.IntegrateTaskEntity;
import jnpf.integrate.model.childnode.IntegrateChildNode;
import jnpf.integrate.model.childnode.IntegrateMsgModel;
import jnpf.integrate.model.childnode.IntegrateParamModel;
import jnpf.integrate.model.childnode.IntegrateProperties;
import jnpf.integrate.model.childnode.IntegrateTemplateModel;
import jnpf.integrate.model.childnode.TransferModel;
import jnpf.integrate.model.integrate.IntegratePagination;
import jnpf.integrate.model.nodeJson.IntegrateChildNodeList;
import jnpf.integrate.model.nodeJson.IntegrateChildNodeModel;
import jnpf.integrate.service.IntegrateNodeService;
import jnpf.integrate.service.IntegrateService;
import jnpf.integrate.service.IntegrateTaskService;
import jnpf.message.model.SentMessageForm;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualDevInfoService;
import jnpf.onlinedev.service.VisualDevListService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.AuthUtil;
import jnpf.util.FlowFormConstant;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IntegrateUtil {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private SentMessageUtil sentMessageUtil;
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private VisualDevListService visualDevListService;
    @Autowired
    private IntegrateService integrateService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualDevInfoService visualDevInfoService;
    @Autowired
    private IntegrateTaskService integrateTaskService;
    @Autowired
    private IntegrateNodeService integrateNodeService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;

    //------------------------------------------事件------------------------------------------------------

    @Async
    public void deleteDataList(List<VisualdevModelDataInfoVO> dataInfoVOList, List<String> id, UserInfo userInfo) {
        for (VisualdevModelDataInfoVO infoVO : dataInfoVOList) {
            String integrateId = infoVO.getIntegrateId();
            List<Map<String, Object>> dataList = JsonUtil.getJsonToListMap(infoVO.getData()).stream().filter(t -> id.contains(String.valueOf(t.get("id")))).collect(Collectors.toList());
            integrate(integrateId, dataList, userInfo);
        }
    }

    @Async
    public void dataAsyncList(String modelId, Integer trigger, List<String> dataId, UserInfo userInfo) {
        List<VisualdevModelDataInfoVO> resultData = new ArrayList<>();
        List<IntegrateEntity> list = integrateList(modelId, trigger);
        for (IntegrateEntity entity : list) {
            //数据
            IntegrateChildNode childNode = JsonUtil.getJsonToBean(entity.getTemplateJson(), IntegrateChildNode.class);
            IntegrateChildNodeList childNodeModel = JsonUtil.getJsonToBean(childNode, IntegrateChildNodeList.class);
            List<Map<String, Object>> dataList = dataList(childNodeModel, new HashMap<>(16), dataId);
            VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
            vo.setData(JsonUtil.getObjectToString(dataList));
            vo.setIntegrateId(entity.getId());
            resultData.add(vo);
        }
        integrates(resultData, userInfo);
    }

    public List<VisualdevModelDataInfoVO> dataList(String modelId, Integer trigger, List<String> dataId) {
        List<VisualdevModelDataInfoVO> resultData = new ArrayList<>();
        List<IntegrateEntity> list = integrateList(modelId, trigger);
        for (IntegrateEntity entity : list) {
            //数据
            IntegrateChildNode childNode = JsonUtil.getJsonToBean(entity.getTemplateJson(), IntegrateChildNode.class);
            IntegrateChildNodeList childNodeModel = JsonUtil.getJsonToBean(childNode, IntegrateChildNodeList.class);
            List<Map<String, Object>> dataList = dataList(childNodeModel, new HashMap<>(16), dataId);
            VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
            vo.setData(JsonUtil.getObjectToString(dataList));
            vo.setIntegrateId(entity.getId());
            resultData.add(vo);
        }
        return resultData;
    }

    public void integrates(List<VisualdevModelDataInfoVO> dataInfoVOList, UserInfo userInfo) {
        for (VisualdevModelDataInfoVO infoVO : dataInfoVOList) {
            String integrateId = infoVO.getIntegrateId();
            List<Map<String, Object>> dataList = JsonUtil.getJsonToListMap(infoVO.getData());
            integrate(integrateId, dataList, userInfo);
        }
    }

    private List<IntegrateEntity> integrateList(String modelId, Integer trigger) {
        IntegratePagination pagination = new IntegratePagination();
        pagination.setFormId(modelId);
        pagination.setType(1);
        pagination.setTrigger(trigger);
        pagination.setEnabledMark(1);
        List<IntegrateEntity> list = integrateService.getList(pagination, false);
        return list;
    }

    //------------------------------------------定时------------------------------------------------------

    //重试
    public void integrate(String taskId, String parentId, String taskNodeId) {
        IntegrateTaskEntity taskEntity = integrateTaskService.getInfo(taskId);
        if (taskEntity != null) {
            Date time = new Date();
            IntegrateNodeEntity info = integrateNodeService.getInfo(taskNodeId);
            String retryNodeCode = info != null ? info.getNodeCode() : "";
            Map<String, Object> objectMap = JsonUtil.stringToMap(taskEntity.getData());
            IntegrateEntity entity = integrateService.getInfo(taskEntity.getIntegrateId());
            boolean isParentId = !"0".equals(parentId);
            String json = isParentId ? entity.getTemplateJson() : taskEntity.getTemplateJson();
            IntegrateChildNode childNode = JsonUtil.getJsonToBean(json, IntegrateChildNode.class);
            List<IntegrateChildNodeList> childNodeListAll = new ArrayList<>();
            childList(childNode, childNodeListAll);
            if (isParentId) {
                IntegrateTaskEntity parentTask = integrateTaskService.getInfo(parentId);
                taskEntity.setId(RandomUtil.uuId());
                taskEntity.setParentTime(parentTask.getExecutionTime());
                taskEntity.setParentId(parentTask.getId());
                taskEntity.setExecutionTime(new Date());
                taskEntity.setProcessId(taskEntity.getId());
                integrateTaskService.save(taskEntity);
            }
            List<IntegrateNodeEntity> nodeList = new ArrayList<>();
            List<IntegrateChildNodeList> nodeListAll = new ArrayList<>(childNodeListAll);
            for (int k = 0; k < nodeListAll.size(); k++) {
                IntegrateChildNodeList childNodeList = nodeListAll.get(k);
                childNodeList.setIntegrateType(taskEntity.getType());
                IntegrateProperties childProperties = childNodeList.getProperties();
                IntegrateNodeEntity nodeEntity = new IntegrateNodeEntity();
                nodeEntity.setResultType(0);
                nodeEntity.setParentId(StringUtil.isNotEmpty(taskNodeId) ? taskNodeId : "0");
                nodeEntity.setNodeType(childNodeList.getType());
                nodeEntity.setNodeNext(childNodeList.getNextId());
                nodeEntity.setStartTime(time);
                nodeEntity.setEndTime(time);
                nodeEntity.setTaskId(taskEntity.getId());
                nodeEntity.setNodeCode(childNodeList.getNodeId());
                nodeEntity.setFormId(childProperties.getFormId());
                nodeEntity.setNodeName(childProperties.getTitle());
                nodeEntity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                nodeEntity.setId(RandomUtil.uuId());
                nodeEntity.setSortCode(Long.parseLong(k + ""));
                nodeList.add(nodeEntity);
            }
            try {
                IntegrateChildNodeModel childNodeModel = IntegrateChildNodeModel.builder().data(objectMap).dataListAll(new ArrayList<>())
                        .nodeList(nodeList).node(childNode.getNodeId()).entity(entity).retryNodeCode(retryNodeCode).userInfo(userProvider.get()).build();
                childNode(childNodeModel);
            } catch (Exception e) {
                msg(entity, new HashMap<>(objectMap), nodeList, "end", false, userProvider.get());
            }
            boolean failSum = nodeList.stream().filter(t -> t.getResultType() == 0).count() > 0;
            taskEntity.setResultType(failSum ? 0 : 1);
            integrateTaskService.update(taskEntity.getId(), taskEntity);
        }
    }

    //定时执行
    public void integrate(String integrateId, UserInfo userInfo) throws WorkFlowException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        IntegrateEntity entity = integrateService.getInfo(integrateId);
        if (entity != null) {
            IntegrateChildNode childNode = JsonUtil.getJsonToBean(entity.getTemplateJson(), IntegrateChildNode.class);
            IntegrateChildNode getDataChildNode = childNode.getChildNode();
            IntegrateChildNodeList dataListNodeMode = JsonUtil.getJsonToBean(getDataChildNode, IntegrateChildNodeList.class);
            IntegrateProperties properties = dataListNodeMode.getProperties();
            Integer formType = properties.getFormType();
            //远端节点
            if (Objects.equals(formType, 3)) {
                dataListNodeMode.getProperties().setTemplateJson(dataListNodeMode.getProperties().getInterfaceTemplateJson());
                ActionResult result = interfaceTemplateJson(dataListNodeMode, new HashMap<>(), userInfo);
                if (result.getData() instanceof List) {
                    dataList.addAll((List) result.getData());
                }
            } else {
                dataList.addAll(dataList(dataListNodeMode, new HashMap<>(16), new ArrayList<>()));
            }
            integrate(integrateId, dataList, userInfo);
        }
    }

    //集成执行逻辑
    private void integrate(String integrateId, List<Map<String, Object>> dataList, UserInfo userInfo) {
        IntegrateEntity entity = integrateService.getInfo(integrateId);
        if (entity != null) {
            Date time = new Date();
            IntegrateChildNode childNode = JsonUtil.getJsonToBean(entity.getTemplateJson(), IntegrateChildNode.class);
            IntegrateChildNode getDataChildNode = childNode.getChildNode();
            IntegrateChildNodeList dataListNodeMode = JsonUtil.getJsonToBean(getDataChildNode, IntegrateChildNodeList.class);
            dataListNodeMode.setStartTime(time);
            List<IntegrateChildNodeList> childNodeListAll = new ArrayList<>();
            childList(childNode, childNodeListAll);
            IntegrateChildNodeList startNode = childNodeListAll.stream().filter(t -> "start".equals(t.getType())).findFirst().orElse(null);
            if (startNode != null) {
                for (int i = 0; i < dataList.size(); i++) {
                    Map<String, Object> objectMap = dataList.get(i);
                    String id = String.valueOf(objectMap.get("id"));
                    List<IntegrateNodeEntity> nodeList = new ArrayList<>();
                    IntegrateTaskEntity taskEntity = new IntegrateTaskEntity();
                    taskEntity.setIntegrateId(entity.getId());
                    taskEntity.setType(entity.getType());
                    taskEntity.setTemplateJson(entity.getTemplateJson());
                    taskEntity.setDataId(id);
                    taskEntity.setParentId("0");
                    taskEntity.setResultType(1);
                    taskEntity.setExecutionTime(new Date());
                    taskEntity.setId(RandomUtil.uuId());
                    taskEntity.setData(JsonUtil.getObjectToString(objectMap));
                    taskEntity.setProcessId(taskEntity.getId());
                    integrateTaskService.save(taskEntity);
                    //节点
                    List<IntegrateChildNodeList> nodeListAll = new ArrayList<>(childNodeListAll);
                    for (int k = 0; k < nodeListAll.size(); k++) {
                        IntegrateChildNodeList childNodeList = nodeListAll.get(k);
                        childNodeList.setIntegrateType(taskEntity.getType());
                        IntegrateProperties childProperties = childNodeList.getProperties();
                        IntegrateNodeEntity nodeEntity = new IntegrateNodeEntity();
                        nodeEntity.setResultType(0);
                        nodeEntity.setParentId("0");
                        nodeEntity.setNodeType(childNodeList.getType());
                        nodeEntity.setNodeNext(childNodeList.getNextId());
                        nodeEntity.setStartTime(time);
                        nodeEntity.setEndTime(time);
                        nodeEntity.setTaskId(taskEntity.getId());
                        nodeEntity.setNodeCode(childNodeList.getNodeId());
                        nodeEntity.setFormId(childProperties.getFormId());
                        nodeEntity.setNodeName(childProperties.getTitle());
                        nodeEntity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                        nodeEntity.setSortCode(Long.parseLong(k + ""));
                        nodeList.add(nodeEntity);
                    }
                    String msg = "";
                    try {
                        IntegrateChildNodeModel childNodeModel = IntegrateChildNodeModel.builder().dataListAll(dataList).data(objectMap)
                                .nodeList(nodeList).node(startNode.getNodeId()).entity(entity).retryNodeCode("").userInfo(userInfo).build();
                        childNode(childNodeModel);
                    } catch (Exception e) {
                        msg = e.getMessage();
                    }
                    boolean failSum = nodeList.stream().filter(t -> t.getResultType() == 0).count() > 0;
                    taskEntity.setResultType(failSum ? 0 : 1);
                    integrateTaskService.update(taskEntity.getId(), taskEntity);
                    if (StringUtil.isNotEmpty(msg)) {
                        msg(entity, new HashMap<>(objectMap), nodeList, "end", false, userInfo);
//                        throw new WorkFlowException(msg);
                    }
                }
            }
        }
    }

    private void childList(IntegrateChildNode childNodeAll, List<IntegrateChildNodeList> listAll) {
        if (childNodeAll != null) {
            IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(childNodeAll, IntegrateChildNodeList.class);
            boolean isNext = childNodeAll.getChildNode() != null;
            String nextId = isNext ? childNodeAll.getChildNode().getNodeId() : "end";
            childNodeList.setNextId(nextId);
            listAll.add(childNodeList);
            if (isNext) {
                childList(childNodeAll.getChildNode(), listAll);
            }
            if ("end".equals(nextId)) {
                IntegrateChildNodeList endChildNodeList = listAll.stream().filter(t -> "start".equals(t.getType())).findFirst().orElse(null);
                IntegrateChildNodeList endNode = JsonUtil.getJsonToBean(endChildNodeList, IntegrateChildNodeList.class);
                endNode.setNodeId(nextId);
                endNode.setType(nextId);
                endNode.setNextId("");
                endNode.getProperties().setTitle("结束");
                listAll.add(endNode);
            }
        }
    }

    private void childNode(IntegrateChildNodeModel childNodeModel) throws WorkFlowException {
        Map<String, Object> data = childNodeModel.getData();
        List<Map<String, Object>> dataList = childNodeModel.getDataListAll();
        List<IntegrateNodeEntity> nodeList = childNodeModel.getNodeList();
        String node = childNodeModel.getNode();
        IntegrateEntity entity = childNodeModel.getEntity();
        String retryNodeCode = childNodeModel.getRetryNodeCode();
        UserInfo userInfo = childNodeModel.getUserInfo();
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            String nodeType = nodeEntity.getNodeType();
            String nodeCode = nodeEntity.getNodeCode();
            String[] nextCodeAll = nodeEntity.getNodeNext().split(",");
            switch (nodeType) {
                case "addData":
                    addData(new HashMap<>(data), nodeList, nodeCode, retryNodeCode);
                    break;
                case "updateData":
                    updateData(new HashMap<>(data), nodeList, nodeCode, retryNodeCode);
                    break;
                case "deleteData":
                    deleteData(new HashMap<>(data), new ArrayList<>(dataList), nodeList, nodeCode, retryNodeCode);
                    break;
                case "message":
                    message(new HashMap<>(data), nodeList, nodeCode, userInfo);
                    break;
                case "dataInterface":
                    dataInterface(new HashMap<>(data), nodeList, nodeCode, userInfo, retryNodeCode);
                    break;
                case "getData":
                    getData(entity, nodeList, nodeCode);
                    break;
                case "launchFlow":
                    launchFlow(new HashMap<>(data), nodeList, nodeCode, retryNodeCode);
                    break;
                case "end":
                case "start":
                    msg(entity, new HashMap<>(data), nodeList, nodeCode, true, userInfo);
                    break;
                default:
                    break;
            }
            for (String nextCode : nextCodeAll) {
                childNodeModel.setNode(nextCode);
                childNode(childNodeModel);
            }
        }
    }

    //-----------------------------------事件-----------------------------------------------

    private void getData(IntegrateEntity entity, List<IntegrateNodeEntity> nodeList, String node) {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            nodeEntity.setResultType(1);
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            if (list.size() == 0) {
                integrateNodeService.create(nodeEntity);
            }
        }
    }

    private void msg(IntegrateEntity entity, Map<String, Object> data, List<IntegrateNodeEntity> nodeList, String node, boolean isAdd, UserInfo userInfo) {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            String msg = "";
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            boolean isEnd = "end".equals(node);
            String templeId = isEnd ? "PZXTJC001" : "PZXTJC002";
            if (list.size() == 0) {
                nodeEntity.setStartTime(isEnd ? new Date() : nodeEntity.getStartTime());
                boolean failSum = nodeList.stream().filter(t -> !Objects.equals(t.getNodeCode(), "end") && Objects.equals(t.getResultType(), 0)).count() > 0 || !isEnd;
                IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                IntegrateProperties properties = childNodeList.getProperties();
                IntegrateMsgModel msgConfig = isEnd ? properties.getFailMsgConfig() : properties.getStartMsgConfig();
                Integer on = msgConfig.getOn();
                boolean acquiesce = on == 3;
                String msgId = on == 0 ? "" : acquiesce ? templeId : msgConfig.getMsgId();
                List<String> msgUserType = properties.getMsgUserType();
                List<String> msgUserIds = properties.getMsgUserIds();
                Set<String> userIdList = new HashSet<>();
                for (String type : msgUserType) {
                    switch (type) {
                        case "1":
                            userIdList.add(entity.getCreatorUserId());
                            break;
                        case "2":
                            List<String> adminList = userService.getAdminList().stream().map(UserEntity::getId).collect(Collectors.toList());
                            userIdList.addAll(adminList);
                            break;
                        case "3":
                            List<String> userList = userService.getUserIdList(msgUserIds,null);
                            userIdList.addAll(userList);
                            break;
                        default:
                            break;
                    }
                }
                Map<String, Object> dataMap = new HashMap() {{
                    put("@Title", entity.getFullName());
                    put("@CreatorUserName", "");
                }};
                List<IntegrateTemplateModel> templateJson = msgConfig.getTemplateJson();
                try {
                    if (StringUtil.isNotEmpty(msgId) && userIdList.size() > 0 && failSum) {
                        Map<String, Object> parameterMap = acquiesce ? dataMap : templateJson(templateJson, data);
                        message(msgId, new ArrayList<>(userIdList), parameterMap, userInfo);
                    }
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }
                nodeEntity.setEndTime(isEnd ? new Date() : nodeEntity.getEndTime());
                if (isAdd) {
                    integrateNodeService.create(nodeEntity);
                }
            }
        }
    }

    private void addData(Map<String, Object> data, List<IntegrateNodeEntity> nodeList, String node, String retryNodeCode) throws WorkFlowException {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            boolean isRetry = nodeEntity.getNodeCode().equals(retryNodeCode);
            if (list.size() == 0 || isRetry) {
                nodeEntity.setStartTime(new Date());
                IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                IntegrateProperties properties = childNodeList.getProperties();
                boolean isAdd = Objects.equals(childNodeList.getIntegrateType(), 1);
                if (!isAdd) {
                    List<Map<String, Object>> dataList = dataList(childNodeList, data, new ArrayList<>());
                    Integer addRule = properties.getAddRule();
                    isAdd = (dataList.size() > 0 && addRule == 1) || dataList.size() == 0;
                }
                String msg = "";
                try {
                    if (isAdd) {
                        String formId = properties.getFormId();
                        String flowId = properties.getFlowId();
                        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(formId);
                        List<TransferModel> transferList = properties.getTransferList();
                        Map<String, Object> saveData = formData(data, transferList);
                        saveData.put(FlowFormConstant.FLOWID, flowId);
                        visualdevModelDataService.visualCreate(visualdevEntity, saveData);
                    }
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }
                nodeEntity.setEndTime(new Date());
                if (isRetry) {
                    integrateNodeService.update(nodeEntity.getTaskId(), nodeEntity.getNodeCode());
                }
                integrateNodeService.create(nodeEntity);
                if (StringUtil.isNotEmpty(msg)) {
                    throw new WorkFlowException(msg);
                }
            }
        }
    }

    private void updateData(Map<String, Object> data, List<IntegrateNodeEntity> nodeList, String node, String retryNodeCode) throws WorkFlowException {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            boolean isRetry = nodeEntity.getNodeCode().equals(retryNodeCode);
            if (list.size() == 0 || isRetry) {
                nodeEntity.setStartTime(new Date());
                IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                IntegrateProperties properties = childNodeList.getProperties();
                String formId = properties.getFormId();
                String flowId = properties.getFlowId();
                VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(formId);
                List<TransferModel> transferList = properties.getTransferList();
                List<Map<String, Object>> dataList = dataList(childNodeList, data, new ArrayList<>());
                Integer unFoundRule = properties.getUnFoundRule();
                String msg = "";
                boolean isAdd = dataList.size() == 0 && unFoundRule == 1;
                if (isAdd) {
                    Map<String, Object> saveData = formData(data, transferList);
                    saveData.put(FlowFormConstant.FLOWID, flowId);
                    dataList.add(saveData);
                }
                try {
                    for (Map<String, Object> objectMap : dataList) {
                        if (isAdd) {
                            visualdevModelDataService.visualCreate(visualdevEntity, objectMap);
                        } else {
                            String id = String.valueOf(objectMap.get("id"));
                            Map<String, Object> saveData = formData(data, objectMap, transferList);
                            Map<String, Object> dataObject = new HashMap<>(saveData);
                            visualdevModelDataService.visualUpdate(visualdevEntity, dataObject, id);
                        }
                    }
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }
                nodeEntity.setEndTime(new Date());
                if (isRetry) {
                    integrateNodeService.update(nodeEntity.getTaskId(), nodeEntity.getNodeCode());
                }
                integrateNodeService.create(nodeEntity);
                if (StringUtil.isNotEmpty(msg)) {
                    throw new WorkFlowException(msg);
                }
            }
        }
    }

    private void deleteData(Map<String, Object> data, List<Map<String, Object>> dataListAll, List<IntegrateNodeEntity> nodeList, String node, String retryNodeCode) throws WorkFlowException {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            boolean isRetry = nodeEntity.getNodeCode().equals(retryNodeCode);
            if (list.size() == 0 || isRetry) {
                nodeEntity.setStartTime(new Date());
                IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                IntegrateProperties properties = childNodeList.getProperties();
                String formId = properties.getFormId();
                //条件的数据
                Integer deleteRule = Objects.equals(childNodeList.getIntegrateType(), 1) ? 1 : properties.getDeleteRule();
                IntegrateChildNodeList dataChildNode = new IntegrateChildNodeList();
                dataChildNode.getProperties().setFormId(formId);
                boolean delete = Objects.equals(deleteRule, 1);
                List<Map<String, Object>> dataList = dataList(delete ? childNodeList : dataChildNode, data, new ArrayList<>());
                List<String> idList = dataList.stream().map(t -> String.valueOf(t.get("id"))).collect(Collectors.toList());
                List<String> deleteList = new ArrayList<>();
                if (delete) {
                    deleteList.addAll(idList);
                } else {
                    List<String> addList = new ArrayList<>();
                    for (Map<String, Object> objectMap : dataListAll) {
                        addList.addAll(dataList(JsonUtil.getJsonToBean(childNodeList, IntegrateChildNodeList.class), objectMap, new ArrayList<>()).stream().map(t -> String.valueOf(t.get("id"))).collect(Collectors.toList()));
                    }
                    idList.removeAll(addList);
                    deleteList.addAll(idList);
                }
                String msg = "";
                try {
                    if (deleteList.size() > 0) {
                        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(formId);
                        visualdevModelDataService.visualDelete(visualdevEntity, deleteList);
                    }
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }
                nodeEntity.setEndTime(new Date());
                if (isRetry) {
                    integrateNodeService.update(nodeEntity.getTaskId(), nodeEntity.getNodeCode());
                }
                integrateNodeService.create(nodeEntity);
                if (StringUtil.isNotEmpty(msg)) {
                    throw new WorkFlowException(msg);
                }
            }
        }
    }

    private void message(Map<String, Object> data, List<IntegrateNodeEntity> nodeList, String node, UserInfo userInfo) throws WorkFlowException {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            String msg = "";
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            if (list.size() == 0) {
                nodeEntity.setStartTime(new Date());
                IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                IntegrateProperties properties = childNodeList.getProperties();
                List<String> userIdList = userService.getUserIdList(properties.getMsgUserIds(), null);
                String msgId = properties.getMsgId();
                List<IntegrateTemplateModel> templateJson = properties.getTemplateJson();
                try {
                    Map<String, Object> parameterMap = templateJson(templateJson, data);
                    if (userIdList.size() > 0) {
                        message(msgId, userIdList, parameterMap, userInfo);
                    }
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }
                nodeEntity.setEndTime(new Date());
                integrateNodeService.create(nodeEntity);
                if (StringUtil.isNotEmpty(msg)) {
                    throw new WorkFlowException(msg);
                }
            }
        }
    }

    private void dataInterface(Map<String, Object> data, List<IntegrateNodeEntity> nodeList, String node, UserInfo userInfo, String retryNodeCode) throws WorkFlowException {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            String msg = "";
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            boolean isRetry = nodeEntity.getNodeCode().equals(retryNodeCode);
            if (list.size() == 0 || isRetry) {
                nodeEntity.setStartTime(new Date());
                try {
                    IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                    interfaceTemplateJson(childNodeList, data, userInfo);
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }
                nodeEntity.setEndTime(new Date());
                if (isRetry) {
                    integrateNodeService.update(nodeEntity.getTaskId(), nodeEntity.getNodeCode());
                }
                integrateNodeService.create(nodeEntity);
                if (StringUtil.isNotEmpty(msg)) {
                    throw new WorkFlowException(msg);
                }
            }
        }
    }

    public Map<String, Object> formData(Map<String, Object> formData, List<TransferModel> transferList) throws WorkFlowException {
        return formData(formData, new HashMap<>(), transferList);
    }

    public Map<String, Object> formData(Map<String, Object> formData, Map<String, Object> resultData, List<TransferModel> transferList) throws WorkFlowException {
        Map<String, Object> result = new HashMap<>(resultData);
        Map<String, Object> oldData = new HashMap<>(formData);
        Set<String> msg = new HashSet<>();
        for (TransferModel assignMode : transferList) {
            //子表处理规则
            Boolean required = assignMode.getRequired();
            String targetFieldLabel = assignMode.getTargetFieldLabel();
            String sourceType = assignMode.getSourceType();
            boolean isData = "2".equals(sourceType);
            String parentField = assignMode.getSourceValue();
            String[] parentFieldList = isData ? new String[]{parentField} : parentField.split("-");
            String childField = assignMode.getTargetField();
            String[] childFieldList = childField.split("-");
            Object childData = isData ? parentField : "@formId".equals(parentField) ? formData.get("id") : formData.get(parentField);
            if (childFieldList.length > 1) {
                List<Map<String, Object>> childMapAll = new ArrayList<>();
                if (result.get(childFieldList[0]) instanceof List) {
                    List<Map<String, Object>> childList = (List<Map<String, Object>>) result.get(childFieldList[0]);
                    for (Map<String, Object> objectMap : childList) {
                        Map<String, Object> childMap = new HashMap<>(objectMap);
                        childMapAll.add(childMap);
                    }
                }
                if (parentFieldList.length > 1) {
                    if (oldData.get(parentFieldList[0]) instanceof List) {
                        List<Map<String, Object>> parentList = (List<Map<String, Object>>) oldData.get(parentFieldList[0]);
                        int num = parentList.size() - childMapAll.size();
                        for (int i = 0; i < num; i++) {
                            childMapAll.add(new HashMap<>());
                        }
                        for (int i = 0; i < parentList.size(); i++) {
                            Map<String, Object> parentMap = parentList.get(i);
                            Map<String, Object> childMap = childMapAll.get(i);
                            if (required && ObjectUtil.isEmpty(parentMap.get(parentFieldList[1]))) {
                                msg.add(targetFieldLabel);
                            }
                            childMap.put(childFieldList[1], parentMap.get(parentFieldList[1]));
                        }
                    }
                } else {
                    if (1 > childMapAll.size()) {
                        childMapAll.add(new HashMap<>());
                    }
                    Map<String, Object> childMap = childMapAll.get(0);
                    if (required && ObjectUtil.isEmpty(childData)) {
                        msg.add(targetFieldLabel);
                    }
                    childMap.put(childFieldList[1], childData);
                }
                result.put(childFieldList[0], childMapAll);
            } else {
                if (parentFieldList.length > 1) {
                    if (oldData.get(parentFieldList[0]) instanceof List) {
                        List<Map<String, Object>> parentList = (List<Map<String, Object>>) oldData.get(parentFieldList[0]);
                        for (int i = 0; i < parentList.size(); i++) {
                            Map<String, Object> parentMap = parentList.get(i);
                            if (i == 0) {
                                childData = parentMap.get(parentFieldList[1]);
                            }
                        }
                    }
                }
                if (required && ObjectUtil.isEmpty(childData)) {
                    msg.add(targetFieldLabel);
                }
                result.put(childField, childData);
            }
        }
        errRequiredMsg(msg);
        return result;
    }

    private List<Map<String, Object>> dataList(IntegrateChildNodeList childNodeList, Map<String, Object> data, List<String> dataId) {
        IntegrateProperties properties = childNodeList.getProperties();
        String formId = properties.getFormId();
        Integer formType = properties.getFormType();
        List<SuperQueryJsonModel> ruleList = properties.getRuleList();
        for (SuperQueryJsonModel superQueryJsonModel : ruleList) {
            List<FieLdsModel> groups = superQueryJsonModel.getGroups();
            for (FieLdsModel fieLdsModel : groups) {
                boolean valueType = "1".equals(fieLdsModel.getFieldValueType());
                String fieldValue = data.get(fieLdsModel.getFieldValue()) != null ? String.valueOf(data.get(fieLdsModel.getFieldValue())) : null;
                if ("@formId".equals(fieLdsModel.getFieldValue())) {
                    fieldValue = String.valueOf(data.get("id"));
                }
                fieLdsModel.setFieldValue(valueType ? fieldValue : fieLdsModel.getFieldValue());
            }
        }
        String ruleMatchLogic = properties.getRuleMatchLogic();
        SuperJsonModel superJsonModel = new SuperJsonModel();
        superJsonModel.setConditionList(ruleList);
        superJsonModel.setMatchLogic(StringUtil.isNotEmpty(ruleMatchLogic) ? ruleMatchLogic : superJsonModel.getMatchLogic());
        PaginationModel paginationModel = new PaginationModel();
        paginationModel.setPageSize(10000L);
        paginationModel.setSuperQueryJson(ruleList.size() > 0 ? JsonUtil.getObjectToString(superJsonModel) : "");
        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(formId);
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
        List<String> idAll = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        try {
            List<Map<String, Object>> dataList = visualDevListService.getDataList(visualJsonModel, paginationModel);
            idList.addAll(dataList.stream().map(t -> String.valueOf(t.get("id"))).collect(Collectors.toList()));
        } catch (Exception e) {
        }
        List<String> intersection = idList.stream().filter(item -> dataId.contains(item)).collect(Collectors.toList());
        if (dataId.size() > 0) {
            idAll.addAll(intersection);
        } else {
            idAll.addAll(idList);
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<FlowTaskEntity> orderStaList = new ArrayList<>();
        if (Objects.equals(formType, 2)) {
            orderStaList.addAll(flowTaskService.getOrderStaList(idAll));
        }
        for (String id : idAll) {
            VisualdevModelDataInfoVO infoVO = visualDevInfoService.getEditDataInfo(id, visualdevEntity);
            boolean taskStatus = orderStaList.stream().filter(t -> Objects.equals(t.getStatus(), 2) && t.getId().equals(id)).count() > 0;
            boolean isAdd = Objects.equals(formType, 2) ? taskStatus : true;
            if (StringUtil.isNotEmpty(infoVO.getData())) {
                Map<String, Object> map = JsonUtil.stringToMap(infoVO.getData());
                map.put("id", infoVO.getId());
                if (isAdd) {
                    dataList.add(map);
                }
            }
        }
        return dataList;
    }

    private void message(String msgId, List<String> userIdList, Map<String, Object> parameterMap, UserInfo userInfo) {
        SentMessageForm sentMessageForm = new SentMessageForm();
        sentMessageForm.setUserInfo(userInfo);
        sentMessageForm.setTemplateId(msgId);
        sentMessageForm.setToUserIds(userIdList);
        sentMessageForm.setParameterMap(parameterMap);
        sentMessageForm.setType(3);
        sentMessageForm.setContentMsg(new HashMap<>());
        sentMessageUtil.sendDelegateMsg(sentMessageForm);
    }

    private List<IntegrateNodeEntity> nodeList(IntegrateNodeEntity nodeEntity) {
        List<IntegrateNodeEntity> list = integrateNodeService.getList(new ArrayList() {{
            add(nodeEntity.getTaskId());
        }}, nodeEntity.getNodeCode(), 1);
        IntegrateNodeEntity integrateNode = list.stream().filter(t -> t.getNodeCode().equals(nodeEntity.getNodeCode()) && t.getResultType() == 1).findFirst().orElse(null);
        nodeEntity.setResultType(integrateNode != null ? integrateNode.getResultType() : nodeEntity.getResultType());
        return list;
    }

    private Map<String, Object> templateJson(List<IntegrateTemplateModel> templateJson, Map<String, Object> data) throws WorkFlowException {
        Map<String, Object> parameterMap = new HashMap<>();
        Set<String> msg = new HashSet<>();
        for (IntegrateTemplateModel templateJsonModel : templateJson) {
            List<IntegrateParamModel> paramJson = templateJsonModel.getParamJson();
            for (IntegrateParamModel integrateParamModel : paramJson) {
                Boolean required = integrateParamModel.getRequired();
                String fieldId = integrateParamModel.getField();
                String relationField = integrateParamModel.getRelationField();
                String[] model = StringUtil.isNotEmpty(relationField) ? relationField.split("-") : new String[]{};
                String dataValue = data.get(relationField) != null ? String.valueOf(data.get(relationField)) : "";
                if ("@formId".equals(relationField)) {
                    dataValue = data.get("id") != null ? String.valueOf(data.get("id")) : "";
                }
                if (model.length > 1) {
                    Object dataList = data.get(model[0]);
                    if (dataList instanceof List) {
                        List<Map<String, Object>> listAll = (List<Map<String, Object>>) dataList;
                        List<Object> dataListAll = new ArrayList<>();
                        for (Map<String, Object> objectMap : listAll) {
                            dataListAll.add(objectMap.get(model[1]));
                        }
                        if (required && ObjectUtil.isEmpty(dataListAll)) {
                            msg.add(fieldId);
                        }
                        dataValue = String.valueOf(dataListAll);
                    }
                }
                if (required && ObjectUtil.isEmpty(dataValue)) {
                    msg.add(fieldId);
                }
                parameterMap.put(integrateParamModel.getMsgTemplateId() + fieldId, dataValue);
            }
        }
        errRequiredMsg(msg);
        return parameterMap;
    }

    private void errMsg(IntegrateNodeEntity nodeEntity, Exception e) {
        ActionResult result = new ActionResult();
        result.setCode(400);
        result.setMsg(e.getMessage());
        if (nodeEntity != null) {
            nodeEntity.setErrorMsg(JsonUtil.getObjectToString(result));
        }
    }

    private ActionResult interfaceTemplateJson(IntegrateChildNodeList childNodeList, Map<String, Object> data, UserInfo userInfo) throws WorkFlowException {
        IntegrateProperties properties = childNodeList.getProperties();
        String interId = properties.getFormId();
        Map<String, String> parameterMap = new HashMap<>();
        List<IntegrateTemplateModel> templateJson = properties.getTemplateJson();
        Set<String> msg = new HashSet<>();
        for (IntegrateTemplateModel templateJsonModel : templateJson) {
            String fieldId = templateJsonModel.getField();
            Boolean required = templateJsonModel.getRequired();
            String relationField = templateJsonModel.getRelationField();
            String dataValue = data.get(relationField) != null ? String.valueOf(data.get(relationField)) : null;
            if ("@formId".equals(relationField)) {
                dataValue = String.valueOf(data.get("id"));
            }
            String dataFieldValue = relationField;
            String dataJson = !"2".equals(templateJsonModel.getSourceType()) ? dataValue : dataFieldValue;
            String[] model = StringUtil.isNotEmpty(relationField) ? relationField.split("-") : new String[]{};
            if (model.length > 1) {
                Object dataList = data.get(model[0]);
                if (dataList instanceof List) {
                    List<Map<String, Object>> listAll = (List<Map<String, Object>>) dataList;
                    List<Object> dataListAll = new ArrayList<>();
                    for (Map<String, Object> objectMap : listAll) {
                        dataListAll.add(objectMap.get(model[1]));
                    }
                    if (required && ObjectUtil.isEmpty(dataListAll)) {
                        msg.add(fieldId);
                    }
                    dataJson = String.valueOf(dataListAll);
                }
            }
            if (required && ObjectUtil.isEmpty(dataJson)) {
                msg.add(fieldId);
            }
            parameterMap.put(fieldId, dataJson);
        }
        errRequiredMsg(msg);
        DataInterfaceInvokeModel invokeModel = new DataInterfaceInvokeModel();
        invokeModel.setId(interId);
        invokeModel.setMap(parameterMap);
        invokeModel.setTenantId(userInfo.getTenantId());
        invokeModel.setToken(userInfo.getToken());
        ActionResult dataInterfaceInfo = dataInterfaceService.infoToId(interId, userInfo.getTenantId(),
                parameterMap, userInfo.getToken(),
                null, null, null, null);
        return dataInterfaceInfo;
    }

    private void errRequiredMsg(Set<String> msg) throws WorkFlowException {
        if (msg.size() > 0) {
            throw new WorkFlowException(new ArrayList(msg).get(0) + "字段不能为空");
        }
    }

    /**
     * 发起审批
     *
     * @param data          数据
     * @param nodeList      节点数组
     * @param node          当前节点
     * @param retryNodeCode 日志失败重试
     * @throws WorkFlowException 异常
     */
    private void launchFlow(Map<String, Object> data, List<IntegrateNodeEntity> nodeList, String node, String retryNodeCode) throws WorkFlowException {
        IntegrateNodeEntity nodeEntity = nodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (nodeEntity != null) {
            List<IntegrateNodeEntity> list = nodeList(nodeEntity);
            boolean isRetry = nodeEntity.getNodeCode().equals(retryNodeCode);
            if (list.size() == 0 || isRetry) {
                nodeEntity.setStartTime(new Date());
                IntegrateChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), IntegrateChildNodeList.class);
                IntegrateProperties properties = childNodeList.getProperties();
                String msg = "";
                try {
                    List<String> userList = properties.getInitiator();
                    List<UserEntity> userName = userService.getUserName(userList, true);
                    for (UserEntity entity : userName) {
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserId(entity.getId());
                        userInfo.setUserName(entity.getRealName());
                        String flowId = properties.getFlowId();
                        boolean isApp = !RequestContext.isOrignPc();
                        List<TransferModel> transferList = properties.getTransferList();
                        Map<String, Object> saveData = formData(data, transferList);
                        FlowModel flowModel = new FlowModel();
                        flowModel.setUserInfo(userInfo);
                        flowModel.setFlowId(flowId);
                        flowModel.setStatus(FlowStatusEnum.submit.getMessage());
                        flowModel.setSystemId(isApp ? userInfo.getSystemId() : userInfo.getAppSystemId());
                        flowModel.setFormData(saveData);
                        ActionResult result = flowTaskService.batchCreateOrUpdate(flowModel);
                        if(StringUtil.isNotEmpty(result.getMsg())){
                            throw new WorkFlowException(result.getMsg());
                        }
                    }
                    nodeEntity.setResultType(1);
                } catch (Exception e) {
                    errMsg(nodeEntity, e);
                    msg = e.getMessage();
                }

                nodeEntity.setEndTime(new Date());
                if (isRetry) {
                    integrateNodeService.update(nodeEntity.getTaskId(), nodeEntity.getNodeCode());
                }
                integrateNodeService.create(nodeEntity);
                if (StringUtil.isNotEmpty(msg)) {
                    throw new WorkFlowException(msg);
                }
            }
        }
    }
    //------------------------------------------webhook触发------------------------------------------------------

    public void integrate(String id, String tenantId, Map<String, Object> body) throws WorkFlowException {
        IntegrateEntity entity = integrateService.getInfo(id);
        if (entity == null) {
            throw new WorkFlowException("路径错误");
        }
        if (Objects.equals(entity.getEnabledMark(), 0)) {
            throw new WorkFlowException("集成助手被禁用");
        }
        IntegrateChildNode childNode = JsonUtil.getJsonToBean(entity.getTemplateJson(), IntegrateChildNode.class);
        IntegrateProperties properties = childNode.getProperties();
        List<FieLdsModel> formFieldList = properties.getFormFieldList();
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (FieLdsModel fieLdsModel : formFieldList) {
            map.put(fieLdsModel.getId(), body.get(fieLdsModel.getId()));
        }
        dataList.add(map);
        //登录一个临时用户
        String token = AuthUtil.loginTempUser(entity.getCreatorUserId(), tenantId);
        UserInfo userInfo = UserProvider.getUser(token);
        UserProvider.setLocalLoginUser(userInfo);
        integrate(entity.getId(), dataList, userInfo);
    }
}

