package jnpf.base.util;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import jnpf.base.SmsModel;
import jnpf.base.UserInfo;
import jnpf.base.entity.SynThirdInfoEntity;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.model.synthirdinfo.DingTalkModel;
import jnpf.base.service.MessageTemplateService;
import jnpf.base.service.SmsTemplateService;
import jnpf.base.service.SysconfigService;
import jnpf.constant.MsgCode;
import jnpf.message.entity.AccountConfigEntity;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageMonitorEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.message.entity.MessageTemplateConfigEntity;
import jnpf.message.entity.WechatUserEntity;
import jnpf.message.enums.MessageTypeEnum;
import jnpf.message.model.EmailModel;
import jnpf.message.model.WxgzhMessageModel;
import jnpf.message.model.messagetemplateconfig.TemplateParamModel;
import jnpf.message.model.sendmessageconfig.SendConfigTemplateModel;
import jnpf.message.service.AccountConfigService;
import jnpf.message.service.MessageMonitorService;
import jnpf.message.service.MessageService;
import jnpf.message.service.MessageTemplateConfigService;
import jnpf.message.service.MessagereceiveService;
import jnpf.message.service.SmsFieldService;
import jnpf.message.service.SynThirdInfoService;
import jnpf.message.service.TemplateParamService;
import jnpf.message.service.WechatUserService;
import jnpf.message.util.EmailUtil;
import jnpf.message.util.JnpfMessageUtil;
import jnpf.message.util.WebHookUtil;
import jnpf.message.util.weixingzh.WXGZHWebChatUtil;
import jnpf.model.BaseSystemInfo;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.message.SmsUtil;
import jnpf.util.third.DingTalkUtil;
import jnpf.util.third.QyWebChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TestSendConfigUtil {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MessagereceiveService messagereceiveService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;

    @Autowired
    private MessageTemplateService messageTemplateService;
    @Autowired
    private SmsTemplateService smsTemplateService;
    @Autowired
    private MessageTemplateConfigService messageTemplateConfigService;

    @Autowired
    private TemplateParamService templateParamService;
    @Autowired
    private SmsFieldService smsFieldService;
    @Autowired
    private AccountConfigService accountConfigService;
    @Autowired
    private MessageMonitorService messageMonitorService;
    @Autowired
    private WechatUserService wechatUserService;

    /**
     * 测试发送配置
     */
    public String sendMessage(SendConfigTemplateModel model, UserInfo userInfo) {
        List<String> toUserIdsList = model.getToUser();
        // 模板id
        String templateId = model.getTemplateId();
        // 参数
        Map<String, Object> parameterMap = new HashMap<>();
        List<TemplateParamModel> paramModelList = JsonUtil.getJsonToList(model.getParamJson(), TemplateParamModel.class);
        if (paramModelList != null && paramModelList.size() > 0) {
            for (TemplateParamModel paramModel : paramModelList) {
                parameterMap.put(paramModel.getField(), paramModel.getValue());
            }
        }
        boolean flag = true;
        if (!"webhook".equals(model.getMessageType())) {
            if (!(toUserIdsList != null && toUserIdsList.size() > 0)) {
                log.error("接收人员为空");
                flag = false;
            }
        }
        if (StringUtil.isEmpty(templateId)) {
            log.error("模板Id为空");
            flag = false;
        }
        if (flag) {
            // 获取消息模板详情
            MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(templateId);
            // 替换参数
            String content = entity.getContent();
            // 替换参数
            if (StringUtil.isNotEmpty(content)) {
                StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
                content = strSubstitutor.replace(content);
            }
            if (entity != null) {
                MessageTypeEnum typeEnum = MessageTypeEnum.getByCode(entity.getMessageType());
                String sendType = entity.getMessageType();
                switch (typeEnum) {
                    case SysMessage:
                        // 站内消息
                        String title = entity.getTitle();
                        if (StringUtil.isNotEmpty(title)) {
                            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
                            title = strSubstitutor.replace(title);
                        }
                        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(model.getTemplateId());
                        Integer source = Integer.parseInt(msgTemEntity.getMessageSource());
                        Integer type = Integer.parseInt(msgTemEntity.getMessageType());
                        messageService.sentMessage(toUserIdsList, title, content, userInfo, source, type, true);
                        if ("1".equals(msgTemEntity.getMessageSource())) {
                            content = null;
                        }
//                            MessageMonitorEntity monitorEntity = createSysMessageMonitor(msgTemEntity, content, userInfo, toUser, title);
//                            messageMonitorService.create(monitorEntity);
                        break;
                    case SmsMessage:
                        // 发送短信
                        JSONObject jsonObject1 = sendSms(toUserIdsList, model, parameterMap);
                        if (!(Boolean) jsonObject1.get("code")) {
                            return "发送短信消息失败，错误：" + jsonObject1.get("error");
                        }
                        break;
                    case MailMessage:
                        // 邮件
                        JSONObject jsonObject2 = SendMail(toUserIdsList, userInfo, sendType, model, parameterMap);
                        if (!(Boolean) jsonObject2.get("code")) {
                            return "发送邮件消息失败，错误：" + jsonObject2.get("error");
                        }
                        break;
                    case QyMessage:
                        // 企业微信
                        JSONObject jsonObject3 = SendQyWebChat(toUserIdsList, userInfo, sendType, model, parameterMap);
                        if (!(Boolean) jsonObject3.get("code")) {
                            return "发送企业微信消息失败，错误：" + jsonObject3.get("error");
                        }
                        break;
                    case DingMessage:
                        // 钉钉
                        JSONObject jsonObject4 = SendDingTalk(toUserIdsList, userInfo, sendType, model, parameterMap);
                        if (!(Boolean) jsonObject4.get("code")) {
                            return "发送钉钉消息失败，错误：" + jsonObject4.get("error");
                        }
                        break;
                    case WebHookMessage:
                        // webhook
                        JSONObject jsonObject5 = SendWebHook(sendType, userInfo, model, parameterMap);
                        if (!(Boolean) jsonObject5.get("code")) {
                            return "发送webhook消息失败，错误：" + jsonObject5.get("error");
                        }
                        break;
                    case WechatMessage:
                        // 微信公众号
                        JSONObject jsonObject6 = SendWXGzhChat(toUserIdsList, userInfo, sendType, model, parameterMap);
                        if (!(Boolean) jsonObject6.get("code")) {
                            return "发送微信公众号消息失败，错误：" + jsonObject6.get("error");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return null;
    }

    private JSONObject SendWebHook(String sendType, UserInfo userInfo, SendConfigTemplateModel model, Map<String, Object> parameterMap) {
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(model.getTemplateId());
        String content = entity.getContent();
        JSONObject retJson = new JSONObject();
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());

        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }
        AccountConfigEntity entity1 = accountConfigService.getInfo(model.getAccountConfigId());
        if (entity != null) {
            //消息监控-消息模板写入
            monitorEntity.setMessageType(entity.getMessageType());
            monitorEntity.setMessageTemplateId(entity.getId());
            String title = entity.getTitle();
            if (StringUtil.isNotEmpty(title)) {
                StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
                title = strSubstitutor.replace(title);
            }
            monitorEntity.setTitle(title);
            monitorEntity.setReceiveUser(entity1.getWebhookAddress());
            monitorEntity.setContent(content);
            monitorEntity.setMessageSource(entity.getMessageSource());
            if (entity1 != null) {
                //消息监控-账号配置写入
                monitorEntity.setAccountId(entity1.getId());
                monitorEntity.setAccountCode(entity1.getEnCode());
                monitorEntity.setAccountName(entity1.getFullName());
                switch (entity1.getWebhookType()) {
                    case "1":
                        //钉钉
                        if ("1".equals(entity1.getApproveType())) {
                            JSONObject result = WebHookUtil.sendDDMessage(entity1.getWebhookAddress(), content);
                            messageMonitorService.create(monitorEntity);
                            if (ObjectUtil.isNotEmpty(result)) {
                                if (!"0".equals(result.get("errcode").toString())) {
                                    retJson.put("code", false);
                                    retJson.put("error", result.get("errmsg"));
                                    return retJson;
                                }
                            } else {
                                retJson.put("code", false);
                                retJson.put("error", "webhook账号地址配置错误！");
                                return retJson;
                            }
                        } else if ("2".equals(entity1.getApproveType())) {
                            JSONObject result = WebHookUtil.sendDingDing(entity1.getWebhookAddress(), entity1.getBearer(), content);
                            messageMonitorService.create(monitorEntity);
                            if (ObjectUtil.isNotEmpty(result)) {
                                if (!"0".equals(result.get("errcode").toString())) {
                                    retJson.put("code", false);
                                    retJson.put("error", result.get("errmsg"));
                                    return retJson;
                                }
                            } else {
                                retJson.put("code", false);
                                retJson.put("error", "webhook账号地址配置错误！");
                                return retJson;
                            }
                        }
                        break;
                    case "2":
                        if ("1".equals(entity1.getApproveType())) {
                            JSONObject result = WebHookUtil.callWeChatBot(entity1.getWebhookAddress(), content);
                            messageMonitorService.create(monitorEntity);
                            if (!"0".equals(result.get("errcode").toString())) {
                                retJson.put("code", false);
                                retJson.put("error", result.get("errmsg"));
                                return retJson;
                            }
                        }
                        break;
                    default:
                        break;
                }
            } else {
                retJson.put("code", false);
                retJson.put("error", "账号配置数据不存在！");
                messageMonitorService.create(monitorEntity);
                return retJson;
            }
        } else {
            retJson.put("code", false);
            retJson.put("error", "消息模板数据不存在！");
            messageMonitorService.create(monitorEntity);
            return retJson;
        }
        retJson.put("code", true);
        retJson.put("error", MsgCode.SU012.get());
        return retJson;
    }

    /**
     * 发送企业微信消息
     *
     * @param toUserIdsList
     * @param userInfo
     * @param sendType
     * @param parameterMap
     * @return
     */
    private JSONObject SendQyWebChat(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateModel model, Map<String, Object> parameterMap) {
        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(model.getTemplateId());

        //创建消息监控
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        //消息监控-消息模板写入
        monitorEntity.setMessageType(entity.getMessageType());
        monitorEntity.setMessageSource(entity.getMessageSource());
        monitorEntity.setMessageTemplateId(entity.getId());

        String content = entity.getContent();
        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }

        String title = entity.getTitle();
        if (StringUtil.isNotEmpty(title)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            title = strSubstitutor.replace(title);
        }
        monitorEntity.setTitle(title);
        monitorEntity.setContent(content);
        for (String userId : toUserIdsList) {
            UserEntity userEntity = userService.getInfo(userId);
            if (entity != null) {
                // 获取系统配置
                Map<String, String> objModel = getSystemConfig();
                BaseSystemInfo config = JsonUtil.getJsonToBean(objModel, BaseSystemInfo.class);
                String corpId = config.getQyhCorpId();
                String agentId = config.getQyhAgentId();
                // 获取的应用的Secret值
                String corpSecret = config.getQyhAgentSecret();

                String wxUserId = "";
                StringBuilder toWxUserId = new StringBuilder();
                String toUserIdAll = "";
                StringBuilder nullUserInfo = new StringBuilder();
                List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();

                // 相关参数验证
                if (StringUtil.isEmpty(corpId)) {
                    log.error("企业ID为空");
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：企业ID为空！");
                    continue;
                }
                if (StringUtil.isEmpty(corpSecret)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：凭证密钥为空！");
                    continue;
                }
                if (StringUtil.isEmpty(agentId)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：企业微信应用凭证为空！");
                    continue;
                }
                if (StringUtil.isEmpty(content)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：内容为空！");
                    continue;
                }
                if (StringUtil.isEmpty(userId)) {
                    code = false;
                    error = error.append("；").append("接收人为空！");
                    continue;
                }
                // 创建消息实体
                MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), content, null, Integer.parseInt(sendType));

                    // 获取接收人员的企业微信号、创建消息用户实体
                    wxUserId = "";
                    // 从同步表获取对应的企业微信ID
                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId("1", "2", userId);
                    if (synThirdInfoEntity != null) {
                        wxUserId = synThirdInfoEntity.getThirdObjId();
                    }
                    if (StringUtil.isEmpty(wxUserId)) {
                        nullUserInfo = nullUserInfo.append(",").append(userId);
                    } else {
                        toWxUserId = toWxUserId.append("|").append(wxUserId);
                    }
                    messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(userId, title, Integer.valueOf(sendType)));

                // 处理企业微信号信息串并验证
                toUserIdAll = toWxUserId.toString();
                if (StringUtil.isNotEmpty(toUserIdAll)) {
                    toUserIdAll = toUserIdAll.substring(1);
                }
                if (StringUtil.isEmpty(toUserIdAll)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：接收人对应的企业微信号全部为空！");
                    continue;
                }

                // 批量发送企业信息信息
                retJson = QyWebChatUtil.sendWxMessage(corpId, corpSecret, agentId, toUserIdAll, content);
                if (!retJson.getBoolean("code")) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：" + retJson.get("error"));
                    continue;
                }

                // 企业微信号为空的信息写入备注
                if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
                    messageEntity.setExcerpt(nullUserInfo.substring(1) + "对应的企业微信号为空");
                }
                continue;
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                continue;
            }
        }
        messageMonitorService.create(monitorEntity);
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            msg = msg.substring(1);
            retJson.put("code", false);
            retJson.put("error", msg);
        }
        return retJson;
    }

    /**
     * List<String> toUserIdsList, UserInfo userInfo, String sendType, MessageTemplateEntity entity, Map<String, String> parameterMap
     *
     * @param toUserIdsList
     * @param userInfo
     * @param sendType
     * @param parameterMap
     * @return
     */
    private JSONObject SendDingTalk(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateModel model, Map<String, Object> parameterMap) {
        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();
        //创建消息监控
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(model.getTemplateId());

        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        //消息监控-消息模板写入
        monitorEntity.setMessageType(entity.getMessageType());
        monitorEntity.setMessageSource(entity.getMessageSource());
        monitorEntity.setMessageTemplateId(entity.getId());

        String content = entity.getContent();
        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }

        String title = entity.getTitle();
        if (StringUtil.isNotEmpty(title)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            title = strSubstitutor.replace(title);
        }
        monitorEntity.setTitle(title);
        monitorEntity.setContent(content);
        for (String userId : toUserIdsList) {
            UserEntity userEntity = userService.getInfo(userId);
            if (entity != null) {
                // 获取系统配置
                Map<String, String> objModel = getSystemConfig();
                DingTalkModel dingTalkModel = JsonUtil.getJsonToBean(objModel, DingTalkModel.class);
                String appKey = dingTalkModel.getDingSynAppKey();
                String appSecret = dingTalkModel.getDingSynAppSecret();
                String agentId = dingTalkModel.getDingAgentId();
                String dingUserId = "";
                StringBuilder toDingUserId = new StringBuilder();
                String toUserIdAll = "";
                StringBuilder nullUserInfo = new StringBuilder();
                List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();

                // 相关参数验证
                if (StringUtil.isEmpty(appKey)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AppKey为空！");
                    continue;
                }
                if (StringUtil.isEmpty(appSecret)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AppSecret为空！");
                    continue;
                }
                if (StringUtil.isEmpty(agentId)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AgentId为空！");
                    continue;
                }
                if (StringUtil.isEmpty(content)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：内容为空！");
                    continue;
                }
                if (StringUtil.isEmpty(userId)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：接收人为空！");
                    continue;
                }

                // 创建消息实体
                MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), content, null, Integer.parseInt(sendType));

                // 获取接收人员的钉钉号、创建消息用户实体
                dingUserId = "";
                // 从同步表获取对应用户的钉钉ID
                SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId("2", "2", userId);
                if (synThirdInfoEntity != null) {
                    dingUserId = synThirdInfoEntity.getThirdObjId();
                }
                if (StringUtil.isEmpty(dingUserId)) {
                    nullUserInfo = nullUserInfo.append(",").append(userId);
                } else {
                    toDingUserId = toDingUserId.append(",").append(dingUserId);
                }
                messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(userId, title, Integer.valueOf(sendType)));

                // 处理接收人员的钉钉号信息串并验证
                toUserIdAll = toDingUserId.toString();
                if (StringUtil.isNotEmpty(toUserIdAll)) {
                    toUserIdAll = toUserIdAll.substring(1);
                }
                if (StringUtil.isEmpty(toUserIdAll)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：接收人对应的钉钉号为空！");
                    continue;
                }

                // 批量发送钉钉信息
                retJson = DingTalkUtil.sendDingMessage(appKey, appSecret, agentId, toUserIdAll, content);
                if (!retJson.getBoolean("code")) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：" + retJson.get("error"));
                    continue;
                }

                // 钉钉号为空的信息写入备注
                if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
                    messageEntity.setExcerpt(nullUserInfo.toString().substring(1) + "对应的钉钉号为空！");
                }
                continue;
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                continue;
            }
        }
        messageMonitorService.create(monitorEntity);
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            msg = msg.substring(1);
            retJson.put("code", false);
            retJson.put("error", msg);
        }
        return retJson;
    }

    /**
     * 发送邮件
     *
     * @param toUserIdsList
     * @param userInfo
     * @param sendType
     * @param parameterMap
     * @return
     */
    private JSONObject SendMail(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateModel model, Map<String, Object> parameterMap) {
        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();
        //创建消息监控
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(model.getTemplateId());
        AccountConfigEntity entity1 = accountConfigService.getInfo(model.getAccountConfigId());
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        //消息监控-消息模板写入
        monitorEntity.setMessageType(entity.getMessageType());
        monitorEntity.setMessageSource(entity.getMessageSource());
        monitorEntity.setMessageTemplateId(entity.getId());
        //消息监控-账号配置写入
        monitorEntity.setAccountId(entity1.getId());
        monitorEntity.setAccountCode(entity1.getEnCode());
        monitorEntity.setAccountName(entity1.getFullName());
        String content = entity.getContent();
        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }

        String title = entity.getTitle();
        if (StringUtil.isNotEmpty(title)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            title = strSubstitutor.replace(title);
        }
        monitorEntity.setTitle(title);
        monitorEntity.setContent(content);
        for (String userId : toUserIdsList) {
            UserEntity userEntity = userService.getInfo(userId);
            if (userEntity != null) {
                if (entity != null) {
                    if (entity1 != null) {

                        Map<String, String> objModel = new HashMap<>();
                        objModel.put("emailSmtpHost", entity1.getSmtpServer());
                        objModel.put("emailSmtpPort", entity1.getSmtpPort().toString());
                        objModel.put("emailSenderName", entity1.getAddressorName());
                        objModel.put("emailAccount", entity1.getSmtpUser());
                        objModel.put("emailPassword", entity1.getSmtpPassword());
                        objModel.put("emailSsl", entity1.getSslLink().equals("1") ? "true" : "false");

                        EmailModel emailModel = JsonUtil.getJsonToBean(objModel, EmailModel.class);
                        StringBuilder nullUserInfo = new StringBuilder();
                        List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();
                        StringBuilder toUserMail = new StringBuilder();
                        String userEmailAll = "";
                        String userEmail = "";
                        String userName = "";

                        // 相关参数验证
                        if (StringUtil.isEmpty(emailModel.getEmailSmtpHost())) {
                            code = false;
                            error = error.append("；").append(userEntity.getRealName() + "：SMTP服务为空！");
                            continue;
                        } else if (StringUtil.isEmpty(emailModel.getEmailSmtpPort())) {
                            code = false;
                            error = error.append("；").append(userEntity.getRealName() + "：SMTP端口为空！");
                            continue;
                        } else if (StringUtil.isEmpty(emailModel.getEmailAccount())) {
                            code = false;
                            error = error.append("；").append(userEntity.getRealName() + "：发件人邮箱为空！");
                            continue;
                        } else if (StringUtil.isEmpty(emailModel.getEmailPassword())) {
                            code = false;
                            error = error.append("；").append(userEntity.getRealName() + "：发件人密码为空！");
                            continue;
                        } else if (StringUtil.isEmpty(userId)) {
                            code = false;
                            error = error.append("；").append("接收人为空！");
                            continue;
                        } else {
                            // 设置邮件标题
                            emailModel.setEmailTitle(title);
                            // 设置邮件内容
                            emailModel.setEmailContent(content);

                            // 创建消息实体
                            MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), emailModel.getEmailTitle(), emailModel.getEmailContent(), Integer.parseInt(sendType));

                            // 获取收件人的邮箱地址、创建消息用户实体
                            if (userEntity != null) {
                                userEmail = StringUtil.isEmpty(userEntity.getEmail()) ? "" : userEntity.getEmail();
                                userName = userEntity.getRealName();
                            }
                            if (userEmail != null && !"".equals(userEmail)) {
                                if (EmailUtil.isEmail(userEmail)) {
                                    toUserMail = toUserMail.append(",").append(userName).append("<").append(userEmail).append(">");
                                }
                            } else {
                                nullUserInfo = nullUserInfo.append(",").append(userId);
                            }
                            messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(userId, title, Integer.valueOf(sendType)));

                            // 处理接收人员的邮箱信息串并验证
                            userEmailAll = toUserMail.toString();
                            if (StringUtil.isNotEmpty(userEmailAll)) {
                                userEmailAll = userEmailAll.substring(1);
                            }
                            if (StringUtil.isEmpty(userEmailAll)) {
                                code = false;
                                error = error.append("；").append(userEntity.getRealName() + "：接收人为空！");
                                continue;
                            } else {
                                // 设置接收人员
                                emailModel.setEmailToUsers(userEmailAll);
                                // 发送邮件
                                retJson = EmailUtil.sendMail(emailModel);
                                if (!retJson.getBoolean("code")) {
                                    code = false;
                                    error = error.append("；").append(userEntity.getRealName() + "：" + retJson.get("error"));
                                    continue;
                                } else {
                                    // 邮箱地址为空的信息写入备注
                                    if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
                                        messageEntity.setExcerpt(nullUserInfo.substring(1) + "对应的邮箱为空");
                                    }
                                    continue;
                                    // 写入系统的消息表、消息用户表
                                }
                            }
                        }
                    } else {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：账号配置数据不存在！");
                        continue;
                    }
                } else {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                    continue;
                }
            }
        }
        messageMonitorService.create(monitorEntity);
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            msg = msg.substring(1);
            retJson.put("code", false);
            retJson.put("error", msg);
        }
        return retJson;
    }

    /**
     * 发送短信
     *
     * @param toUserIdsList
     * @param parameterMap
     * @return
     */
    private JSONObject sendSms(List<String> toUserIdsList, SendConfigTemplateModel model, Map<String, Object> parameterMap) {
        UserInfo userInfo = userProvider.get();
        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();

        //创建消息监控
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(model.getTemplateId());
        AccountConfigEntity entity1 = accountConfigService.getInfo(model.getAccountConfigId());
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        //消息监控-消息模板写入
        monitorEntity.setMessageType(entity.getMessageType());
        monitorEntity.setMessageSource(entity.getMessageSource());
        monitorEntity.setMessageTemplateId(entity.getId());
        //消息监控-账号配置写入
        monitorEntity.setAccountId(entity1.getId());
        monitorEntity.setAccountCode(entity1.getEnCode());
        monitorEntity.setAccountName(entity1.getFullName());
        String content = entity.getContent();
        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }

        String title = entity.getTitle();
        if (StringUtil.isNotEmpty(title)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            title = strSubstitutor.replace(title);
        }
        monitorEntity.setTitle(title);
        monitorEntity.setContent(content);
        for (String userId : toUserIdsList) {
            UserEntity userEntity = userService.getInfo(userId);
            if (entity != null) {
                if (entity1 != null) {
                    //账号配置——短信
                    Map<String, String> objModel = new HashMap<>(16);
                    objModel.put("aliAccessKey", entity1.getAppId());
                    objModel.put("aliSecret", entity1.getAppSecret());
                    objModel.put("tencentSecretId", entity1.getAppId());
                    objModel.put("tencentSecretKey", entity1.getAppSecret());
                    objModel.put("tencentAppId", entity1.getSdkAppId());
                    objModel.put("tencentAppKey", entity1.getAppKey());
                    SmsModel smsConfig = JsonUtil.getJsonToBean(objModel, SmsModel.class);
                    int company = Integer.parseInt(entity1.getChannel());
                    // 组装接受用户
                    StringBuffer toUserIdList = new StringBuffer();

                    if (isPhone(userEntity.getMobilePhone())) {
                        toUserIdList.append(userEntity.getMobilePhone() + ",");
                    }
                    //短信参数
                    Map<String, Object> smsMap = new HashMap<>();
                    if (entity != null) {
                        smsMap = smsFieldService.getParamMap(entity.getId(), parameterMap);
                    } else {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                        continue;
                    }
                    // 发送短信
                    String endPoint = "";
                    if ("1".equals(entity1.getChannel())) {
                        endPoint = entity1.getEndPoint();
                    } else if ("2".equals(entity1.getChannel())) {
                        endPoint = entity1.getZoneName();
                    }
                    content = SmsUtil.querySmsTemplateContent(company, smsConfig, endPoint, entity1.getZoneParam(), entity.getTemplateCode());
                    if (StringUtil.isNotBlank(content) && !"null".equals(content)) {
                        if ("1".equals(entity1.getChannel())) {
                            if (content.contains("${")) {
                                for (String key : smsMap.keySet()) {
                                    if (StringUtil.isNotBlank(String.valueOf(smsMap.get(key))) && smsMap.get(key) != null) {
                                        String v = String.valueOf(smsMap.get(key));
                                        content = content.replace("${" + key + "}", smsMap.get(key).toString());
                                    }
                                }
                            }
                        } else if ("2".equals(entity1.getChannel())) {
                            if (content.contains("{")) {
                                for (String key : smsMap.keySet()) {
                                    if (StringUtil.isNotBlank(String.valueOf(smsMap.get(key))) && smsMap.get(key) != null) {
                                        content = content.replace("{" + key + "}", smsMap.get(key).toString());
                                    }
                                }
                            }
                        }
                    }
                    monitorEntity.setContent(content);
                    if (StringUtil.isEmpty(toUserIdList)) {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：手机号码格式错误！");
                        continue;
                    }
                    String result = SmsUtil.sentSms(company, smsConfig, endPoint, entity1.getZoneParam(), toUserIdList.toString(), entity1.getSmsSignature(), entity.getTemplateCode(), smsMap);
                    if (!"Ok".equalsIgnoreCase(result)) {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：" + result);
                        continue;
                    }
                    continue;
                } else {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：账号配置数据不存在！");
                    continue;
                }
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                continue;
            }
        }
        messageMonitorService.create(monitorEntity);
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            msg = msg.substring(1);
            retJson.put("code", false);
            retJson.put("error", msg);
        }
        return retJson;
    }

    /**
     * 发送微信公众号消息
     *
     * @param toUserIdsList
     * @param userInfo
     * @param sendType
     * @param parameterMap
     * @return
     */
    public JSONObject SendWXGzhChat(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateModel model, Map<String, Object> parameterMap) {
        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();
        //创建消息监控
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(model.getTemplateId());
        AccountConfigEntity entity1 = accountConfigService.getInfo(model.getAccountConfigId());
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        //消息监控-消息模板写入
        monitorEntity.setMessageType(entity.getMessageType());
        monitorEntity.setMessageSource(entity.getMessageSource());
        monitorEntity.setMessageTemplateId(entity.getId());
        //消息监控-账号配置写入
        monitorEntity.setAccountId(entity1.getId());
        monitorEntity.setAccountCode(entity1.getEnCode());
        monitorEntity.setAccountName(entity1.getFullName());
        String content = entity.getContent();
        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }

        String title = entity.getTitle();
        if (StringUtil.isNotEmpty(title)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            title = strSubstitutor.replace(title);
        }
        monitorEntity.setTitle(title);
        monitorEntity.setContent(content);
        for (String userId : toUserIdsList) {
            if (StringUtil.isEmpty(userId)) {
                code = false;
                error = error.append("；").append("接收人为空！");
                continue;
            }
            UserEntity userEntity = userService.getById(userId);
            AccountConfigEntity accountEntity = accountConfigService.getInfo(model.getAccountConfigId());
            // 获取消息模板详情
            MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(model.getTemplateId());
            if (msgTemEntity != null) {
                String templateKId = msgTemEntity.getTemplateCode();
                //微信公众号参数
                Map<String, Object> smsMap = new HashMap<>();
                if (model != null) {
                    smsMap = smsFieldService.getParamMap(model.getTemplateId(), parameterMap);
                }
                if (smsMap.containsKey("title")) {
                    title = smsMap.get("title").toString();
                    smsMap.keySet().removeIf(k -> k.equals("title"));
                }
                monitorEntity.setTitle(title);
                if (ObjectUtil.isNotEmpty(accountEntity)) {
                    // 获取系统配置
                    String appId = accountEntity.getAppId();
                    String appsecret = accountEntity.getAppSecret();
                    String wxxcxAppId = msgTemEntity.getXcxAppId();
                    String type = msgTemEntity.getWxSkip();

                    // 相关参数验证
                    if (StringUtil.isEmpty(templateKId)) {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：微信公众号模板id未创建！");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    }
                    if (StringUtil.isEmpty(appId)) {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：公众号appid为空为空！");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    }
                    if (StringUtil.isEmpty(appsecret)) {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：公众号appsecret为空为空！");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    }
                    // 获取微信公众号的token
                    String token = WXGZHWebChatUtil.getAccessToken(appId, appsecret);
                    if (StringUtil.isEmpty(token)) {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：获取微信公众号token失败！");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    }
                    // 微信公众号发送消息
                    //获取用户在对应微信公众号上的openid
                    WechatUserEntity wechatUserEntity = wechatUserService.getInfoByGzhId(userId, accountEntity.getAppKey());
                    if (wechatUserEntity != null) {
                        if (StringUtil.isNotBlank(wechatUserEntity.getOpenId())) {
                            String openid = wechatUserEntity.getOpenId();
                            String pagepath = "pages/login/index?tag=1&flowId=";
                            //参数封装
                            String message = WXGZHWebChatUtil.messageJson(templateKId, openid, wxxcxAppId, pagepath, smsMap, title, "2", null);
                            //发送信息
                            retJson = WXGZHWebChatUtil.sendMessage(token, message);
                            JSONObject rstObj = WXGZHWebChatUtil.getMessageList(token);
                            List<WxgzhMessageModel> wxgzhMessageModelList = JsonUtil.getJsonToList(rstObj.get("template_list"), WxgzhMessageModel.class);
                            WxgzhMessageModel messageModel = wxgzhMessageModelList.stream().filter(t -> t.getTemplateId().equals(templateKId)).findFirst().orElse(null);
                            if (ObjectUtil.isNotEmpty(messageModel)) {
                                content = messageModel.getContent();
                                if (StringUtil.isNotBlank(content) && !"null".equals(content)) {
                                    if (ObjectUtil.isNotEmpty(smsMap) && !"null".equals(smsMap)) {
                                        if (content.contains(".DATA}")) {
                                            for (String key : smsMap.keySet()) {
                                                content = content.replace(key, smsMap.get(key).toString());
                                            }
                                        }
                                    }
                                }
                            }
                            //创建消息监控
                            monitorEntity.setContent(content);
                            if (!retJson.getBoolean("code")) {
                                code = false;
                                error = error.append("；").append(userEntity.getRealName() + "：" + retJson.get("error"));
                                messageMonitorService.create(monitorEntity);
                                continue;
                            }
                            messageMonitorService.create(monitorEntity);
                            continue;
                        } else {
                            code = false;
                            error = error.append("；").append(userEntity.getRealName() + "：" + "账号未绑定公众号！");
                            messageMonitorService.create(monitorEntity);
                            continue;
                        }
                    } else {
                        code = false;
                        error = error.append("；").append(userEntity.getRealName() + "：" + "账号未绑定公众号！");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    }
                } else {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：账号配置数据不存在！");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
        }
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            msg = msg.substring(1);
            retJson.put("code", false);
            retJson.put("error", msg);
        }
        return retJson;
    }

    /**
     * 获取系统配置
     */
    private Map<String, String> getSystemConfig() {
        // 获取系统配置
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        Map<String, String> objModel = new HashMap<>(16);
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        return objModel;
    }

    private MessageMonitorEntity createSysMessageMonitor(MessageTemplateConfigEntity msgTemEntity, String content, UserInfo userInfo, List<String> toUserIdsList, String title) {
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setMessageType(msgTemEntity.getMessageType());
        monitorEntity.setMessageSource(msgTemEntity.getMessageSource());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setMessageTemplateId(msgTemEntity.getId());
        monitorEntity.setTitle(title);
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
        monitorEntity.setContent(content);
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        return monitorEntity;
    }

    public static boolean isPhone(String phone) {
        if (StringUtil.isNotBlank(phone) && !"null".equals(phone)) {
            return Pattern.matches("^1[3-9]\\d{9}$", phone);
        }
        return false;
    }
}
