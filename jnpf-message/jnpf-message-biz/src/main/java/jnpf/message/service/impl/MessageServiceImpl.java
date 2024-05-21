package jnpf.message.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.base.entity.MessageTemplateEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.util.SentMessageUtil;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageMonitorEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.message.entity.MessageTemplateConfigEntity;
import jnpf.message.entity.SendConfigTemplateEntity;
import jnpf.message.entity.SendMessageConfigEntity;
import jnpf.message.mapper.MessageMapper;
import jnpf.message.model.NoticePagination;
import jnpf.message.model.NoticeVO;
import jnpf.message.model.SentMessageForm;
import jnpf.message.service.MessageMonitorService;
import jnpf.message.service.MessageService;
import jnpf.message.service.MessageTemplateConfigService;
import jnpf.message.service.MessagereceiveService;
import jnpf.message.service.SendConfigTemplateService;
import jnpf.message.service.SendMessageConfigService;
import jnpf.message.util.OnlineUserModel;
import jnpf.message.util.OnlineUserProvider;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.PageUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import jnpf.util.context.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 消息实例
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
@Slf4j
public class MessageServiceImpl extends SuperServiceImpl<MessageMapper, MessageEntity> implements MessageService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private MessageMonitorService messageMonitorService;
    @Autowired
    private MessagereceiveService messagereceiveService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SentMessageUtil sentMessageUtil;
    @Autowired
    private SendMessageConfigService sendMessageConfigService;
    @Autowired
    private SendConfigTemplateService sendConfigTemplateService;
    @Autowired
    private MessageTemplateConfigService messageTemplateConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    @Override
    public List<MessageEntity> getNoticeList(NoticePagination pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        //关键词（消息标题）
        if (!StringUtils.isEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().like(MessageEntity::getTitle, pagination.getKeyword());
        }
        // 类型
        if (pagination.getType() != null && pagination.getType().size() > 0) {
            queryWrapper.lambda().in(MessageEntity::getCategory, pagination.getType());
        }
        // 状态
        if (pagination.getEnabledMark() != null && pagination.getEnabledMark().size() > 0) {
            queryWrapper.lambda().in(MessageEntity::getEnabledMark, pagination.getEnabledMark());
        } else {
            queryWrapper.lambda().and(t->t.ne(MessageEntity::getEnabledMark, 3).or().isNull(MessageEntity::getEnabledMark));
        }
        //默认排序
        queryWrapper.lambda().orderByAsc(MessageEntity::getEnabledMark).orderByDesc(MessageEntity::getLastModifyTime).orderByDesc(MessageEntity::getCreatorTime);
        queryWrapper.lambda().select(MessageEntity::getId, MessageEntity::getCreatorUserId, MessageEntity::getEnabledMark,
                MessageEntity::getLastModifyTime, MessageEntity::getTitle, MessageEntity::getCreatorTime,
                MessageEntity::getLastModifyUserId, MessageEntity::getExpirationTime, MessageEntity::getCategory);
        Page<MessageEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<MessageEntity> userIPage = this.page(page, queryWrapper);
        return pagination.setData(userIPage.getRecords(), page.getTotal());
    }

    @Override
    public List<MessageEntity> getNoticeList() {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageEntity::getEnabledMark, 1);
        queryWrapper.lambda().orderByAsc(MessageEntity::getSortCode);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<MessageEntity> getDashboardNoticeList(List<String> typeList) {
        List<MessageEntity> list = new ArrayList<>(16);
        // 判断哪些消息是自己接收的
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userProvider.get().getUserId());
        queryWrapper.lambda().eq(MessageReceiveEntity::getType, 1);
        List<MessageReceiveEntity> receiveEntities = messagereceiveService.list(queryWrapper);
        for (int i = 0; i < receiveEntities.size(); i++) {
            // 得到message
            MessageReceiveEntity messageReceiveEntity = receiveEntities.get(i);
            MessageEntity entity = JsonUtil.getJsonToBean(messageReceiveEntity.getBodyText(), MessageEntity.class);
            if (entity != null) {
                if (StringUtil.isNotEmpty(entity.getId())) {
                    MessageEntity messageEntity = this.getInfo(entity.getId());
                    if (messageEntity != null) {
                        if ((typeList != null && typeList.size() > 0 && typeList.contains(messageEntity.getCategory()) || typeList == null || typeList.size() == 0)) {
                            if (Objects.equals(messageEntity.getEnabledMark(), 1)
                                    && (entity.getExpirationTime() == null || entity.getExpirationTime().getTime() > System.currentTimeMillis())) {
                                messageEntity.setId(messageReceiveEntity.getId());
                                list.add(messageEntity);
                            }
                        }
                    }
                }
            }
            if (list.size() > 49) {
                break;
            }
        }
        list = list.stream().sorted(Comparator.comparing(MessageEntity::getLastModifyTime).reversed()).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<MessageReceiveEntity> getMessageList1(Pagination pagination, Integer type,Integer isRead) {
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, UserProvider.getLoginUserId());
        if (type != null) {
            queryWrapper.lambda().eq(MessageReceiveEntity::getType, type);
        }
        if (isRead != null) {
            queryWrapper.lambda().eq(MessageReceiveEntity::getIsRead, isRead);
        }
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(t -> {
                t.like(MessageReceiveEntity::getTitle, pagination.getKeyword());
            });
        }
        queryWrapper.lambda().orderByDesc(MessageReceiveEntity::getCreatorTime);
        Page<MessageReceiveEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<MessageReceiveEntity> userIPage = messagereceiveService.page(page, queryWrapper);
        return pagination.setData(userIPage.getRecords(), page.getTotal());
    }

    @Override
    public List<MessageReceiveEntity> getMessageList3(Pagination pagination, Integer type, String user,Integer isRead) {
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, UserProvider.getLoginUserId());
        if (type != null) {
            queryWrapper.lambda().eq(MessageReceiveEntity::getType, type);
        }
        if (isRead != null) {
            queryWrapper.lambda().eq(MessageReceiveEntity::getIsRead, isRead);
        }
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(t -> {
                t.like(MessageReceiveEntity::getTitle, pagination.getKeyword());
            });
        }
        queryWrapper.lambda().orderByDesc(MessageReceiveEntity::getCreatorTime);
        Page<MessageReceiveEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<MessageReceiveEntity> userIPage = messagereceiveService.page(page, queryWrapper);
        return pagination.setData(userIPage.getRecords(), page.getTotal());
    }

    @Override
    public List<MessageReceiveEntity> getMessageList(Pagination pagination, Integer type, String user) {
        String userId = StringUtil.isEmpty(user) ? userProvider.get().getUserId() : user;
        Map<String, Object> map = new HashMap<>(16);
        map.put("userId", userId);
        //关键词（消息标题）
        String keyword = pagination.getKeyword();
        if (!StringUtil.isEmpty(keyword)) {
            map.put("keyword", "%" + keyword + "%");
        }
        //消息类别
        if (!ObjectUtils.isEmpty(type)) {
            map.put("type", type);
        }
        List<MessageReceiveEntity> lists = this.baseMapper.getMessageList(map);
        return pagination.setData(PageUtil.getListPage((int) pagination.getCurrentPage(), (int) pagination.getPageSize(), lists), lists.size());
    }

    @Override
    public List<MessageReceiveEntity> getMessageList(Pagination pagination) {
        return this.getMessageList(pagination, null,null);
    }

    @Override
    public MessageEntity getInfo(String id) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public MessageEntity getInfoDefault(int type) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageEntity::getEnabledMark, 1);
        if (type == 1) {
            queryWrapper.lambda().orderByDesc(MessageEntity::getCreatorTime);
        } else {
            queryWrapper.lambda().orderByDesc(MessageEntity::getLastModifyTime);
        }
        // 只查询id
        queryWrapper.lambda().select(MessageEntity::getId, MessageEntity::getTitle, MessageEntity::getCreatorTime);
        List<MessageEntity> list = this.page(new Page<>(1, 1, false), queryWrapper).getRecords();
        MessageEntity entity = new MessageEntity();
        if (list.size() > 0) {
            entity = list.get(0);
        }
        return entity;
    }

    @Override
    @DSTransactional
    public void delete(MessageEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public void create(MessageEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setBodyText(XSSEscape.escapeImgOnlyBase64(entity.getBodyText()));
        entity.setEnabledMark(0);
        entity.setCreatorUserId(userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, MessageEntity entity) {
        entity.setId(id);
        entity.setBodyText(XSSEscape.escapeImgOnlyBase64(entity.getBodyText()));
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setLastModifyUserId(null);
        entity.setLastModifyTime(null);
        return this.updateById(entity);
    }

    @Override
    public MessageReceiveEntity messageRead(String messageId) {
        String userId = userProvider.get().getUserId();
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userId).eq(MessageReceiveEntity::getId, messageId);
        MessageReceiveEntity entity = messagereceiveService.getOne(queryWrapper);
        if (entity != null) {
            entity.setIsRead(1);
            messagereceiveService.updateById(entity);
        }
        return entity;
    }

    @Override
    @DSTransactional
    public void messageRead(List<String> idList) {
        String userId = userProvider.get().getUserId();
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userId).eq(MessageReceiveEntity::getIsRead, 0);
//        queryWrapper.lambda().in(MessageReceiveEntity::getMessageId,idList);
        List<MessageReceiveEntity> entitys = messagereceiveService.list(queryWrapper);
        if (entitys.size() > 0) {
            for (MessageReceiveEntity entity : entitys) {
                entity.setIsRead(1);
                messagereceiveService.updateById(entity);
            }
        }
    }

    @Override
    @DSTransactional
    public void deleteRecord(List<String> messageIds) {
        // 删除已读表
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        if (messageIds.size() > 0) {
            queryWrapper.lambda().in(MessageReceiveEntity::getId, messageIds);
        }
        // 通过id删除无需判断接收人
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, UserProvider.getLoginUserId());
        messagereceiveService.remove(queryWrapper);
    }

    @Override
    public int getUnreadCount(String userId, Integer type) {
        int result = this.baseMapper.getUnreadCount(userId,type);
        return result;
    }

    @Override
    public boolean sentNotice(List<String> toUserIds, MessageEntity entity) {
        // 存到redis中的key对象
        UserInfo userInfo = userProvider.get();
        List<String> idList = new ArrayList<>();
        // 修改发送状态
        entity.setEnabledMark(1);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userInfo.getUserId());
        this.updateById(entity);
        // 存到redis，生成Redis的key
        Callable executeInsert = () -> {
            executeInsert(toUserIds, idList);
            return true;
        };
        ThreadPoolTaskExecutor threadPoolExecutor = SpringContext.getBean(ThreadPoolTaskExecutor.class);
        Future<Boolean> submit = threadPoolExecutor.submit(executeInsert);
        try {
            if (submit.get()) {
                // 执行发送公告操作
                Runnable runnable = () -> executeBatch(idList, entity, userInfo);
                threadPoolExecutor.submit(runnable);
            }
            return true;
        } catch (Exception e) {
            // 还原公告状态
            entity.setEnabledMark(0);
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            this.updateById(entity);
        }
        return false;
    }

    /**
     * 数据存到redis中
     *
     * @param toUserIds 接受者id
     */
    private void executeInsert(List<String> toUserIds, List<String> idList) throws Exception {
        List<String> key = new ArrayList<>();
        try {
            int frequency = 10000;
            int count = toUserIds.size() / frequency + 1;
            if (toUserIds.size() < 1) return;
            for (int i = 0; i < count; i++) {
                // 生成redis的key
                String cacheKey = RandomUtil.uuId() + toUserIds.get(i);
                // 存到redis
                int endSize = Math.min(((i + 1) * frequency), toUserIds.size());
                redisUtil.insert(cacheKey, toUserIds.subList(i * frequency, endSize));
                key.add(cacheKey);
            }
        } catch (Exception e) {
            key.forEach(k->redisUtil.remove(k));
            key.clear();
            throw new Exception();
        }
        idList.addAll(key);
    }

    /**
     * 执行发送操作
     *
     * @param idList   存到redis中的key
     * @param entity
     * @param userInfo
     */
    private void executeBatch(List<String> idList, MessageEntity entity, UserInfo userInfo) {
        if (idList.size() == 0 || "3".equals(String.valueOf(entity.getRemindCategory()))) {
            return;
        }
        SentMessageForm sentMessageForm = new SentMessageForm();
        List<String> toUserId = new ArrayList<>();
        for (String cacheKey : idList) {
            List<String> cacheValue = (List)redisUtil.get(cacheKey, 0, -1);
            toUserId.addAll(cacheValue);
        }
        sentMessageForm.setToUserIds(toUserId);
        sentMessageForm.setTitle(entity.getTitle());
        sentMessageForm.setContent(entity.getBodyText());
        sentMessageForm.setContentMsg(Collections.EMPTY_MAP);
        sentMessageForm.setUserInfo(userInfo);
        sentMessageForm.setType(1);
        sentMessageForm.setId(entity.getId());

        // 站内信
        if ("1".equals(String.valueOf(entity.getRemindCategory()))) {
            message(sentMessageForm);
        } else if ("2".equals(String.valueOf(entity.getRemindCategory()))) {
            SendMessageConfigEntity sendMessageConfigEntity = sendMessageConfigService.getInfo(entity.getSendConfigId());
            if (sendMessageConfigEntity != null) {
                List<SendConfigTemplateEntity> configTemplateEntityList = sendConfigTemplateService.getDetailListByParentId(sendMessageConfigEntity.getId());
                for (SendConfigTemplateEntity sendConfigTemplateEntity : configTemplateEntityList) {
                    Map<String, String> map = new HashMap<>();
                    map.put("Title", entity.getTitle());
                    map.put("Content", entity.getBodyText());
                    map.put("Remark", entity.getExcerpt());
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("@title", entity.getTitle());
                    paramMap.put("@Content", entity.getBodyText());
                    paramMap.put("@Remark", entity.getExcerpt());
                    switch (sendConfigTemplateEntity.getMessageType()) {
                        case "1" :
                            MessageTemplateConfigEntity configEntity = messageTemplateConfigService.getInfo(sendConfigTemplateEntity.getTemplateId());
                            if (configEntity != null) {
                                sentMessageForm.setTitle(configEntity.getTitle());
                            }
                            message(sentMessageForm);
                            break;
                        case "2":
                            // 邮件
                            sentMessageUtil.SendMail(toUserId, userInfo, "2", sendConfigTemplateEntity, new HashMap<>(), map);
                            break;
                        case "3":
                            // 发送短信
                            sentMessageUtil.sendSms(toUserId, userInfo, sendConfigTemplateEntity, paramMap, new HashMap<>());
                            break;
                        case "4":
                            // 钉钉
                            JSONObject jsonObject1 = sentMessageUtil.SendDingTalk(toUserId, userInfo, "4", sendConfigTemplateEntity, new HashMap<>(), map);
                            if (!(Boolean) jsonObject1.get("code")) {
                                log.error("发送企业微信消息失败，错误：" + jsonObject1.get("error"));
                            }
                            break;
                        case "5":
                            // 企业微信
                            JSONObject jsonObject = sentMessageUtil.SendQyWebChat(toUserId, userInfo, "5", sendConfigTemplateEntity, new HashMap<>(), map);
                            if (!(Boolean) jsonObject.get("code")) {
                                log.error("发送企业微信消息失败，错误：" + jsonObject.get("error"));
                            }
                            break;
                        case "6":
                            // webhook
                            sentMessageUtil.SendWebHook(null, userInfo, sendConfigTemplateEntity, new HashMap<>(), map);
                            break;
                        case "7":
                            // 微信公众号
                            sentMessageUtil.SendWXGzhChat(toUserId, userInfo, "7", sendConfigTemplateEntity, new HashMap<>(), paramMap);
                            break;
                        default:
                            break;
                    }
                }

            }
        }
    }


    @Override
    public void sentMessage(List<String> toUserIds, String title) {
        this.sentMessage(toUserIds, title, null);
    }

    @Override
    @DSTransactional
    public void sentMessage(List<String> toUserIds, String title, String bodyText) {
        UserInfo userInfo = userProvider.get();

        MessageReceiveEntity messageReceiveEntity = new MessageReceiveEntity();
        messageReceiveEntity.setTitle(title);
        messageReceiveEntity.setType(2);
        messageReceiveEntity.setFlowType(1);
        messageReceiveEntity.setIsRead(0);
        List<MessageReceiveEntity> receiveEntityList = new ArrayList<>();
        for (String item : toUserIds) {
            MessageReceiveEntity messageReceiveEntitys = new MessageReceiveEntity();
            BeanUtils.copyProperties(messageReceiveEntity, messageReceiveEntitys);
            messageReceiveEntitys.setId(RandomUtil.uuId());
            messageReceiveEntitys.setUserId(item);
            messageReceiveEntitys.setBodyText(bodyText);
            receiveEntityList.add(messageReceiveEntitys);
        }
        for (MessageReceiveEntity messageReceiveEntitys : receiveEntityList) {
            messagereceiveService.save(messageReceiveEntitys);
        }
        //消息推送 - PC端
        for (int i = 0; i < toUserIds.size(); i++) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (toUserIds.get(i).equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "messagePush");
                    map.put("unreadNoticeCount", 1);
                    map.put("messageType", 2);
                    map.put("userId", userInfo.getTenantId());
                    map.put("toUserId", toUserIds);
                    map.put("title", messageReceiveEntity.getTitle());
                    map.put("messageDefaultTime", messageReceiveEntity.getLastModifyTime() != null ? messageReceiveEntity.getLastModifyTime().getTime() : null);
                    OnlineUserProvider.sendMessage(item, map);
                }
            }
        }
    }

    @Override
    @DSTransactional
    public void sentMessage(List<String> toUserIds, String title, String bodyText, Map<String, String> contentMsg, UserInfo userInfo) {
        MessageReceiveEntity messageReceiveEntity = new MessageReceiveEntity();
        messageReceiveEntity.setTitle(title);
        messageReceiveEntity.setType(2);
        messageReceiveEntity.setFlowType(1);
        messageReceiveEntity.setIsRead(0);
        List<MessageReceiveEntity> receiveEntityList = new ArrayList<>();
        for (String item : toUserIds) {
            MessageReceiveEntity messageReceiveEntitys = new MessageReceiveEntity();
            BeanUtils.copyProperties(messageReceiveEntity, messageReceiveEntitys);
            messageReceiveEntitys.setId(RandomUtil.uuId());
            messageReceiveEntitys.setUserId(item);
            String msg = contentMsg.get(item) != null ? contentMsg.get(item) : "{}";
            messageReceiveEntitys.setBodyText(msg);
            receiveEntityList.add(messageReceiveEntitys);
        }
        for (MessageReceiveEntity messageReceiveEntitys : receiveEntityList) {
            messagereceiveService.save(messageReceiveEntitys);
            /*List<String> cidList = userDeviceService.getCidList(messageReceiveEntity.getUserId());
            //个推
            if (cidList != null && cidList.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "2");
                jsonObject.put("id", entity.getId());
                jsonObject.put("title",title);
                String text = JSONObject.toJSONString(jsonObject);
                byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                text = Base64.getEncoder().encodeToString(bytes);
                uinPush.sendUniPush(cidList, title, "你有一条流程消息", "2", text);
            }*/
        }

        //消息推送 - PC端
        for (int i = 0; i < toUserIds.size(); i++) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (toUserIds.get(i).equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "messagePush");
                    map.put("unreadNoticeCount", 1);
                    map.put("messageType", 2);
                    map.put("userId", userInfo.getTenantId());
                    map.put("toUserId", toUserIds);
                    map.put("title", messageReceiveEntity.getTitle());
                    map.put("id",messageReceiveEntity.getId());
                    map.put("messageDefaultTime", messageReceiveEntity.getLastModifyTime() != null ? messageReceiveEntity.getLastModifyTime().getTime() : null);
                    OnlineUserProvider.sendMessage(item, map);
                }
            }
        }
    }

    @Override
    @DSTransactional
    public void sentMessage(List<String> toUserIds, String title, String bodyText, UserInfo userInfo, Integer source, Integer type) {
        sentMessage(toUserIds, title, bodyText, userInfo, source, type, false);
    }
    @Override
    @DSTransactional
    public void sentMessage(List<String> toUserIds, String title, String bodyText, Integer source, Integer type, UserInfo userInfo){
        sentMessage(toUserIds, title, bodyText, userInfo, source, type, false);
    }

    @Override
    @DSTransactional
    public void sentMessage(List<String> toUserIds, String title, String bodyText, UserInfo userInfo, Integer source, Integer type, boolean testMessage) {
        MessageReceiveEntity messageReceiveEntity = new MessageReceiveEntity();
        messageReceiveEntity.setTitle(title);
        messageReceiveEntity.setType(source);
        messageReceiveEntity.setFlowType(1);
        messageReceiveEntity.setIsRead(0);
        List<MessageReceiveEntity> receiveEntityList = new ArrayList<>();
        for (String item : toUserIds) {
            MessageReceiveEntity messageReceiveEntitys = new MessageReceiveEntity();
            BeanUtils.copyProperties(messageReceiveEntity, messageReceiveEntitys);
            messageReceiveEntitys.setId(RandomUtil.uuId());
            messageReceiveEntitys.setUserId(item);
            receiveEntityList.add(messageReceiveEntitys);
        }
//        if (testMessage && source == 1) {
//            entity.setEnabledMark(3);
//        }
        for (MessageReceiveEntity messageReceiveEntitys : receiveEntityList) {
            messagereceiveService.save(messageReceiveEntitys);
        }
        //消息监控写入
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setTitle(title);
        monitorEntity.setMessageType(String.valueOf(type));
        monitorEntity.setMessageSource(String.valueOf(source));
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIds));
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        messageMonitorService.create(monitorEntity);
        for (int i = 0; i < toUserIds.size(); i++) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (toUserIds.get(i).equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "messagePush");
                    map.put("unreadNoticeCount", 1);
                    map.put("messageType", messageReceiveEntity.getType());
                    map.put("userId", userInfo.getTenantId());
                    map.put("toUserId", toUserIds);
                    map.put("title", messageReceiveEntity.getTitle());
                    map.put("id",messageReceiveEntity.getId());
                    map.put("messageDefaultTime", messageReceiveEntity.getLastModifyTime() != null ? messageReceiveEntity.getLastModifyTime().getTime() : null);
                    OnlineUserProvider.sendMessage(item, map);
                }
            }
        }
    }

    @Override
    public void sentFlowMessage(List<String> toUserIds, MessageTemplateEntity entity, String content) {
        if (entity != null) {
            // 消息标题
            String title = entity.getTitle();
            this.sentMessage(toUserIds, title, content);
        }
    }

    @Override
    public void logoutWebsocketByToken(String token, String userId){
        if(StringUtil.isNotEmpty(token)) {
            OnlineUserProvider.removeWebSocketByToken(token.split(","));
        }else{
            OnlineUserProvider.removeWebSocketByUser(userId);
        }
    }

    @Override
    public void sentScheduleMessage(SentMessageForm sentMessageForm, String type) {
        UserInfo userInfo = sentMessageForm.getUserInfo();
        String templateId = sentMessageForm.getTemplateId();
        String title = sentMessageForm.getTitle();
        List<String> toUserIds = sentMessageForm.getToUserIds();
        //获取发送配置详情
        SendMessageConfigEntity configEntity = sendMessageConfigService.getInfoByEnCode(templateId);
        if (configEntity != null) {
            templateId = configEntity.getId();
        } else {
            configEntity = sendMessageConfigService.getInfo(templateId);
        }
        List<SendConfigTemplateEntity> list = sendConfigTemplateService.getDetailListByParentId(templateId);
        if (configEntity != null) {
            for (SendConfigTemplateEntity sendConfigTemplateEntity : list) {
                Map<String, Object> objectMap = new HashMap<>(sentMessageForm.getParameterMap());
                Map<String,Object> parameterMap = new HashMap<>();
                for(String key:objectMap.keySet()){
                    if(key.contains(sendConfigTemplateEntity.getId())){
                        parameterMap.put(key.substring(sendConfigTemplateEntity.getId().length()),objectMap.get(key));
                    }
                }
                parameterMap.putAll(objectMap);
                Map<String, String> contentMsg = new HashMap<>();
                for(String key : parameterMap.keySet()){
                    contentMsg.put(key,String.valueOf(parameterMap.get(key)));
                }
                String sendType = sendConfigTemplateEntity.getMessageType();
                switch (sendType) {
                    case "1":
                        MessageTemplateConfigEntity templateConfigEntity = messageTemplateConfigService.getInfo(sendConfigTemplateEntity.getTemplateId());
                        String messageTitle = StringUtil.isNotEmpty(templateConfigEntity.getTitle()) ? templateConfigEntity.getTitle() : "";
                        String content = StringUtil.isNotEmpty(templateConfigEntity.getContent()) ? templateConfigEntity.getContent() : "";
                        StringSubstitutor strSubstitutor = new StringSubstitutor(parameterMap, "{", "}");
                        messageTitle = strSubstitutor.replace(messageTitle);
                        content = strSubstitutor.replace(content);
                        sentMessageForm.setTitle(messageTitle);
                        sentMessageForm.setContent(content);
                        // 站内消息
                        message(sentMessageForm);
                        break;
                    case "2":
                        // 邮件
                        sentMessageUtil.SendMail(toUserIds, userInfo, sendType, sendConfigTemplateEntity, new HashMap<>(), contentMsg);
                        break;
                    case "3":
                        // 发送短信
                        sentMessageUtil.sendSms(toUserIds, userInfo, sendConfigTemplateEntity, parameterMap, new HashMap<>());
                        break;
                    case "4":
                        // 钉钉
                        JSONObject jsonObject1 = sentMessageUtil.SendDingTalk(toUserIds, userInfo, sendType, sendConfigTemplateEntity, new HashMap<>(), contentMsg);
                        if (!(Boolean) jsonObject1.get("code")) {
                            log.error("发送企业微信消息失败，错误：" + jsonObject1.get("error"));
                        }
                        break;
                    case "5":
                        // 企业微信
                        JSONObject jsonObject = sentMessageUtil.SendQyWebChat(toUserIds, userInfo, sendType, sendConfigTemplateEntity, new HashMap<>(), contentMsg);
                        if (!(Boolean) jsonObject.get("code")) {
                            log.error("发送企业微信消息失败，错误：" + jsonObject.get("error"));
                        }
                        break;
                    case "6":
                        // webhook
                        sentMessageUtil.SendWebHook(sendType, userInfo, sendConfigTemplateEntity, new HashMap<>(),contentMsg);
                        break;
                    case "7":
                        // 微信公众号
                        sentMessageUtil.SendWXGzhChat(toUserIds, userInfo, sendType, sendConfigTemplateEntity, new HashMap<>(),parameterMap);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public Boolean updateEnabledMark() {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(MessageEntity::getEnabledMark, 1).lt(MessageEntity::getExpirationTime, new Date()));
        List<MessageEntity> list = this.list(queryWrapper);
        list.forEach(t -> {
            t.setEnabledMark(2);
            this.updateById(t);
        });
        return true;
    }

    /**
     * 工作流消息发送
     *
     * @param toUserIds
     * @param entity
     * @param userInfo
     */
    private static void pushMessage(List<String> toUserIds, MessageReceiveEntity entity, UserInfo userInfo,int messageType){
        for (int i = 0; i < toUserIds.size(); i++) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (toUserIds.get(i).equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "messagePush");
                    map.put("unreadNoticeCount", 1);
                    map.put("messageType", messageType);
                    map.put("userId", userInfo.getTenantId());
                    map.put("toUserId", toUserIds);
                    map.put("title", entity.getTitle());
                    map.put("id",entity.getId());
                    map.put("messageDefaultTime", entity.getLastModifyTime() != null ? entity.getLastModifyTime().getTime() : null);
                    OnlineUserProvider.sendMessage(item, map);
                }
            }
        }
    }

    private void message(SentMessageForm sentMessageForm) {
        List<String> toUserIds = sentMessageForm.getToUserIds();
        Integer type = sentMessageForm.getType();
        String title = sentMessageForm.getTitle();
        String content = sentMessageForm.getContent();
        String bodyText = Objects.equals(type,3) ? content: JsonUtil.getObjectToString(sentMessageForm.getContentMsg());
        UserInfo userInfo = sentMessageForm.getUserInfo();
        MessageReceiveEntity messageReceiveEntity = new MessageReceiveEntity();
        messageReceiveEntity.setIsRead(0);
        messageReceiveEntity.setId(RandomUtil.uuId());
        messageReceiveEntity.setType(sentMessageForm.getType());
        if (type != null) {
            messageReceiveEntity.setId(sentMessageForm.getId());
            messageReceiveEntity.setType(type);
        }
        //消息监控写入
        MessageMonitorEntity monitorEntity = new MessageMonitorEntity();
        MessageEntity messageEntity = this.getInfo(sentMessageForm.getId());
        if (!"1".equals(String.valueOf(messageReceiveEntity.getType()))) {
            monitorEntity.setMessageSource(sentMessageForm.getType()+"");
            messageReceiveEntity.setFlowType(sentMessageForm.getFlowType());
            monitorEntity.setTitle(title);
        } else {
            monitorEntity.setMessageSource("1");
            title = title.replaceAll("\\{@Title}", messageEntity.getTitle())
                    .replaceAll("\\{@CreatorUserName}", userInfo.getUserName())
                    .replaceAll("\\{@Content}", messageEntity.getBodyText())
                    .replaceAll("\\{@Remark}", StringUtil.isNotEmpty(messageEntity.getExcerpt()) ? messageEntity.getExcerpt() : "");
            monitorEntity.setTitle(title);
            bodyText = JsonUtil.getObjectToString(messageEntity);
        }
        List<MessageReceiveEntity> receiveEntityList = new ArrayList<>();
        for (String item : toUserIds) {
            MessageReceiveEntity messageReceiveEntitys = new MessageReceiveEntity();
            BeanUtils.copyProperties(messageReceiveEntity, messageReceiveEntitys);
            messageReceiveEntitys.setId(RandomUtil.uuId());
            messageReceiveEntitys.setUserId(item);
            messageReceiveEntitys.setTitle(title);
//            if(ObjectUtil.isNotEmpty(messageEntity)) {
//                messageReceiveEntitys.setTitle(title.replaceAll("\\{@Title}", messageEntity.getTitle())
//                        .replaceAll("\\{@CreatorUserName}", userInfo.getUserName())
//                        .replaceAll("\\{@Content}", messageEntity.getBodyText())
//                        .replaceAll("\\{@Remark}", StringUtil.isNotEmpty(messageEntity.getExcerpt()) ? messageEntity.getExcerpt() : ""));
//            }
            messageReceiveEntitys.setBodyText(bodyText);
            receiveEntityList.add(messageReceiveEntitys);
        }
        for (MessageReceiveEntity messageReceiveEntitys : receiveEntityList) {
            messageReceiveEntitys.setTitle(title);
            messageReceiveEntitys.setCreatorUserId(userInfo.getUserId());
            messageReceiveEntitys.setCreatorTime(new Date());
            messagereceiveService.save(messageReceiveEntitys);
        }
        monitorEntity.setId(RandomUtil.uuId());
        monitorEntity.setMessageType("1");
        monitorEntity.setReceiveUser(JsonUtil.getObjectToString(toUserIds));
        monitorEntity.setSendTime(DateUtil.getNowDate());
        monitorEntity.setCreatorTime(DateUtil.getNowDate());
        monitorEntity.setCreatorUserId(userInfo.getUserId());
        monitorEntity.setContent(content);
        messageMonitorService.create(monitorEntity);
        //消息推送 - PC端
        pushMessage(toUserIds, messageReceiveEntity, userInfo, 4);
    }



    public List<NoticeVO> getNoticeList(List<String> list){
        List<MessageEntity> dashboardNoticeList = this.getDashboardNoticeList(list);
        List<UserEntity> userList = userService.getUserName(dashboardNoticeList.stream().map(MessageEntity::getCreatorUserId).collect(Collectors.toList()));
        DictionaryTypeEntity dictionaryTypeEntity = dictionaryTypeService.getInfoByEnCode("NoticeType");
        List<DictionaryDataEntity> noticeType = dictionaryDataService.getDicList(dictionaryTypeEntity.getId());
        dashboardNoticeList.forEach(t -> {
            // 转换创建人、发布人
            UserEntity user = userList.stream().filter(ul -> ul.getId().equals(t.getCreatorUserId())).findFirst().orElse(null);
            t.setCreatorUserId(user != null ? user.getRealName() + "/" + user.getAccount() : "");
            if (t.getEnabledMark() != null && t.getEnabledMark() != 0) {
                UserEntity entity = userService.getInfo(t.getLastModifyUserId());
                t.setLastModifyUserId(entity != null ? entity.getRealName() + "/" + entity.getAccount() : "");
            }
            DictionaryDataEntity dictionaryDataEntity = noticeType.stream().filter(notice -> notice.getEnCode().equals(t.getCategory())).findFirst().orElse(new DictionaryDataEntity());
            t.setCategory(dictionaryDataEntity.getFullName());
        });
        List<NoticeVO> jsonToList = new ArrayList<>();
        dashboardNoticeList.forEach(t->{
            NoticeVO vo = JsonUtil.getJsonToBean(t, NoticeVO.class);
            vo.setReleaseTime(t.getLastModifyTime() != null ? t.getLastModifyTime().getTime() : null);
            vo.setReleaseUser(t.getLastModifyUserId());
            vo.setCreatorUser(t.getCreatorUserId());
            jsonToList.add(vo);
        });
        return jsonToList;
    }
}
