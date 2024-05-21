package jnpf.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.exception.DataException;
import jnpf.message.entity.ImContentEntity;
import jnpf.message.model.imrecord.PaginationImRecordModel;
import jnpf.message.model.websocket.MessageListVo;
import jnpf.message.service.ImContentService;
import jnpf.util.JsonUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 聊天记录接口
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-05
 */
@Tag(description = "ImRecord", name = "聊天记录")
@RestController
@RequestMapping("/api/message/ImRecord")
public class ImRecordController {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ImContentService imContentService;

    /**
     * 聊天记录
     *
     * @param imRecordModel 分页模型
     * @return ignore
     */
    @Operation(summary = "聊天记录列表")
    @GetMapping("/MessageList")
    public ActionResult<PageListVO<MessageListVo>> messageList(PaginationImRecordModel imRecordModel) throws DataException {
        PaginationVO paginationVO = JsonUtil.getJsonToBean(imRecordModel, PaginationVO.class);
        List<ImContentEntity> data = imContentService.getMessageList(imRecordModel);
//                .stream().sorted(Comparator.comparing()).collect(Collectors.toList());
        List<MessageListVo> listVOS = JsonUtil.getJsonToList(data, MessageListVo.class);
        return ActionResult.page(listVOS, paginationVO);
    }

    /**
     * 更新已读
     * @param formUserId
     * @return
     */
    @Operation(summary = "更新已读")
    @PostMapping("/UpdateReadMessage")
    public void updateReadMessage(String formUserId) throws DataException {
        imContentService.readMessage(formUserId, UserProvider.getLoginUserId());
    }

}
