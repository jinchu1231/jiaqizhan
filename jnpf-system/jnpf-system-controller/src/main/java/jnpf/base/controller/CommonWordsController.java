package jnpf.base.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.CommonWordsEntity;
import jnpf.base.model.commonword.ComWordsPagination;
import jnpf.base.model.commonword.CommonWordsForm;
import jnpf.base.model.commonword.CommonWordsVO;
import jnpf.base.service.CommonWordsService;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 常用语控制类
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.6
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-01-06
 */
@Tag(name = "审批常用语", description = "commonWords")
@RestController
@RequestMapping("/api/system/CommonWords")
public class CommonWordsController extends SuperController<CommonWordsService, CommonWordsEntity> {

    @Autowired
    private CommonWordsService commonWordsService;


    /**
     * 列表
     *
     * @param comWordsPagination 页面参数对象
     * @return 列表结果集
     */
    @Operation(summary = "当前系统应用列表")
    @GetMapping()
    public ActionResult<PageListVO<CommonWordsVO>> getList(ComWordsPagination comWordsPagination) {
        List<CommonWordsEntity> entityList = commonWordsService.getSysList(comWordsPagination, false);
        List<CommonWordsVO> voList = JsonUtil.getJsonToList(entityList, CommonWordsVO.class);
        return ActionResult.page(voList, JsonUtil.getJsonToBean(comWordsPagination, PaginationVO.class));
    }

    @Operation(summary = "获取信息")
    @GetMapping("/{id}")
    public ActionResult<CommonWordsVO> getInfo(@PathVariable String id) {
        CommonWordsEntity entity = commonWordsService.getById(id);
        CommonWordsVO vo = JsonUtil.getJsonToBean(entity, CommonWordsVO.class);
        return ActionResult.success(vo);
    }

    @Operation(summary = "下拉列表")
    @GetMapping("/Selector")
    public ActionResult<ListVO<CommonWordsVO>> getSelect(String type) {
        List<CommonWordsVO> voList = JsonUtil.getJsonToList(commonWordsService.getListModel(type), CommonWordsVO.class);
        return ActionResult.success(new ListVO<>(voList));
    }

    @Operation(summary = "新建")
    @PostMapping("")
    public ActionResult create(@RequestBody CommonWordsForm commonWordsForm) {
        CommonWordsEntity entity = JsonUtil.getJsonToBean(commonWordsForm, CommonWordsEntity.class);
        entity.setId(RandomUtil.uuId());
        commonWordsService.save(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public ActionResult update(@RequestBody CommonWordsForm commonWordsForm) {
        CommonWordsEntity entity = JsonUtil.getJsonToBean(commonWordsForm, CommonWordsEntity.class);
        entity.setId(commonWordsForm.getId());
        commonWordsService.updateById(entity);
        return ActionResult.success(MsgCode.SU004.get());
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable String id) {
        //对象存在判断
        if (commonWordsService.getById(id) != null) {
            commonWordsService.removeById(id);
            return ActionResult.success(MsgCode.SU003.get());
        } else {
            return ActionResult.fail(MsgCode.FA003.get());
        }
    }

}
