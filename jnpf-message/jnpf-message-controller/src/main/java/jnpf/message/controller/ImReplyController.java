package jnpf.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.ListVO;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.model.ImReplyListModel;
import jnpf.message.model.ImReplyListVo;
import jnpf.message.service.ImContentService;
import jnpf.message.service.ImReplyService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息会话接口
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Tag(name = "消息会话接口", description = "imreply")
@RestController
@RequestMapping("/api/message/imreply")
public class ImReplyController extends SuperController<ImReplyService, ImReplyEntity> {
    @Autowired
    private ImReplyService imReplyService;
    @Autowired
    private ImContentService imContentService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;

    /**
     * 获取消息会话列表
     *
     * @return ignore
     */
    @Operation(summary = "获取消息会话列表")
    @GetMapping
    public ActionResult<ListVO<ImReplyListVo>> getList() {
        List<ImReplyListModel> imReplyList = imReplyService.getImReplyList();
        //过滤 发送者删除标记
        imReplyList = imReplyList.stream().filter(t ->{
            if(t.getImreplySendDeleteMark() != null){
                return !t.getImreplySendDeleteMark().equals(userProvider.get().getUserId());
            }
            return true;
        }).collect(Collectors.toList());
        List<ImReplyListModel> imReplyLists = new ArrayList<>(imReplyList);
        for (ImReplyListModel vo : imReplyList) {
            UserEntity entity = userService.getInfo(vo.getId());
            if (entity == null || entity.getEnabledMark() == 0) {
                imReplyLists.remove(vo);
                continue;
            }
            //头像路径拼接
            vo.setHeadIcon(UploaderUtil.uploaderImg(vo.getHeadIcon()));
            //获取未读消息
            vo.setUnreadMessage(imContentService.getUnreadCount(vo.getId(), userProvider.get().getUserId()));
            //拼接账号和名称
            vo.setRealName(entity.getRealName());
            vo.setAccount(entity.getAccount());
            if(vo.getSendDeleteMark()!=null && vo.getSendDeleteMark().equals(userProvider.get().getUserId()) || vo.getDeleteMark()==1){
                vo.setLatestMessage("");
                vo.setMessageType("");
            }
        }
        //排序
        imReplyLists = imReplyLists.stream().sorted(Comparator.comparing(ImReplyListModel::getLatestDate).reversed())
                .collect(Collectors.toList());
        List<ImReplyListVo> imReplyListVoList = JsonUtil.getJsonToList(imReplyLists, ImReplyListVo.class);
        ListVO listVO = new ListVO();
        listVO.setList(imReplyListVoList);
        return ActionResult.success(listVO);
    }
    /**
     * 删除聊天记录
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "删除聊天记录")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @DeleteMapping("/deleteChatRecord/{id}")
    public ActionResult deleteChatRecord(@PathVariable("id") String id){
        imContentService.deleteChatRecord(userProvider.get().getUserId(),id);
        return ActionResult.success("");
    }
    /**
     * 移除会话列表
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "移除会话列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @DeleteMapping("/relocation/{id}")
    public ActionResult relocation(@PathVariable("id") String id){
        imReplyService.relocation(userProvider.get().getUserId(),id);
        return ActionResult.success("");
    }

}
