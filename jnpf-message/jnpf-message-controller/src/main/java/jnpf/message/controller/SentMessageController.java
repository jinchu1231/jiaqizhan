package jnpf.message.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.model.synthirdinfo.DingTalkModel;
import jnpf.base.model.synthirdinfo.QyWebChatModel;
import jnpf.base.service.MessageTemplateService;
import jnpf.message.entity.SendConfigTemplateEntity;
import jnpf.message.entity.SendMessageConfigEntity;
import jnpf.message.model.SentFlowMessageModel;
import jnpf.message.model.SentMessageForm;
import jnpf.message.service.MessageService;
import jnpf.message.service.SendConfigTemplateService;
import jnpf.message.service.SendMessageConfigService;
import jnpf.message.util.OnlineUserProvider;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.third.DingTalkUtil;
import jnpf.util.third.QyWebChatUtil;
import jnpf.util.wxutil.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 发送消息模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/21 10:28
 */
@Tag(name = "发送消息", description = "MessageAll")
@RestController
@RequestMapping("/api/message/MessageAll")
@Slf4j
public class SentMessageController  {


    @Autowired
    private MessageTemplateService messageTemplateService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private MessageService messageService;
    @Autowired
    private SendMessageConfigService sendMessageConfigService;

    @Autowired
    private SendConfigTemplateService sendConfigTemplateService;


    /**
     * @param sentMessageForm
     * @return
     */
    
    @PostMapping
    public void sendMessage(@RequestBody SentMessageForm sentMessageForm) {
        List<String> toUserIdsList = sentMessageForm.getToUserIds();
        // 模板id
        String templateId = sentMessageForm.getTemplateId();
        // 参数
        Map<String, Object> parameterMap = sentMessageForm.getParameterMap();
        UserInfo userInfo = sentMessageForm.getUserInfo();
        if (userInfo == null) {
            userInfo = userProvider.get();
        }
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
            //获取发送配置详情
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
                        if(parameterMap.get(entity1.getId()+"@Title")==null){
                            parameterMap.put(entity1.getId()+"@Title",sentMessageForm.getTitle());
                        }
                        if(parameterMap.get(entity1.getId()+"@CreatorUserName")==null){
                            parameterMap.put(entity1.getId()+"@CreatorUserName",sentMessageForm.getUserInfo().getUserName());
                        }
                        if(parameterMap.get(entity1.getId()+"@FlowLink")==null){
                            parameterMap.put(entity1.getId()+"@FlowLink","");
                        }
                        String sendType = entity1.getMessageType();
                        // 构建消息模型
                        SentFlowMessageModel sentFlowMessageModel = new SentFlowMessageModel(toUserIdsList, entity1, sentMessageForm.getContent(), parameterMap, userInfo,sendType, sentMessageForm.getTitle(),sentMessageForm.getFlowName(),sentMessageForm.getUserName());
                        sentFlowMessageModel.setContentMsg(sentMessageForm.getContentMsg());
//                        source.output().send(
                                MessageBuilder.withPayload(
                                        JsonUtil.getObjectToString(sentFlowMessageModel)
                                ).setHeader("type", "sentMessage").build();
//                        );
                    }
                }
            }
        }
    }

    
    @PostMapping("/sendDelegateMsg")
    public void sendDelegateMsg(@RequestBody SentMessageForm sentMessageForm) {
        messageService.sentScheduleMessage(sentMessageForm,"");
    }


    //=====================================测试企业微信、钉钉的连接=====================================

    /**
     * 测试企业微信配置的连接功能
     *
     * @param qyWebChatModel 企业微信配置模型
     * @return
     */
    @Operation(summary = "测试企业微信配置的连接")
    @Parameters({
            @Parameter(name = "qyWebChatModel", description = "企业微信配置模型", required = true)
    })
    @PostMapping("/testQyWebChatConnect")
    public ActionResult testQyWebChatConnect(@RequestBody @Valid QyWebChatModel qyWebChatModel) {
        // 测试发送消息、组织同步的连接
        String corpId = qyWebChatModel.getQyhCorpId();
        String agentSecret = qyWebChatModel.getQyhAgentSecret();
        String corpSecret = qyWebChatModel.getQyhCorpSecret();
        // 测试发送消息的连接
        JSONObject retMsg = QyWebChatUtil.getAccessToken(corpId, agentSecret);
        if (HttpUtil.isWxError(retMsg)) {
            return ActionResult.fail("测试发送消息的连接失败：" + retMsg.getString("errmsg"));
        }

        // 测试发送组织同步的连接
        retMsg = QyWebChatUtil.getAccessToken(corpId, corpSecret);
        if (HttpUtil.isWxError(retMsg)) {
            return ActionResult.fail("测试组织同步的连接失败：" + retMsg.getString("errmsg"));
        }

        return ActionResult.success("测试连接成功");
    }


    /**
     * 测试钉钉配置的连接功能
     *
     * @param dingTalkModel 钉钉配置模型
     * @return
     */
    @Operation(summary = "测试钉钉配置的连接")
    @Parameters({
            @Parameter(name = "dingTalkModel", description = "钉钉配置模型", required = true)
    })
    @PostMapping("/testDingTalkConnect")
    public ActionResult testDingTalkConnect(@RequestBody @Valid DingTalkModel dingTalkModel) {
        // 测试钉钉配置的连接
        String appKey = dingTalkModel.getDingSynAppKey();
        String appSecret = dingTalkModel.getDingSynAppSecret();
        String agentId = dingTalkModel.getDingAgentId();
        // 测试钉钉的连接
        JSONObject retMsg = DingTalkUtil.getAccessToken(appKey, appSecret);
        if (!retMsg.getBoolean("code")) {
            return ActionResult.fail("测试连接失败：" + retMsg.getString("error"));
        }

        return ActionResult.success("测试连接成功");
    }

    /**
     * 发送消息
     *
     * @param toUserIds 发送用户
     * @param title     标题
     * @param bodyText  内容
     */
    
    @PostMapping("/sentMessage")
    public void sentMessage(@RequestParam("toUserIds")  List<String> toUserIds, @RequestParam("title") String title , @RequestParam("bodyText") String bodyText, @RequestParam("source") Integer source, @RequestParam("type") Integer type , @RequestBody UserInfo userInfo){
        messageService.sentMessage(toUserIds, title, bodyText, userInfo, source, type, false);
    }

    /**
     * 退出在线的WebSocket
     * @param token token
     * @param userId 用户id
     */
    @Operation(summary = "退出在线的WebSocket")
    @Parameters({
            @Parameter(name = "token", description = "token"),
            @Parameter(name = "userId", description = "用户id")
    })
    @NoDataSourceBind
    @GetMapping("/logoutWS")
    public void logoutWebsocketByToken(@RequestParam(name = "token", required = false) String token, @RequestParam(name = "userId", required = false) String userId) {
        if (StringUtil.isNotEmpty(token)) {
            OnlineUserProvider.removeWebSocketByToken(token.split(","));
        } else {
            OnlineUserProvider.removeWebSocketByUser(userId);
        }
    }

    
    @PostMapping("/sendScheduleMessage/{type}")
    public void sendScheduleMessage(@RequestBody SentMessageForm sentMessageForm,@PathVariable("type") String type) {
        messageService.sentScheduleMessage(sentMessageForm,type);
    }

}
