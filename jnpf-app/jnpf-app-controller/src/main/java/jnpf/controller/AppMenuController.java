package jnpf.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.vo.ListVO;
import jnpf.model.AppMenuListVO;
import jnpf.model.UserMenuModel;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.service.AuthorizeService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * app应用
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-08
 */
@Tag(name = "app应用", description = "Menu")
@RestController
@RequestMapping("/api/app/Menu")
public class AppMenuController {

    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private UserProvider userProvider;

    /**
     * 获取菜单列表
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取菜单列表")
    @GetMapping
    public ActionResult<ListVO<AppMenuListVO>> list(Page page) {
        AuthorizeVO authorizeModel = authorizeService.getAuthorize(true, false);
        List<ModuleModel> buttonListAll = authorizeModel.getModuleList().stream().filter(t -> "App".equals(t.getCategory())).collect(Collectors.toList());
        // 通过系统id捞取相应的菜单
        buttonListAll = buttonListAll.stream().filter(t -> userProvider.get().getAppSystemId() != null && userProvider.get().getAppSystemId().equals(t.getSystemId())).collect(Collectors.toList());
        List<ModuleModel> buttonList = buttonListAll;
        if (StringUtil.isNotEmpty(page.getKeyword())) {
            buttonList = buttonListAll.stream().filter(t -> t.getFullName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        List<UserMenuModel> list = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(buttonList, buttonListAll), UserMenuModel.class);
        List<SumTree<UserMenuModel>> menuAll = TreeDotUtils.convertListToTreeDot(list, "-1");
        List<AppMenuListVO> data = JsonUtil.getJsonToList(menuAll, AppMenuListVO.class);
        ListVO listVO = new ListVO();
        listVO.setList(data);
        return ActionResult.success(listVO);
    }


}
