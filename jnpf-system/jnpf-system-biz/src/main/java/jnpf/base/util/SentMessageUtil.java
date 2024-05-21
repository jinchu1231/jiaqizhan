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
import jnpf.config.JnpfOauthConfig;
import jnpf.constant.MsgCode;
import jnpf.message.entity.AccountConfigEntity;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageMonitorEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.message.entity.MessageTemplateConfigEntity;
import jnpf.message.entity.SendConfigTemplateEntity;
import jnpf.message.entity.SendMessageConfigEntity;
import jnpf.message.entity.ShortLinkEntity;
import jnpf.message.entity.WechatUserEntity;
import jnpf.message.enums.MessageTypeEnum;
import jnpf.message.model.EmailModel;
import jnpf.message.model.SentMessageForm;
import jnpf.message.model.WxgzhMessageModel;
import jnpf.message.service.AccountConfigService;
import jnpf.message.service.MessageMonitorService;
import jnpf.message.service.MessageService;
import jnpf.message.service.MessageTemplateConfigService;
import jnpf.message.service.MessagereceiveService;
import jnpf.message.service.SendConfigTemplateService;
import jnpf.message.service.SendMessageConfigService;
import jnpf.message.service.ShortLinkService;
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
import jnpf.util.AuthUtil;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 消息实体类
 *
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/22 9:06
 */
@Component
@Slf4j
public class SentMessageUtil {

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
    private SendMessageConfigService sendMessageConfigService;

    @Autowired
    private SendConfigTemplateService sendConfigTemplateService;

    @Autowired
    private AccountConfigService accountConfigService;

    @Autowired
    private MessageTemplateConfigService messageTemplateConfigService;
    @Autowired
    private MessageMonitorService messageMonitorService;
    @Autowired
    private SmsFieldService smsFieldService;
    @Autowired
    private ShortLinkService shortLinkService;
    @Autowired
    private JnpfOauthConfig jnpfOauthConfig;
    @Autowired
    private WechatUserService wechatUserService;
    @Autowired
    private TemplateParamService templateParamService;
    @Autowired
    protected AuthUtil authUtil;

    /**
     * 发送消息
     *
     * @param sentMessageForm
     */
    public void sendMessage(SentMessageForm sentMessageForm) {
        List<String> toUserIdsList = sentMessageForm.getToUserIds();
        // 模板id
        String templateId = sentMessageForm.getTemplateId();
        // 参数
        Map<String, Object> parameterMap = sentMessageForm.getParameterMap();
        UserInfo userInfo = sentMessageForm.getUserInfo();
        boolean flag = true;
        if (!(toUserIdsList != null && toUserIdsList.size() > 0)) {
            log.error("接收人员为空");
            flag = false;
        }
        if (StringUtil.isEmpty(templateId)) {
            log.error("模板Id为空");
            flag = false;
        }
        if (flag) {
            // 获取发送配置详情
//            MessageTemplateEntity entity = messageTemplateService.getInfo(templateId);
            SendMessageConfigEntity entity = sendMessageConfigService.getInfoByEnCode(templateId);
            if (entity != null) {
                templateId = entity.getId();
            } else {
                entity = sendMessageConfigService.getInfo(templateId);
            }
            if (entity != null) {
                List<SendConfigTemplateEntity> list = sendConfigTemplateService.getDetailListByParentId(templateId);
                if (list != null && list.size() > 0) {
                    for (SendConfigTemplateEntity entity1 : list) {
                        if (parameterMap.get(entity1.getId() + "@Title") == null) {
                            parameterMap.put(entity1.getId() + "@Title", sentMessageForm.getTitle());
                        }
                        if (parameterMap.get(entity1.getId() + "@CreatorUserName") == null) {
                            parameterMap.put(entity1.getId() + "@CreatorUserName", sentMessageForm.getUserInfo().getUserName());
                        }
                        if (parameterMap.get(entity1.getId() + "@FlowLink") == null) {
                            parameterMap.put(entity1.getId() + "@FlowLink", "");
                        }
                        if ("1".equals(String.valueOf(entity1.getEnabledMark()))) {
                            String sendType = entity1.getMessageType();
                            MessageTypeEnum typeEnum = MessageTypeEnum.getByCode(sendType);
                            Map<String, String> contentMsg = sentMessageForm.getContentMsg();
                            switch (typeEnum) {
                                case SysMessage:
                                    // 站内消息、
                                    for (String toUserId : toUserIdsList) {
                                        List<String> toUser = new ArrayList<>();
                                        String content = sentMessageForm.getContent();
                                        MessageTemplateConfigEntity templateConfigEntity = messageTemplateConfigService.getInfo(entity1.getTemplateId());
                                        String title = sentMessageForm.getTitle();
                                        String appLink = "";
                                        if (templateConfigEntity != null) {
                                            title = templateConfigEntity.getTitle();
                                            String msg = contentMsg.get(toUserId) != null ? contentMsg.get(toUserId) : "{}";
                                            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                                            String encode = Base64.getEncoder().encodeToString(bytes);
                                            //流程审批页面链接地址
                                            //流程审批页面链接地址
                                            String pcLink = jnpfOauthConfig.getJnpfFrontDomain() + "/workFlowDetail?config=" + encode;
                                            appLink = jnpfOauthConfig.getJnpfAppDomain() + "/pages/workFlow/flowBefore/index?config=" + encode;
                                            //转换为短链
                                            String shortLink = shortLinkService.shortLink(pcLink + toUserId + templateConfigEntity.getMessageType());
                                            shortLink = getShortLink(pcLink, toUserId, shortLink, templateConfigEntity.getMessageType());
                                            String link = jnpfOauthConfig.getJnpfDomain() + "/api/message/ShortLink/" + shortLink;
                                            if (StringUtil.isNotBlank(userInfo.getTenantId())) {
                                                link = link + "/" + userInfo.getTenantId();
                                            }
                                            if (title.contains("{@FlowLink}")) {
                                                title = title.replace("{@FlowLink}", link + " ");
                                                //链接数据保存
                                                this.saveShortLink(pcLink, appLink, shortLink, userInfo, toUserId, msg);
                                            }
                                            Map<String, Object> msgMap = new HashMap<>();
                                            msgMap = getParamMap(entity1.getId(), parameterMap);
                                            if (StringUtil.isNotEmpty(title)) {
                                                StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                                                title = strSubstitutor.replace(title);
                                            }
                                        }
                                        toUser.add(toUserId);
                                        messageService.sentMessage(toUser, title, content, contentMsg, userInfo);
                                        //消息监控写入
                                        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
                                        monitorEntity.setId(RandomUtil.uuId());
                                        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUser));
                                        monitorEntity.setSendTime(DateUtil.getNowDate());
                                        monitorEntity.setCreatorTime(DateUtil.getNowDate());
                                        monitorEntity.setCreatorUserId(userInfo.getUserId());
                                        createMessageMonitor(monitorEntity, templateConfigEntity, null, null, userInfo, toUser, title);
                                        messageMonitorService.create(monitorEntity);
                                    }
                                    break;
                                case SmsMessage:
                                    // 发送短信
                                    sendSms(toUserIdsList, userInfo, entity1, parameterMap, contentMsg);
                                    break;
                                case MailMessage:
                                    // 邮件
                                    SendMail(toUserIdsList, userInfo, sendType, entity1, parameterMap, contentMsg);
                                    break;
                                case QyMessage:
                                    // 企业微信
                                    JSONObject jsonObject = SendQyWebChat(toUserIdsList, userInfo, sendType, entity1, parameterMap, contentMsg);
                                    if (!(Boolean) jsonObject.get("code")) {
                                        log.error("发送企业微信消息失败，错误：" + jsonObject.get("error"));
                                    }
                                    break;
                                case DingMessage:
                                    // 钉钉
                                    JSONObject jsonObject1 = SendDingTalk(toUserIdsList, userInfo, sendType, entity1, parameterMap, contentMsg);
                                    if (!(Boolean) jsonObject1.get("code")) {
                                        log.error("发送企业微信消息失败，错误：" + jsonObject1.get("error"));
                                    }
                                    break;
                                case WebHookMessage:
                                    // webhook
                                    SendWebHook(sendType, userInfo, entity1, parameterMap, new HashMap<>());
                                    break;
                                case WechatMessage:
                                    // 微信公众号
                                    SendWXGzhChat(toUserIdsList, userInfo, sendType, entity1, contentMsg, parameterMap);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            } else {
                for (String toUserId : toUserIdsList) {
                    List<String> toUser = new ArrayList<>();
                    toUser.add(toUserId);
                    String content = sentMessageForm.getContent();
                    Map<String, String> contentMsg = sentMessageForm.getContentMsg();
                    String title = sentMessageForm.getTitle();
                    messageService.sentMessage(toUser, title, content, contentMsg, userInfo);
                    //消息监控写入
                    MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
                    monitorEntity.setId(RandomUtil.uuId());
                    monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUser));
                    monitorEntity.setSendTime(DateUtil.getNowDate());
                    monitorEntity.setCreatorTime(DateUtil.getNowDate());
                    monitorEntity.setCreatorUserId(userInfo.getUserId());
                    monitorEntity.setMessageType("1");
                    createMessageMonitor(monitorEntity, null, null, null, userInfo, toUser, title);
                    messageMonitorService.create(monitorEntity);
                }
            }
        }
    }


    public void SendWebHook(String sendType, UserInfo userInfo, SendConfigTemplateEntity entity, Map<String, Object> parameterMap, Map<String, String> contentMsg) {
        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(entity.getTemplateId());
        AccountConfigEntity accountEntity = accountConfigService.getInfo(entity.getAccountConfigId());
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        String content = msgTemEntity.getContent();
        //获取消息模板参数
        parameterMap = getParamMap(entity.getId(), parameterMap);
        // 替换参数
        if (StringUtil.isNotEmpty(content)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            content = strSubstitutor.replace(content);
        }
        String title = msgTemEntity.getTitle();
        if (StringUtil.isNotEmpty(title)) {
            StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
            title = strSubstitutor.replace(title);
        }
        title = systemParam(parameterMap, contentMsg, title, userInfo);
        content = systemParam(parameterMap, contentMsg, content, userInfo);
        if (entity != null) {
            if (accountEntity != null) {
                //创建消息监控
                monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, accountEntity, content, userInfo, null, title);
                messageMonitorService.create(monitorEntity);
                switch (accountEntity.getWebhookType()) {
                    case "1":
                        //钉钉
                        if ("1".equals(accountEntity.getApproveType())) {
                            WebHookUtil.sendDDMessage(accountEntity.getWebhookAddress(), content);
                        } else if ("2".equals(accountEntity.getApproveType())) {
                            WebHookUtil.sendDingDing(accountEntity.getWebhookAddress(), accountEntity.getBearer(), content);
                        }
                        break;
                    case "2":
                        if ("1".equals(accountEntity.getApproveType())) {
                            WebHookUtil.callWeChatBot(accountEntity.getWebhookAddress(), content);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, null, content, userInfo, null, title);
                messageMonitorService.create(monitorEntity);
            }
        } else {
            monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, null, content, userInfo, null, title);
            messageMonitorService.create(monitorEntity);
        }
    }

    /**
     * 发送企业微信消息
     *
     * @param toUserIdsList
     * @param userInfo
     * @param sendType
     * @param entity
     * @param parameterMap
     * @return
     */
    public JSONObject SendQyWebChat(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateEntity entity, Map<String, Object> parameterMap, Map<String, String> contentMsg) {
        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(entity.getTemplateId());

        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();
        // 获取接收人员的企业微信号、创建消息用户实体
        for (String userId : toUserIdsList) {
            error = new StringBuilder();
            MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
            monitorEntity.setId(RandomUtil.uuId());
            monitorEntity.setSendTime(DateUtil.getNowDate());
            monitorEntity.setCreatorTime(DateUtil.getNowDate());
            monitorEntity.setCreatorUserId(userInfo.getUserId());
            if (StringUtil.isEmpty(userId)) {
                code = false;
                error = error.append("；").append("接收人为空！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            monitorEntity.setReceiveUser(userId);
            UserEntity userEntity = userService.getInfo(userId);
            if (ObjectUtil.isEmpty(userEntity)) {
                code = false;
                error = error.append("；").append("用户不存在！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            if (msgTemEntity != null) {
                //获取消息模板参数
                Map<String, Object> msgMap = getParamMap(entity.getId(), parameterMap);
                // 替换参数
                String content = msgTemEntity.getContent();
                String msg = contentMsg.get(userId) != null ? contentMsg.get(userId) : "{}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                String encode = Base64.getEncoder().encodeToString(bytes);
                //流程审批页面链接地址
                String pcLink = jnpfOauthConfig.getJnpfFrontDomain() + "/workFlowDetail?config=" + encode;
                String appLink = jnpfOauthConfig.getJnpfAppDomain() + "/pages/workFlow/flowBefore/index?config=" + encode;
                //转换为短链
                String shortLink = shortLinkService.shortLink(pcLink + userId + msgTemEntity.getMessageType());
                shortLink = getShortLink(pcLink, userId, shortLink, msgTemEntity.getMessageType());
                String msgTC = msgTemEntity.getTitle() + msgTemEntity.getContent();
                if (StringUtil.isNotBlank(msgTC)) {
                    if (msgTC.contains("{@FlowLink}")) {
                        //链接数据保存
                        this.saveShortLink(pcLink, appLink, shortLink, userInfo, userId, msg);
                    }
                }
                String link = jnpfOauthConfig.getJnpfDomain() + "/api/message/ShortLink/" + shortLink;
                if (StringUtil.isNotBlank(userInfo.getTenantId())) {
                    link = link + "/" + userInfo.getTenantId();
                }
                if (StringUtil.isNotEmpty(content)) {
                    if (content.contains("{@FlowLink}")) {
                        content = content.replace("{@FlowLink}", link + " ");
                    }
                    StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                    content = strSubstitutor.replace(content);
                }

                // 替换参数
                String title = msgTemEntity.getTitle();
                if (StringUtil.isNotEmpty(title)) {
                    if (title.contains("{@FlowLink}")) {
                        title = title.replace("{@FlowLink}", link + " ");
                    }
                    StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                    title = strSubstitutor.replace(title);
                }
                title = systemParam(parameterMap, contentMsg, title, userInfo);
                content = systemParam(parameterMap, contentMsg, content, userInfo);
                monitorEntity.setTitle(title);
                monitorEntity.setContent(content);
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
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                if (StringUtil.isEmpty(corpSecret)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：Secret为空！");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                if (StringUtil.isEmpty(agentId)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AgentId为空！");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                if (StringUtil.isEmpty(content)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：内容为空！");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                // 创建消息实体
                MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), content, null, Integer.parseInt(sendType));
                //创建消息监控
                monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, null, content, userInfo, null, title);
                // 获取接收人员的企业微信号、创建消息用户实体
//                    for (String userId : toUserIdsList) {
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
//                    }

                // 处理企业微信号信息串并验证
                toUserIdAll = toWxUserId.toString();
                if (StringUtil.isNotEmpty(toUserIdAll)) {
                    toUserIdAll = toUserIdAll.substring(1);
                }
                if (StringUtil.isEmpty(toUserIdAll)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：接收人对应的企业微信号全部为空！");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                // 发送企业信息信息
                retJson = QyWebChatUtil.sendWxMessage(corpId, corpSecret, agentId, toUserIdAll, content);
                if (!retJson.getBoolean("code")) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：" + retJson.get("error"));
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                // 批量发送企业信息信息
//                    retJson = QyWebChatUtil.sendWxMessage(corpId, corpSecret, agentId, toUserIdAll, content);
//                    messageMonitorService.create(monitorEntity);
//                    if (!retJson.getBoolean("code")) {
//                        return retJson;
//                    }

                    // 企业微信号为空的信息写入备注
                    if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
                        messageEntity.setExcerpt(nullUserInfo.substring(1) + "对应的企业微信号为空");
                    }
                    messageMonitorService.create(monitorEntity);
                    continue;
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
            if (StringUtil.isNotBlank(msg)) {
                msg = msg.substring(1);
            }
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
     * @param entity
     * @param parameterMap
     * @return
     */
    public JSONObject SendDingTalk(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateEntity entity, Map<String, Object> parameterMap, Map<String, String> contentMsg) {
        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(entity.getTemplateId());

        boolean code = true;
        StringBuilder error = new StringBuilder();
        JSONObject retJson = new JSONObject();
        for (String userId : toUserIdsList) {
            error = new StringBuilder();
            //消息监控
            MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
            monitorEntity.setId(RandomUtil.uuId());
            monitorEntity.setSendTime(DateUtil.getNowDate());
            monitorEntity.setCreatorTime(DateUtil.getNowDate());
            monitorEntity.setCreatorUserId(userInfo.getUserId());
            monitorEntity.setReceiveUser(userId);
            if (StringUtil.isEmpty(userId)) {
                code = false;
                error = error.append("；").append("接收人为空!");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            UserEntity userEntity = userService.getInfo(userId);
            if (ObjectUtil.isEmpty(userEntity)) {
                code = false;
                error = error.append("；").append("用户不存在！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            if (msgTemEntity != null) {
                String content = msgTemEntity.getContent();
                //获取消息模板参数
                Map<String, Object> msgMap = getParamMap(entity.getId(), parameterMap);
                //转换链接
                String msg = contentMsg.get(userId) != null ? contentMsg.get(userId) : "{}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                String encode = Base64.getEncoder().encodeToString(bytes);
                //流程审批页面链接地址
                String pcLink = jnpfOauthConfig.getJnpfFrontDomain() + "/workFlowDetail?config=" + encode;
                String appLink = jnpfOauthConfig.getJnpfAppDomain() + "/pages/workFlow/flowBefore/index?config=" + encode;
                //转换为短链
                String shortLink = shortLinkService.shortLink(pcLink + userId + msgTemEntity.getMessageType());
                shortLink = getShortLink(pcLink, userId, shortLink, msgTemEntity.getMessageType());
                String msgTC = msgTemEntity.getTitle() + msgTemEntity.getContent();
                if (StringUtil.isNotBlank(msgTC)) {
                    if (msgTC.contains("{@FlowLink}")) {
                        //链接数据保存
                        this.saveShortLink(pcLink, appLink, shortLink, userInfo, userId, msg);
                    }
                }
                String link = jnpfOauthConfig.getJnpfDomain() + "/api/message/ShortLink/" + shortLink;
                if (StringUtil.isNotBlank(userInfo.getTenantId())) {
                    link = link + "/" + userInfo.getTenantId();
                }
                if (StringUtil.isNotEmpty(content)) {
                    if (content.contains("{@FlowLink}")) {
                        content = content.replace("{@FlowLink}", link + " ");
                    }
                    StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                    content = strSubstitutor.replace(content);
                }
                // 替换参数
                String title = msgTemEntity.getTitle();
                if (StringUtil.isNotEmpty(title)) {
                    if (title.contains("{@FlowLink}")) {
                        title = title.replace("{@FlowLink}", link + " ");
                    }
                    StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                    title = strSubstitutor.replace(title);
                }

                title = systemParam(parameterMap, contentMsg, title, userInfo);
                content = systemParam(parameterMap, contentMsg, content, userInfo);
                monitorEntity.setTitle(title);
                monitorEntity.setContent(content);

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
                    error = error.append("；").append(userEntity.getRealName() + "：AppKey为空!");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                if (StringUtil.isEmpty(appSecret)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AppSecret为空!");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                if (StringUtil.isEmpty(agentId)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AgentId为空!");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                if (StringUtil.isEmpty(content)) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：AgentId为空!");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                // 创建消息实体
                MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), content, null, Integer.parseInt(sendType));
                //创建消息监控
                monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, null, content, userInfo, null, title);
                // 获取接收人员的钉钉号、创建消息用户实体
//                    for (String userId : toUserIdsList) {
                dingUserId = "";
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
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
                // 发送钉钉信息
                retJson = DingTalkUtil.sendDingMessage(appKey, appSecret, agentId, toUserIdAll, content);
                if (!retJson.getBoolean("code")) {
                    code = false;
                    error = error.append("；").append(userEntity.getRealName() + "：" + retJson.get("error"));
                    messageMonitorService.create(monitorEntity);
                    continue;
                }

                // 钉钉号为空的信息写入备注
                if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
                    messageEntity.setExcerpt(nullUserInfo.toString().substring(1) + "对应的钉钉号为空");
                }
                messageMonitorService.create(monitorEntity);
                continue;
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在");
                messageMonitorService.create(monitorEntity);
                continue;
            }
        }
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            if (StringUtil.isNotBlank(msg)) {
                msg = msg.substring(1);
            }
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
     * @param entity
     * @param parameterMap
     * @return
     */
    public void SendMail(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateEntity entity, Map<String, Object> parameterMap, Map<String, String> contentMsg) {
        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(entity.getTemplateId());
        AccountConfigEntity accountEntity = accountConfigService.getInfo(entity.getAccountConfigId());
        for (String userId : toUserIdsList) {
            //消息监控
            MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
            monitorEntity.setId(RandomUtil.uuId());
            monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
            monitorEntity.setSendTime(DateUtil.getNowDate());
            monitorEntity.setCreatorTime(DateUtil.getNowDate());
            monitorEntity.setCreatorUserId(userInfo.getUserId());
            if (StringUtil.isEmpty(userId)) {
                log.error("接收人为空");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            monitorEntity.setReceiveUser(userId);
            UserEntity userEntity = userService.getInfo(userId);
            if (msgTemEntity != null) {
                String msg = contentMsg.get(userId) != null ? contentMsg.get(userId) : "{}";
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                String encode = Base64.getEncoder().encodeToString(bytes);
                //流程审批页面链接地址
                String pcLink = jnpfOauthConfig.getJnpfFrontDomain() + "/workFlowDetail?config=" + encode;
                String appLink = jnpfOauthConfig.getJnpfAppDomain() + "/pages/workFlow/flowBefore/index?config=" + encode;
                //转换为短链
                String shortLink = shortLinkService.shortLink(pcLink + userId + msgTemEntity.getMessageType());
                shortLink = getShortLink(pcLink, userId, shortLink, msgTemEntity.getMessageType());
                String msgTC = msgTemEntity.getTitle() + msgTemEntity.getContent();
                if (StringUtil.isNotBlank(msgTC)) {
                    if (msgTC.contains("{@FlowLink}")) {
                        //链接数据保存
                        this.saveShortLink(pcLink, appLink, shortLink, userInfo, userId, msg);
                    }
                }
                String link = jnpfOauthConfig.getJnpfDomain() + "/api/message/ShortLink/" + shortLink;
                if (StringUtil.isNotBlank(userInfo.getTenantId())) {
                    link = link + "/" + userInfo.getTenantId();
                }
                Map<String, Object> msgMap = getParamMap(entity.getId(), parameterMap);
                // 设置邮件标题
                String title = msgTemEntity.getTitle();
                if (title.contains("{@FlowLink}")) {
                    title = title.replace("{@FlowLink}", link + " ");
                }
                if (StringUtil.isNotEmpty(title)) {
                    StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                    title = strSubstitutor.replace(title);
                }
                // 设置邮件内容
                String content = msgTemEntity.getContent();
                if (content.contains("{@FlowLink}")) {
                    content = content.replace("{@FlowLink}", link + " ");
                }
                //获取消息模板参数
                if (StringUtil.isNotEmpty(content)) {
                    StringSubstitutor strSubstitutor = new StringSubstitutor(msgMap, "{", "}");
                    content = strSubstitutor.replace(content);
                }
                title = systemParam(parameterMap, contentMsg, title, userInfo);
                content = systemParam(parameterMap, contentMsg, content, userInfo);
                monitorEntity.setTitle(title);
                monitorEntity.setContent(content);
                if (accountEntity != null) {
                    // 获取系统配置
                    Map<String, String> objModel = new HashMap<>();
                    objModel.put("emailSmtpHost", accountEntity.getSmtpServer());
                    objModel.put("emailSmtpPort", accountEntity.getSmtpPort().toString());
                    objModel.put("emailSenderName", accountEntity.getAddressorName());
                    objModel.put("emailAccount", accountEntity.getSmtpUser());
                    objModel.put("emailPassword", accountEntity.getSmtpPassword());
                    objModel.put("emailSsl", accountEntity.getSslLink().equals("1") ? "true" : "false");

                    EmailModel emailModel = JsonUtil.getJsonToBean(objModel, EmailModel.class);
                    StringBuilder nullUserInfo = new StringBuilder();
                    List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();
                    StringBuilder toUserMail = new StringBuilder();
                    String userEmailAll = "";
                    String userEmail = "";
                    String userName = "";

                    // 相关参数验证
                    if (StringUtil.isEmpty(emailModel.getEmailSmtpHost())) {
                        log.error("SMTP服务为空");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    } else if (StringUtil.isEmpty(emailModel.getEmailSmtpPort())) {
                        log.error("SMTP端口为空");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    } else if (StringUtil.isEmpty(emailModel.getEmailAccount())) {
                        log.error("发件人邮箱为空");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    } else if (StringUtil.isEmpty(emailModel.getEmailPassword())) {
                        log.error("发件人密码为空");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    } else {
                        // 设置邮件标题
                        emailModel.setEmailTitle(title);
                        // 设置邮件内容
                        emailModel.setEmailContent(content);

                        // 创建消息实体
                        MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), title, emailModel.getEmailContent(), Integer.parseInt(sendType));
                        //创建消息监控
                        monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, accountEntity, content, userInfo, null, title);
                        // 获取收件人的邮箱地址、创建消息用户实体
//                        for (String userId : toUserIdsList) {
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
                        messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(userId, title, Integer.parseInt(sendType)));
//                        }

                        // 处理接收人员的邮箱信息串并验证
                        userEmailAll = toUserMail.toString();
                        if (StringUtil.isNotEmpty(userEmailAll)) {
                            userEmailAll = userEmailAll.substring(1);
                        }
                        if (StringUtil.isEmpty(userEmailAll)) {
                            log.error("接收人对应的邮箱格式错误");
                            messageMonitorService.create(monitorEntity);
                            continue;
                        } else {
                            // 设置接收人员
                            emailModel.setEmailToUsers(userEmailAll);
                            // 发送邮件
                            JSONObject retJson = EmailUtil.sendMail(emailModel);
                            messageMonitorService.create(monitorEntity);
                            if (!retJson.getBoolean("code")) {
                                log.error("发送失败");
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
                }
                continue;
            }
            continue;
        }
    }

    /**
     * 发送短信
     *
     * @param toUserIdsList
     * @param entity
     * @param parameterMap
     * @return
     */
    public void sendSms(List<String> toUserIdsList, UserInfo userInfo, SendConfigTemplateEntity entity, Map<String, Object> parameterMap, Map<String, String> contentMsg) {
        //获取短信配置
        AccountConfigEntity accountEntity = accountConfigService.getInfo(entity.getAccountConfigId());
        // 获取消息模板详情
        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(entity.getTemplateId());
//        // 得到参数详情列表
//        List<TemplateParamEntity> detailListByParentId = templateParamService.getDetailListByParentId(msgTemEntity.getId());
//        detailListByParentId.forEach(t-> {
//
//        });
        for (String toUserId : toUserIdsList) {
            //消息监控
            MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
            monitorEntity.setId(RandomUtil.uuId());
            monitorEntity.setSendTime(DateUtil.getNowDate());
            monitorEntity.setCreatorTime(DateUtil.getNowDate());
            monitorEntity.setCreatorUserId(userInfo.getUserId());
            monitorEntity.setReceiveUser(toUserId);
            String msg = contentMsg.get(toUserId) != null ? contentMsg.get(toUserId) : "{}";
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            String encode = Base64.getEncoder().encodeToString(bytes);
            //流程审批页面链接地址
            //流程审批页面链接地址
            String pcLink = jnpfOauthConfig.getJnpfFrontDomain() + "/workFlowDetail?config=" + encode;
            String appLink = jnpfOauthConfig.getJnpfAppDomain() + "/pages/workFlow/flowBefore/index?config=" + encode;
            //转换为短链
            String shortLink = shortLinkService.shortLink(pcLink + toUserId + msgTemEntity.getMessageType());
            shortLink = getShortLink(pcLink, toUserId, shortLink, msgTemEntity.getMessageType());
            //发送给用户的链接
            String link = jnpfOauthConfig.getJnpfDomain() + "/api/message/ShortLink/" + shortLink;
            if (StringUtil.isNotBlank(userInfo.getTenantId())) {
                link = link + "/" + userInfo.getTenantId();
            }
            //转换为短链

            if (accountEntity != null) {
                monitorEntity.setAccountId(accountEntity.getId());
                //账号配置——短信
                Map<String, String> objModel = new HashMap<>(16);
                objModel.put("aliAccessKey", accountEntity.getAppId());
                objModel.put("aliSecret", accountEntity.getAppSecret());
                objModel.put("tencentSecretId", accountEntity.getAppId());
                objModel.put("tencentSecretKey", accountEntity.getAppSecret());
                objModel.put("tencentAppId", accountEntity.getSdkAppId());
                objModel.put("tencentAppKey", accountEntity.getAppKey());
                SmsModel smsConfig = JsonUtil.getJsonToBean(objModel, SmsModel.class);
                int company = Integer.parseInt(accountEntity.getChannel());
                // 组装接受用户
                StringBuffer toUserIdList = new StringBuffer();
//                for (String toUserId : toUserIdsList) {
                UserEntity userEntity = userService.getInfo(toUserId);
                if (isPhone(userEntity.getMobilePhone())) {
                    toUserIdList.append(userEntity.getMobilePhone() + ",");
                }
//                }
                //获取消息模板参数
                Map<String, Object> msgMap = getParamMap(entity.getId(), parameterMap);
//                if(parameterMap.containsKey("@flowLink")){
//                    parameterMap.put("@flowLink",link);
//                }
                //短信参数
                Map<String, Object> smsMap = new HashMap<>();
                if (entity != null) {
                    smsMap = smsFieldService.getParamMap(entity.getTemplateId(), msgMap);
                    if (ObjectUtil.isNotEmpty(smsMap)) {
                        if (smsMap.containsValue("@FlowLink")) {
                            //链接数据保存
                            this.saveShortLink(pcLink, appLink, shortLink, userInfo, toUserId, msg);
                            for (String key : smsMap.keySet()) {
                                if (smsMap.get(key).equals("@FlowLink")) {
                                    smsMap.put(key, link + " ");
                                }
                            }
                        }
                    }
                }
//                if(smsMap.containsKey("title")) {
//                    smsMap.keySet().removeIf(k -> k.equals("title"));
//                }
                if (msgTemEntity != null) {
                    monitorEntity.setMessageTemplateId(msgTemEntity.getId());
                    String endPoint = "";
                    if ("1".equals(accountEntity.getChannel())) {
                        endPoint = accountEntity.getEndPoint();
                    } else if ("2".equals(accountEntity.getChannel())) {
                        endPoint = accountEntity.getZoneName();
                    }
                    String content = SmsUtil.querySmsTemplateContent(company, smsConfig, endPoint, accountEntity.getZoneParam(), msgTemEntity.getTemplateCode());
                    if (StringUtil.isNotBlank(content) && !"null".equals(content)) {
                        if (ObjectUtil.isNotEmpty(smsMap) && !"null".equals(smsMap)) {
                            if ("1".equals(accountEntity.getChannel())) {
                                if (content.contains("${")) {
                                    for (String key : smsMap.keySet()) {
                                        content = content.replace("${" + key + "}", smsMap.get(key).toString());
                                    }
                                }
                            } else if ("2".equals(accountEntity.getChannel())) {
                                if (content.contains("{")) {
                                    for (String key : smsMap.keySet()) {
                                        content = content.replace("{" + key + "}", smsMap.get(key).toString());
                                    }
                                }
                            }
                        }
                    }
                    //创建消息监控
                    monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, accountEntity, content, userInfo, null, null);
                    if (StringUtil.isEmpty(toUserIdList)) {
                        log.error("全部接收人对应的手机号码格式错误");
                        messageMonitorService.create(monitorEntity);
                        continue;
                    }
                    SmsUtil.sentSms(company, smsConfig, endPoint, accountEntity.getZoneParam(), toUserIdList.toString(), accountEntity.getSmsSignature(), msgTemEntity.getTemplateCode(), smsMap);
                    messageMonitorService.create(monitorEntity);
                    continue;
                } else {
                    log.error("消息模板数据不存在");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
            } else {
                log.error("账号配置数据不存在");
                messageMonitorService.create(monitorEntity);
                continue;
            }
        }
    }

    /**
     * 发送微信公众号消息
     *
     * @param toUserIdsList
     * @param userInfo
     * @param sendType
     * @param entity
     * @param parameterMap
     * @return
     */
    public JSONObject SendWXGzhChat(List<String> toUserIdsList, UserInfo userInfo, String sendType, SendConfigTemplateEntity entity, Map<String, String> contentMsg, Map<String, Object> parameterMap) {
        //获取短信配置
        AccountConfigEntity accountEntity = accountConfigService.getInfo(entity.getAccountConfigId());
        // 获取消息模板详情
        MessageTemplateConfigEntity msgTemEntity = messageTemplateConfigService.getInfo(entity.getTemplateId());
        //消息监控
        JSONObject retJson = new JSONObject();
        boolean code = true;
        StringBuilder error = new StringBuilder();
        for (String userId : toUserIdsList) {
            MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
            monitorEntity.setId(RandomUtil.uuId());
            monitorEntity.setSendTime(DateUtil.getNowDate());
            monitorEntity.setCreatorTime(DateUtil.getNowDate());
            monitorEntity.setCreatorUserId(userInfo.getUserId());
            error = new StringBuilder();
            if (StringUtil.isEmpty(userId)) {
                code = false;
                error = error.append("；").append("接收人为空！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            UserEntity userEntity = userService.getById(userId);
            if (ObjectUtil.isEmpty(userEntity)) {
                code = false;
                error = error.append("；").append("用户不存在！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            monitorEntity.setReceiveUser(userId);

            if (ObjectUtil.isEmpty(msgTemEntity)) {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：消息模板数据不存在！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            monitorEntity.setMessageTemplateId(msgTemEntity.getId());
            String content = msgTemEntity.getContent();
            String templateKId = msgTemEntity.getTemplateCode();
            if (ObjectUtil.isEmpty(accountEntity)) {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：公众号账号配置数据不存在！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            monitorEntity.setAccountId(accountEntity.getId());
            //创建消息监控
            monitorEntity = createMessageMonitor(monitorEntity, msgTemEntity, accountEntity, content, userInfo, toUserIdsList, null);
            // 获取系统配置
            String appId = accountEntity.getAppId();
            String appsecret = accountEntity.getAppSecret();
            String wxxcxAppId = msgTemEntity.getXcxAppId();
            String type = msgTemEntity.getWxSkip();

            String title = "";
            //获取消息模板参数
            Map<String, Object> msgMap = getParamMap(entity.getId(), parameterMap);
//            if(parameterMap.containsKey("@flowLink")){
//                parameterMap.put("@flowLink",link);
//            }
            //微信公众号参数
            Map<String, Object> smsMap = new HashMap<>();
            if (entity != null) {
                smsMap = smsFieldService.getParamMap(entity.getTemplateId(), msgMap);
            }
            if (smsMap.containsKey("title")) {
                title = smsMap.get("title").toString();
                smsMap.keySet().removeIf(k -> k.equals("title"));
            }
            monitorEntity.setTitle(title);
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
            String msg = contentMsg.get(userId) != null ? contentMsg.get(userId) : "{}";
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            String encode = Base64.getEncoder().encodeToString(bytes);
            //流程审批页面链接地址
            String pcLink = jnpfOauthConfig.getJnpfFrontDomain() + "/workFlowDetail?config=" + encode;
            String appLink = jnpfOauthConfig.getJnpfAppDomain() + "/pages/workFlow/flowBefore/index?config=" + encode;
            //转换为短链
            String shortLink = shortLinkService.shortLink(pcLink + userId + msgTemEntity.getMessageType());
            shortLink = getShortLink(pcLink, userId, shortLink, msgTemEntity.getMessageType());
            if (!"1".equals(type)) {
                //链接数据保存
                this.saveShortLink(pcLink, appLink, shortLink, userInfo, userId, msg);
            }
            String link = jnpfOauthConfig.getJnpfDomain() + "/api/message/ShortLink/" + shortLink;
            if (StringUtil.isNotBlank(userInfo.getTenantId())) {
                link = link + "/" + userInfo.getTenantId();
            }
            if (ObjectUtil.isNotEmpty(smsMap)) {
                for (String key : smsMap.keySet()) {
                    if (smsMap.get(key).equals("@FlowLink")) {
                        smsMap.put(key, link);
                    }
                }
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：公众号模板参数为空！");
                messageMonitorService.create(monitorEntity);
                continue;
            }
            WechatUserEntity wechatUserEntity = wechatUserService.getInfoByGzhId(userId, accountEntity.getAppKey());
            if (wechatUserEntity != null) {
                if (StringUtil.isNotBlank(wechatUserEntity.getOpenId())) {
                    String openid = wechatUserEntity.getOpenId();
                    String apptoken = authUtil.loginTempUser(userId, userInfo.getTenantId());
                    String pagepath = "/pages/workFlow/flowBefore/index?config=" + encode + "&token=" + apptoken;
                    if (ObjectUtil.isNotEmpty(smsMap)) {
                        //参数封装
                        String message = WXGZHWebChatUtil.messageJson(templateKId, openid, wxxcxAppId, pagepath, smsMap, title, type, link);
                        //发送信息
                        retJson = WXGZHWebChatUtil.sendMessage(token, message);
                    }
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
                    error = error.append("；").append(userEntity.getRealName() + "：" + "账号未绑定公众号");
                    messageMonitorService.create(monitorEntity);
                    continue;
                }
            } else {
                code = false;
                error = error.append("；").append(userEntity.getRealName() + "：" + "账号未绑定公众号");
                messageMonitorService.create(monitorEntity);
                continue;
            }
        }
        if (code) {
            retJson.put("code", true);
            retJson.put("error", MsgCode.SU012.get());
        } else {
            String msg = error.toString();
            if (StringUtil.isNotBlank(msg)) {
                msg = msg.substring(1);
            }
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

    private Map<String, Object> getParamMap(String templateId, Map<String, Object> paramMap) {
        Map<String, Object> map = new HashMap<>();
        for (String key : paramMap.keySet()) {
            if (key.contains(templateId)) {
                map.put(key.substring(templateId.length()), paramMap.get(key));
            }
        }
        return map;
    }

    private MessageMonitorEntity createMessageMonitor(MessageMonitorEntity monitorEntity, MessageTemplateConfigEntity msgTemEntity, AccountConfigEntity accountEntity, String content, UserInfo userInfo, List<String> toUserIdsList, String title) {
        if (msgTemEntity != null) {
            monitorEntity.setMessageTemplateId(msgTemEntity.getId());
            monitorEntity.setMessageSource(msgTemEntity.getMessageSource());
            if (StringUtil.isNotBlank(title)) {
                monitorEntity.setTitle(title);
            } else {
                monitorEntity.setTitle(msgTemEntity.getTitle());
            }
            monitorEntity.setMessageType(msgTemEntity.getMessageType());
            if ("6".equals(msgTemEntity.getMessageType()) && accountEntity != null) {
                monitorEntity.setReceiveUser(accountEntity.getWebhookAddress());
            } else {
                if (toUserIdsList != null && toUserIdsList.size() > 0) {
                    monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIdsList));
                }
            }
        } else {
            if (StringUtil.isNotBlank(title)) {
                monitorEntity.setTitle(title);
            }
            monitorEntity.setMessageType("1");
        }
        if (accountEntity != null) {
            monitorEntity.setAccountId(accountEntity.getId());
            monitorEntity.setAccountCode(accountEntity.getEnCode());
            monitorEntity.setAccountName(accountEntity.getFullName());
        }
        monitorEntity.setContent(content);
        return monitorEntity;
    }

    private String getShortLink(String pcLink, String userId, String shortLink, String type) {
        if (StringUtil.isNotBlank(shortLink)) {
            ShortLinkEntity entity = shortLinkService.getInfoByLink(shortLink);
            if (entity != null) {
                if (pcLink.equals(entity.getRealPcLink())) {
                    return shortLink;
                } else {
                    shortLink = shortLinkService.shortLink(pcLink + userId + type);
                    return getShortLink(pcLink, userId, shortLink, type);
                }
            } else {
                return shortLink;
            }
        } else {
            shortLink = shortLinkService.shortLink(pcLink + userId + type);
            return getShortLink(pcLink, userId, shortLink, type);
        }
    }

    private void saveShortLink(String pcLink, String appLink, String shortLink, UserInfo userInfo, String userId, String bodyText) {
        ShortLinkEntity shortLinkEntity = shortLinkService.getInfoByLink(shortLink);
        if (shortLinkEntity == null) {
            ShortLinkEntity entity = new ShortLinkEntity();
            Map<String, String> sysConfig = getSystemConfig();
            String linkTime = sysConfig.get("linkTime");
            Integer isClick = 0;
            if (StringUtil.isNotBlank(sysConfig.get("isClick")) && !"null".equals(sysConfig.get("isClick"))) {
                isClick = Integer.parseInt(sysConfig.get("isClick"));
            }
            int unClickNum = 20;
            if (StringUtil.isNotBlank(sysConfig.get("unClickNum")) && !"null".equals(sysConfig.get("unClickNum"))) {
                unClickNum = Integer.parseInt(sysConfig.get("unClickNum"));
            }
            entity.setId(RandomUtil.uuId());
            entity.setRealPcLink(pcLink);
            entity.setRealAppLink(appLink);
            entity.setShortLink(shortLink);
            entity.setBodyText(bodyText);
//            entity.setTenantId(userInfo.getTenantId());
            entity.setUserId(userId);
            entity.setIsUsed(isClick);
            entity.setUnableNum(unClickNum);
            entity.setClickNum(0);
            if (StringUtil.isNotEmpty(linkTime)) {
                Date unableTime = getUnableTime(linkTime);
                entity.setUnableTime(unableTime);
            } else {
                entity.setUnableTime(DateUtil.dateAddHours(DateUtil.getNowDate(), 24));
            }
            entity.setCreatorTime(DateUtil.getNowDate());
            entity.setCreatorUserId(userInfo.getUserId());
            shortLinkService.save(entity);
        }
    }

    private Date getUnableTime(String linkTime) {
        Double time = Double.parseDouble(linkTime);
        int second = Double.valueOf(time * 60 * 60).intValue();
        Date unableTime = DateUtil.dateAddSeconds(DateUtil.getNowDate(), second);
        return unableTime;
    }

    public static boolean isPhone(String phone) {
        if (StringUtil.isNotBlank(phone) && !"null".equals(phone)) {
            return Pattern.matches("^1[3-9]\\d{9}$", phone);
        }
        return false;
    }

    public void sendDelegateMsg(SentMessageForm sentMessageForm){
        messageService.sentScheduleMessage(sentMessageForm,"");
    }

    /**
     * 系统参数替换
     *
     * @param parameterMap
     * @param contentMsg
     * @param title
     * @param userInfo
     * @return
     */
    private String systemParam(Map<String, Object> parameterMap, Map<String, String> contentMsg, String title, UserInfo userInfo) {
        if (parameterMap.isEmpty()) {
            return title = title.replaceAll("\\{@Title}", contentMsg.get("Title"))
                    .replaceAll("\\{@CreatorUserName}", userInfo.getUserName())
                    .replaceAll("\\{@Content}", contentMsg.get("Content"))
                    .replaceAll("\\{@Remark}", contentMsg.get("Remark"))
                    .replaceAll("\\{@StartDate}", contentMsg.get("StartDate"))
                    .replaceAll("\\{@StartTime}", contentMsg.get("StartTime"))
                    .replaceAll("\\{@EndDate}", contentMsg.get("EndDate"))
                    .replaceAll("\\{@FlowLink}", "")
                    .replaceAll("\\{@EndTime}", contentMsg.get("EndTime"));
        }
        return title;
    }

}
