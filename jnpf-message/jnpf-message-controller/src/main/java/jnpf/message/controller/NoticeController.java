package jnpf.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.SuperBaseEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.SystemService;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.JnpfConst;
import jnpf.consts.AuthConsts;
import jnpf.exception.DataException;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.message.model.MessageInfoVO;
import jnpf.message.model.MessageNoticeVO;
import jnpf.message.model.MessageRecordForm;
import jnpf.message.model.NoticeCrForm;
import jnpf.message.model.NoticeInfoVO;
import jnpf.message.model.NoticePagination;
import jnpf.message.model.NoticeUpForm;
import jnpf.message.model.NoticeVO;
import jnpf.message.model.PaginationMessage;
import jnpf.message.model.SentMessageModel;
import jnpf.message.service.MessageService;
import jnpf.message.util.OnlineUserModel;
import jnpf.message.util.OnlineUserProvider;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static jnpf.consts.AuthConsts.TOKEN_PREFIX;

/**
 * 系统公告
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "系统公告", description = "Message")
@RestController
@RequestMapping("/api/message")
@Slf4j
public class NoticeController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private SystemService systemService;

    /**
     * 列表（通知公告）
     *
     * @param pagination 分页模型
     * @return ignore
     */
    @Operation(summary = "获取系统公告列表（带分页）")
    @SaCheckPermission("system.notice")
    @PostMapping("/Notice/List")
    public ActionResult<PageListVO<MessageNoticeVO>> NoticeList(@RequestBody NoticePagination pagination) {
        messageService.updateEnabledMark();
        List<MessageEntity> list = messageService.getNoticeList(pagination);
        List<UserEntity> userList = userService.getUserName(list.stream().map(MessageEntity::getCreatorUserId).collect(Collectors.toList()));
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        List<DictionaryDataEntity> noticeType = dictionaryDataService.getListByTypeDataCode("NoticeType");
        List<MessageNoticeVO> voList = new ArrayList<>();
        // 判断是否过期
        list.forEach(t -> {
            MessageNoticeVO vo = JsonUtil.getJsonToBean(t, MessageNoticeVO.class);
            // 处理是否过期
            if (t.getExpirationTime() != null) {
                // 已发布的情况下
                if (t.getEnabledMark() == 1) {
                    if (t.getExpirationTime().getTime() < System.currentTimeMillis()) {
                        vo.setEnabledMark(2);
                    }
                }
            }
            DictionaryDataEntity dictionaryDataEntity = noticeType.stream().filter(notice -> notice.getEnCode().equals(t.getCategory())).findFirst().orElse(new DictionaryDataEntity());
            vo.setCategory(dictionaryDataEntity.getFullName());
            // 转换创建人、发布人
            UserEntity user = userList.stream().filter(ul -> ul.getId().equals(t.getCreatorUserId())).findFirst().orElse(null);
            vo.setCreatorUser(user != null ? user.getRealName() + "/" + user.getAccount() : "");
            if (t.getEnabledMark() != null && t.getEnabledMark() != 0) {
                UserEntity entity = userService.getInfo(t.getLastModifyUserId());
                vo.setLastModifyUserId(entity != null ? entity.getRealName() + "/" + entity.getAccount() : "");
                vo.setReleaseTime(t.getLastModifyTime() != null ? t.getLastModifyTime().getTime() : null);
                vo.setReleaseUser(vo.getLastModifyUserId());
            }
            voList.add(vo);
        });
        return ActionResult.page(voList, paginationVO);
    }

    /**
     * 添加系统公告
     *
     * @param noticeCrForm 新建系统公告模型
     * @return
     */
    @Operation(summary = "添加系统公告")
    @Parameters({
            @Parameter(name = "noticeCrForm", description = "新建系统公告模型", required = true)
    })
    @SaCheckPermission("system.notice")
    @PostMapping("/Notice")
    public ActionResult create(@RequestBody @Valid NoticeCrForm noticeCrForm) {
        MessageEntity entity = JsonUtil.getJsonToBean(noticeCrForm, MessageEntity.class);
        if(entity != null && StringUtil.isNotEmpty(entity.getBodyText()) && (entity.getBodyText().contains("&lt;") || entity.getBodyText().contains("&amp;lt;"))){
            return ActionResult.fail("内容不能包含<符号");
        }
        messageService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改系统公告
     *
     * @param id            主键
     * @param messageUpForm 修改系统公告模型
     * @return
     */
    @Operation(summary = "修改系统公告")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "messageUpForm", description = "修改系统公告模型", required = true)
    })
    @SaCheckPermission("system.notice")
    @PutMapping("/Notice/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid NoticeUpForm messageUpForm) {
        MessageEntity entity = JsonUtil.getJsonToBean(messageUpForm, MessageEntity.class);
        if(entity != null && StringUtil.isNotEmpty(entity.getBodyText()) && (entity.getBodyText().contains("&lt;") || entity.getBodyText().contains("&amp;lt;"))){
            return ActionResult.fail("内容不能包含<符号");
        }
        boolean flag = messageService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取/查看系统公告信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.notice")
    @GetMapping("/Notice/{id}")
    public ActionResult<NoticeInfoVO> Info(@PathVariable("id") String id) throws DataException {
        MessageEntity entity = messageService.getInfo(id);
        NoticeInfoVO vo = null;
        if (entity != null) {
            UserEntity info = userService.getInfo(entity.getCreatorUserId());
            entity.setCreatorUserId(info != null ? info.getRealName() + "/" + info.getAccount() : "");
            vo = JsonUtilEx.getJsonToBeanEx(entity, NoticeInfoVO.class);
            vo.setReleaseUser(entity.getCreatorUserId());
            vo.setReleaseTime(entity.getLastModifyTime() != null ? entity.getLastModifyTime().getTime() : null);
            UserEntity userEntity = userService.getInfo(entity.getLastModifyUserId());
            if (userEntity != null && StringUtil.isNotEmpty(userEntity.getId())) {
                String realName = userEntity.getRealName();
                String account = userEntity.getAccount();
                if (StringUtil.isNotEmpty(realName)) {
                    vo.setReleaseUser(realName + "/" + account);
                }
            }
        }
        return ActionResult.success(vo);
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除系统公告")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.notice")
    @DeleteMapping("/Notice/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        MessageEntity entity = messageService.getInfo(id);
        if (entity != null) {
            messageService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 发布公告
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "发布系统公告")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.notice")
    @PutMapping("/Notice/{id}/Actions/Release")
    public ActionResult Release(@PathVariable("id") String id) {
        MessageEntity entity = messageService.getInfo(id);
        if (entity != null) {
            List<String> userIds = null;
            if (StringUtil.isNotEmpty(entity.getToUserIds())) {
                userIds = Arrays.asList(entity.getToUserIds().split(","));
            } else {
                userIds = userService.getListId();
            }
            List<String> userIdList = userService.getUserIdList(userIds,null);
            if (messageService.sentNotice(userIdList, entity)) {
                /*if(userIds != null && userIds.size()>0) {
                    for (String userId : userIds) {
                        List<String> cidList = userDeviceService.getCidList(userId);
                        if(cidList != null && cidList.size()>0){
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","1");
                            jsonObject.put("id",entity.getId());
                            jsonObject.put("title",entity.getTitle());
                            String text = JSONObject.toJSONString(jsonObject);
                            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                            text = Base64.getEncoder().encodeToString(bytes);
                            uinPush.sendUniPush(cidList, entity.getTitle(), "你有一条公告消息", "1", text);
                        }
                    }
                }*/
                return ActionResult.success("发布成功");
            }
            return ActionResult.fail("发布失败");
        }
        return ActionResult.fail("发布失败");
    }

//=======================================站内消息、消息中心=================================================


    /**
     * 获取消息中心列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "列表（通知公告/系统消息/私信消息）")
    @GetMapping
    public ActionResult<PageListVO<MessageInfoVO>> messageList(PaginationMessage pagination) {
        List<MessageInfoVO> listVO = new ArrayList<>();
        List<MessageReceiveEntity> list = messageService.getMessageList1(pagination, pagination.getType(), pagination.getIsRead());
        List<UserEntity> userList = userService.getUserName(list.stream().map(SuperBaseEntity.SuperCBaseEntity::getCreatorUserId).collect(Collectors.toList()));
        list.forEach(t -> {
            MessageInfoVO vo = JsonUtil.getJsonToBean(t, MessageInfoVO.class);
            UserEntity user = userList.stream().filter(ul -> ul.getId().equals(t.getCreatorUserId())).findFirst().orElse(null);
            if (user != null) {
                vo.setReleaseTime(t.getCreatorTime() != null ? t.getCreatorTime().getTime() : null);
                UserEntity entity = userService.getInfo(t.getCreatorUserId());
                vo.setReleaseUser(entity != null ? entity.getRealName() + "/" + entity.getAccount() : "");
                vo.setCreatorUser(entity != null ? entity.getRealName() + "/" + entity.getAccount() : "");
            }
            listVO.add(vo);
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }


    /**
     * 读取消息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "读取消息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @GetMapping("/ReadInfo/{id}")
    public ActionResult<NoticeInfoVO> readInfo(@PathVariable("id") String id) throws DataException {
        MessageReceiveEntity receive = messageService.messageRead(id);
        if (receive != null) {
            UserEntity user = userService.getInfo(receive.getUserId());
            receive.setCreatorUserId(user != null ? user.getRealName() + "/" + user.getAccount() : "");
            receive.setBodyText(receive.getBodyText());
//            if (entity.getType() == 2) {
//                entity.setBodyText(receive.getBodyText());
//            }
        }
        NoticeInfoVO vo = JsonUtil.getJsonToBean(receive, NoticeInfoVO.class);
        if (Objects.equals(receive.getType() , 1)) {
            MessageEntity jsonToBean = JsonUtil.getJsonToBean(receive.getBodyText(), MessageEntity.class);
            if (jsonToBean != null) {
                vo.setCategory(jsonToBean.getCategory());
                vo.setCoverImage(jsonToBean.getCoverImage());
                vo.setExcerpt(jsonToBean.getExcerpt());
                vo.setExpirationTime(jsonToBean.getExpirationTime() != null ? jsonToBean.getExpirationTime().getTime() : null);
                vo.setFiles(jsonToBean.getFiles());
                vo.setBodyText(jsonToBean.getBodyText());
            }
        }
        vo.setReleaseTime(receive.getCreatorTime() != null ? receive.getCreatorTime().getTime() : null);
//        UserEntity info = userService.getInfo(receive.getCreatorUserId());
        vo.setReleaseUser(receive.getCreatorUserId());
        return ActionResult.success(vo);
    }


    /**
     * 全部已读
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "全部已读")
    @Parameters({
            @Parameter(name = "pagination", description = "分页模型", required = true)
    })
    @PostMapping("/Actions/ReadAll")
    public ActionResult allRead(@RequestBody PaginationMessage pagination) {
        List<MessageReceiveEntity> list = messageService.getMessageList3(pagination, pagination.getType(),null,pagination.getIsRead());
        if(list != null && list.size()>0) {
            List<String> idList = list.stream().map(SuperBaseEntity.SuperIBaseEntity::getId).collect(Collectors.toList());
            messageService.messageRead(idList);
            return ActionResult.success("操作成功");
        }else {
            return ActionResult.fail("暂无未读消息");
        }
    }

    /**
     * app端获取未读数据
     *
     * @return
     */
    @Operation(summary = "app端获取未读数据")
    @GetMapping("/getUnReadMsgNum")
    public ActionResult<Map<String, String>> getUnReadMsgNum() {
        Map<String, String> map = new HashMap<>();
        UserInfo userInfo = UserProvider.getUser();
        Integer unReadMsg = messageService.getUnreadCount(userInfo.getUserId(), 2);
        Integer unReadSchedule = messageService.getUnreadCount(userInfo.getUserId(),4);
        Integer unReadNotice = messageService.getUnreadCount(userInfo.getUserId(), 1);
        Integer unReadSystemMsg = messageService.getUnreadCount(userInfo.getUserId(), 3);
        Integer unReadNum = unReadMsg+unReadNotice+unReadSchedule+unReadSystemMsg;
        map.put("unReadMsg",unReadMsg.toString());
        map.put("unReadNotice",unReadNotice.toString());
        map.put("unReadSchedule",unReadSchedule.toString());
        map.put("unReadSystemMsg",unReadSystemMsg.toString());
        map.put("unReadNum",unReadNum.toString());
        return ActionResult.success(map);
    }

    /**
     * 删除记录
     *
     * @param recordForm 已读模型
     * @return
     */
    @Operation(summary = "删除消息")
    @Parameters({
            @Parameter(name = "recordForm", description = "已读模型", required = true)
    })
    @DeleteMapping("/Record")
    public ActionResult deleteRecord(@RequestBody MessageRecordForm recordForm) {
        String[] id = recordForm.getIds().split(",");
        List<String> list = Arrays.asList(id);
        messageService.deleteRecord(list);
        return ActionResult.success("删除成功");
    }

    /**
     * 列表（通知公告）
     *
     * @return
     */
    
    @PostMapping("/GetNoticeList")
    public List<NoticeVO> getNoticeList(@RequestBody List<String> list){
        List<MessageEntity> dashboardNoticeList = messageService.getDashboardNoticeList(list);
        List<UserEntity> userList = userService.getUserName(dashboardNoticeList.stream().map(MessageEntity::getCreatorUserId).collect(Collectors.toList()));
        List<DictionaryDataEntity> noticeType = dictionaryDataService.getListByTypeDataCode("NoticeType");
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

    /**
     * 发送消息
     * @param sentMessageModel
     * @return
     */
    
    @PostMapping("/SentMessage")
    public void sentMessage(@RequestBody SentMessageModel sentMessageModel){
        messageService.sentMessage(sentMessageModel.getToUserIds(),sentMessageModel.getTitle(),sentMessageModel.getBodyText());
    }

    
    @PostMapping("/autoSystem")
    public void autoSystem(@RequestBody Map<String, Object> map) {
        String message = map.get("message").toString();
        SystemEntity entity = JsonUtil.getJsonToBean(map.get("entity"), SystemEntity.class);
        // 如果禁用了系统
        List<OnlineUserModel> onlineUserList = OnlineUserProvider.getOnlineUserList();
        SystemEntity mainSystem = systemService.getInfoByEnCode(JnpfConst.MAIN_SYSTEM_CODE);
        // 所有在线用户信息
        Map<String, UserEntity> userEntityMap = userService.getUserName(onlineUserList.stream().map(OnlineUserModel::getUserId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        for (OnlineUserModel item : onlineUserList) {
            UserEntity userEntity = userEntityMap.get(item.getUserId());
            String systemId = userEntity.getSystemId();
            if (item.getIsMobileDevice()) {
                systemId = userEntity.getAppSystemId();
            }
            if (userEntity == null ||systemId.equals(mainSystem.getId())
                    || (!userProvider.get(item.getToken()).getSystemIds().contains(entity.getId()))
            ) {
                continue;
            }
            if (item.getWebSocket().isOpen()) {
                Map<String, String> maps = new HashMap<>(1);
                maps.put("msg", message);
                if (item.getIsMobileDevice()) {
                    maps.put("method", "logout");
                    maps.put("msg", message.contains("禁用") ? "应用已被禁用，正在退出！" : "应用已被删除，正在退出！");
                } else {
                    maps.put("method", "refresh");
                }
                if (StringUtil.isNotEmpty(userProvider.get().getTenantId())) {
                    if (userProvider.get().getTenantId().equals(item.getTenantId())) {
                        item.getWebSocket().getAsyncRemote().sendText(JsonUtil.getObjectToString(maps));
                    }
                } else {
                    item.getWebSocket().getAsyncRemote().sendText(JsonUtil.getObjectToString(maps));
                }
            }
        }

    }

    
    @PostMapping("/sendMessage")
    public void sendMessage(@RequestBody Map<String, Object> objectMap) {
        List<String> ids = (List<String>) objectMap.get("ids");
        String message = (String) objectMap.get("message");
        String[] token = OnlineUserProvider.getOnlineUserList().stream()
                .filter(t -> ids.contains(t.getUserId()))
                .map(OnlineUserModel::getToken).toArray(String[]::new);
        List<String> tokens = Arrays.stream(token).map(t -> t.contains(AuthConsts.TOKEN_PREFIX) ? t : TOKEN_PREFIX + " " + t).collect(Collectors.toList());
        //清除websocket登录状态
        List<OnlineUserModel> users = OnlineUserProvider.getOnlineUserList().stream().filter(t -> tokens.contains(t.getToken())).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(users)) {
            for (OnlineUserModel user : users) {
                JSONObject obj = new JSONObject();
                obj.put("method", "logout");
                obj.put("msg", StringUtil.isEmpty(message) ? "权限已变更，请重新登录！" : message);
                if(user != null) {
                    OnlineUserProvider.sendMessage(user, obj);
                }
                //先移除对象， 并推送下线信息， 避免网络原因导致就用户未断开 新用户连不上WebSocket
                OnlineUserProvider.removeModel(user);
                //通知所有在线，有用户离线
                for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList().stream().filter(t -> !Objects.equals(user.getUserId(), t.getUserId()) && !Objects.equals(user.getTenantId(),t.getTenantId())).collect(Collectors.toList())) {
                    if (!item.getUserId().equals(user.getUserId())) {
                        JSONObject objs = new JSONObject();
                        objs.put("method", "logout");
                        //推送给前端
                        OnlineUserProvider.sendMessage(item, objs);
                    }
                }
            }
        }
    }

}
