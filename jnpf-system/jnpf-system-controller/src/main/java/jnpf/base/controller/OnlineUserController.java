package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.model.UserOnlineModel;
import jnpf.base.model.UserOnlineVO;
import jnpf.base.model.online.BatchOnlineModel;
import jnpf.base.service.UserOnlineService;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线用户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "在线用户", description = "Online")
@RestController
@RequestMapping("/api/system/OnlineUser")
public class OnlineUserController {

    @Autowired
    private UserOnlineService userOnlineService;

    /**
     * 列表
     *
     * @param page 关键词
     * @return
     */
    @Operation(summary = "获取在线用户列表")
    @SaCheckPermission("permission.userOnline")
    @GetMapping
    public ActionResult<PageListVO<UserOnlineVO>> list(Pagination page) {
        List<UserOnlineModel> data = userOnlineService.getList(page);
        List<UserOnlineVO> voList= data.stream().map(online->{
            UserOnlineVO vo = JsonUtil.getJsonToBean(online, UserOnlineVO.class);
            vo.setUserId(online.getToken());
            //vo.setUserName(vo.getUserName() + "/" + online.getDevice());
            return vo;
        }).collect(Collectors.toList());
        PaginationVO paginationVO = JsonUtil.getJsonToBean(page, PaginationVO.class);
        return ActionResult.page(voList, paginationVO);
    }

    /**
     * 注销
     *
     * @param token token
     * @return
     */
    @Operation(summary = "强制下线")
    @Parameter(name = "token", description = "token", required = true)
    @SaCheckPermission("permission.userOnline")
    @DeleteMapping("/{token}")
    public ActionResult delete(@PathVariable("token") String token) {
        userOnlineService.delete(token);
        return ActionResult.success("操作成功");
    }

    /**
     * 批量下线用户
     *
     * @param model 在线用户id集合
     * @return ignore
     */
    @Operation(summary = "批量下线用户")
    @Parameter(name = "model", description = "在线用户id集合", required = true)
    @SaCheckPermission("permission.userOnline")
    @DeleteMapping
    public ActionResult clear(@RequestBody BatchOnlineModel model) {
        userOnlineService.delete(model.getIds());
        return ActionResult.success("操作成功");
    }

}
