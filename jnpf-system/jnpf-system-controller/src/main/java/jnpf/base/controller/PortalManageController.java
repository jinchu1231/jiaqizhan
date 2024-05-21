package jnpf.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.PortalManageEntity;
import jnpf.base.model.portalManage.PortalManageCreForm;
import jnpf.base.model.portalManage.PortalManagePage;
import jnpf.base.model.portalManage.PortalManagePageDO;
import jnpf.base.model.portalManage.PortalManagePrimary;
import jnpf.base.model.portalManage.PortalManageSelectModel;
import jnpf.base.model.portalManage.PortalManageUpForm;
import jnpf.base.model.portalManage.PortalManageVO;
import jnpf.base.service.PortalManageService;
import jnpf.base.vo.PageListVO;
import jnpf.constant.MsgCode;
import jnpf.permission.model.portalManage.AuthorizePortalManagePrimary;
import jnpf.permission.service.AuthorizeService;
import jnpf.portal.service.PortalService;
import jnpf.util.XSSEscape;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门户管理
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.6
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-02-16
 */
@Slf4j
@RestController
@Tag(name = "门户管理", description = "PortalManage")
@RequestMapping("/api/system/PortalManage")
public class PortalManageController {

    @Autowired
    PortalManageService portalManageService;
    @Autowired
    PortalService portalService;
    @Autowired
    private AuthorizeService authorizeService;

    @Operation(summary = "新增")
    @PostMapping
    public ActionResult<String> create(@RequestBody @Valid PortalManageCreForm portalManageForm) {
        PortalManageEntity entity = portalManageForm.convertEntity();
        try {
            portalManageService.checkCreUp(entity);
        } catch (Exception e) {
            return ActionResult.fail(e.getMessage());
        }
        portalManageService.save(entity);
        return ActionResult.success(MsgCode.SU018.get());
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable String id) {
        boolean flag = portalManageService.removeById(id);
        if(flag){
            // 删除绑定的所有权限
            authorizeService.remove(new AuthorizePortalManagePrimary(null, id).getQuery());
            return ActionResult.success(MsgCode.SU003.get());
        } else {
            return ActionResult.fail("删除失败");
        }
    }

    @Operation(summary = "编辑")
    @PutMapping("/{id}")
    public ActionResult<String> update(@PathVariable("id") String id, @RequestBody @Valid PortalManageUpForm portalManageUpForm){
        PortalManageEntity update = portalManageUpForm.convertEntity();
        try {
            portalManageService.checkCreUp(update);
        } catch (Exception e) {
            return ActionResult.fail(e.getMessage());
        }
        portalManageService.updateById(update);
        return ActionResult.success(MsgCode.SU004.get());
    }

    @Operation(summary = "查看")
    @GetMapping("/{id}")
    public ActionResult<PortalManageVO> getOne(@PathVariable("id") String id) {
        PortalManageEntity entity = portalManageService.getById(id);
        return ActionResult.success(portalManageService.convertVO(entity));
    }

    @Operation(summary = "列表")
    @GetMapping("/list/{systemId}")
    public ActionResult<PageListVO<PortalManageVO>> getPage(@PathVariable("systemId") String systemId, PortalManagePage pmPage) {
        pmPage.setSystemId(systemId);
        return ActionResult.page(
                portalManageService.getPage(pmPage).getRecords()
                        .stream().map(PortalManagePageDO::convert).collect(Collectors.toList()),
                pmPage.getPaginationVO());
    }

    @Operation(summary = "获取集合")
    @GetMapping("/getList")
    public List<PortalManageVO> getList(@RequestParam("systemId") String systemId, @RequestParam("platform") String platform) {
        return portalManageService.getList(new PortalManagePrimary(platform, null, systemId));
    }

    @GetMapping("/getListByPortalIdAndPlatform")
    public List<PortalManageVO> getListByPortalIdAndPlatform(@RequestParam("portalId") String portalId, @RequestParam("platform") String platform) {
        return portalManageService.getList(new PortalManagePrimary(platform, portalId, null));
    }

    @Operation(summary = "获取有效集合")
    @GetMapping("/getListByEnable")
    public List<PortalManageVO> getListByEnable(@RequestParam("systemId") String systemId, @RequestParam("platform") String platform) {
        PortalManagePrimary primary = new PortalManagePrimary(platform, null, systemId);
        primary.getQuery().lambda().eq(PortalManageEntity::getEnabledMark, 1);
        return portalManageService.getList(primary);
    }

    @Operation(summary = "获取所有信息")
    @GetMapping("/getAll")
    public List<PortalManageEntity> getAll() {
        return portalManageService.list();
    }

    @PostMapping("/listByIdsAndPlatform")
    public List<PortalManageEntity> listByIdsAndPlatform(@RequestBody List<String> ids, @RequestParam("platform") String platform){
        QueryWrapper<PortalManageEntity> query = new QueryWrapper<>();
        query.lambda().eq(PortalManageEntity::getEnabledMark, 1)
                .eq(PortalManageEntity::getPlatform, platform)
                .in(PortalManageEntity::getId, ids);
        return portalManageService.list(query);
    }
    @Operation(summary = "获取集合根据门户Id")
    @GetMapping("/listByPortalId")
    public List<PortalManageVO> getListByPortalId(@RequestParam("portalId")String portalId) {
        return portalManageService.getList(new PortalManagePrimary(null, portalId, null));
    }

    @PostMapping("/createBatch")
    public void createBatch(@RequestParam("platform")String platform, @RequestParam("portalId")String portalId,
                            @RequestBody List<String> systemIdList) {
        try {
            portalManageService.createBatch(systemIdList.stream().map(systemId->
                    new PortalManagePrimary(platform, portalId, systemId)).collect(Collectors.toList()));
        }catch (Exception e){

        }
    }

    @PostMapping("/getSelectList")
    public List<PortalManagePageDO> getSelectList(@RequestBody PortalManagePage portalManagePage) {
        return XSSEscape.escapeObj(portalManageService.getSelectList(XSSEscape.escapeObj(portalManagePage)));
    }

    @PostMapping("/selectPortalBySystemIds")
    public List<PortalManagePageDO> selectPortalBySystemIds(@RequestBody PortalManageSelectModel model) {
        return portalManageService.selectPortalBySystemIds(model.getSystemIds(), model.getCollect());
    }

}
