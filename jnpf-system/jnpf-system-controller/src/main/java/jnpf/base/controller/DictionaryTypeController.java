package jnpf.base.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.base.model.dictionarytype.DictionaryTypeCrForm;
import jnpf.base.model.dictionarytype.DictionaryTypeInfoVO;
import jnpf.base.model.dictionarytype.DictionaryTypeListVO;
import jnpf.base.model.dictionarytype.DictionaryTypeModel;
import jnpf.base.model.dictionarytype.DictionaryTypeUpForm;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.vo.ListVO;
import jnpf.exception.DataException;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

/**
 * 字典分类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "数据字典分类", description = "DictionaryType")
@RestController
@RequestMapping("/api/system/DictionaryType")
public class DictionaryTypeController extends SuperController<DictionaryTypeService, DictionaryTypeEntity> {

    @Autowired
    private DictionaryTypeService dictionaryTypeService;

    /**
     * 获取字典分类
     *
     * @return
     */
    @Operation(summary = "获取字典分类")
    @GetMapping
    public ActionResult<ListVO<DictionaryTypeListVO>> list() {
        List<DictionaryTypeEntity> data = dictionaryTypeService.getList();
        List<DictionaryTypeModel> voListVO = JsonUtil.getJsonToList(data, DictionaryTypeModel.class);
        voListVO.forEach(vo -> {
            if (StringUtil.isNotEmpty(vo.getCategory()) && "1".equals(vo.getCategory()) && "-1".equals(vo.getParentId())) {
                vo.setCategory("系统");
                vo.setParentId("1");
            } else if (StringUtil.isNotEmpty(vo.getCategory()) && "0".equals(vo.getCategory()) && "-1".equals(vo.getParentId())) {
                vo.setCategory("业务");
                vo.setParentId("0");
            }
        });
        List<SumTree<DictionaryTypeModel>> sumTrees = TreeDotUtils.convertListToTreeDot(voListVO);
        List<DictionaryTypeListVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryTypeListVO.class);

        DictionaryTypeListVO parentVO = new DictionaryTypeListVO();
        parentVO.setFullName("系统字典");
        parentVO.setChildren(new ArrayList<>());
        parentVO.setId("1");
        DictionaryTypeListVO parentVO1 = new DictionaryTypeListVO();
        parentVO1.setFullName("业务字典");
        parentVO1.setChildren(new ArrayList<>());
        parentVO1.setId("0");

        list.forEach(vo -> {
            if ("系统".equals(vo.getCategory())) {
                List<DictionaryTypeListVO> children = parentVO.getChildren();
                children.add(vo);
                parentVO.setHasChildren(true);
            }else {
                List<DictionaryTypeListVO> children = parentVO1.getChildren();
                children.add(vo);
                parentVO1.setHasChildren(true);
            }
        });
        List<DictionaryTypeListVO> listVo = new ArrayList<>();
        listVo.add(parentVO1);
        listVo.add(parentVO);

        ListVO<DictionaryTypeListVO> vo = new ListVO<>();
        vo.setList(listVo);
        return ActionResult.success(vo);
    }


    /**
     * 获取字典分类
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取所有字典分类下拉框列表")
    @Parameter(name = "id", description = "主键", required = true)
    @GetMapping("/Selector/{id}")
    public ActionResult<ListVO<DictionaryTypeListVO>> selectorTreeView(@PathVariable("id") String id) {
        List<DictionaryTypeEntity> data = dictionaryTypeService.getList();
        if (!"0".equals(id)) {
            data.remove(dictionaryTypeService.getInfo(id));
        }
        List<DictionaryTypeModel> voListVO = JsonUtil.getJsonToList(data, DictionaryTypeModel.class);
        voListVO.forEach(vo -> {
            if (StringUtil.isNotEmpty(vo.getCategory()) && "1".equals(vo.getCategory()) && "-1".equals(vo.getParentId())) {
                vo.setCategory("系统");
                vo.setParentId("1");
            } else if (StringUtil.isNotEmpty(vo.getCategory()) && "0".equals(vo.getCategory()) && "-1".equals(vo.getParentId())) {
                vo.setCategory("业务");
                vo.setParentId("0");
            }
        });
        List<SumTree<DictionaryTypeModel>> sumTrees = TreeDotUtils.convertListToTreeDot(voListVO);
        List<DictionaryTypeListVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryTypeListVO.class);

        DictionaryTypeListVO parentVO = new DictionaryTypeListVO();
        parentVO.setFullName("系统字典");
        parentVO.setChildren(new ArrayList<>());
        parentVO.setId("1");
        DictionaryTypeListVO parentVO1 = new DictionaryTypeListVO();
        parentVO1.setFullName("业务字典");
        parentVO1.setChildren(new ArrayList<>());
        parentVO1.setId("0");

        list.forEach(vo -> {
            if ("系统".equals(vo.getCategory())) {
                List<DictionaryTypeListVO> children = parentVO.getChildren();
                children.add(vo);
                parentVO.setHasChildren(true);
            }else {
                List<DictionaryTypeListVO> children = parentVO1.getChildren();
                children.add(vo);
                parentVO1.setHasChildren(true);
            }
        });
        List<DictionaryTypeListVO> listVo = new ArrayList<>();
        listVo.add(parentVO1);
        listVo.add(parentVO);

        ListVO<DictionaryTypeListVO> vo = new ListVO<>();
        vo.setList(listVo);
        return ActionResult.success(vo);
    }

    /**
     * 获取字典分类信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取字典分类信息")
    @Parameter(name = "id", description = "主键", required = true)
    @GetMapping("/{id}")
    public ActionResult<DictionaryTypeInfoVO> info(@PathVariable("id") String id) throws DataException {
        DictionaryTypeEntity entity = dictionaryTypeService.getInfo(id);
        if ("-1".equals(entity.getParentId())) {
            entity.setParentId(String.valueOf(entity.getCategory()));
        }
        DictionaryTypeInfoVO vo = JsonUtil.getJsonToBeanEx(entity, DictionaryTypeInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 添加字典分类
     *
     * @param dictionaryTypeCrForm 实体对象
     * @return
     */
    @Operation(summary = "添加字典分类")
    @Parameter(name = "dictionaryTypeCrForm", description = "实体对象", required = true)
    @SaCheckPermission("systemData.dictionary")
    @PostMapping
    public ActionResult create(@RequestBody @Valid DictionaryTypeCrForm dictionaryTypeCrForm) {
        DictionaryTypeEntity entity = JsonUtil.getJsonToBean(dictionaryTypeCrForm, DictionaryTypeEntity.class);
        if ("0".equals(entity.getParentId()) || "1".equals(entity.getParentId())) {
            entity.setCategory(Integer.parseInt(entity.getParentId()));
            entity.setParentId("-1");
        } else {
            DictionaryTypeEntity entity1 = dictionaryTypeService.getInfo(dictionaryTypeCrForm.getParentId());
            entity.setCategory(entity1.getCategory());
        }
        if (dictionaryTypeService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail("名称不能重复");
        }
        if (dictionaryTypeService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("编码不能重复");
        }
        dictionaryTypeService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改字典分类
     *
     * @param dictionaryTypeUpForm 实体对象
     * @param id                   主键值
     * @return
     */
    @Operation(summary = "修改字典分类")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "dictionaryTypeUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("systemData.dictionary")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid DictionaryTypeUpForm dictionaryTypeUpForm) {
        DictionaryTypeEntity entity = JsonUtil.getJsonToBean(dictionaryTypeUpForm, DictionaryTypeEntity.class);
        if ("0".equals(entity.getParentId()) || "1".equals(entity.getParentId())) {
            entity.setCategory(Integer.parseInt(entity.getParentId()));
            entity.setParentId("-1");
        } else {
            DictionaryTypeEntity entity1 = dictionaryTypeService.getInfo(dictionaryTypeUpForm.getParentId());
            entity.setCategory(entity1.getCategory());
        }
        if (dictionaryTypeService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail("名称不能重复");
        }
        if (dictionaryTypeService.isExistByEnCode(entity.getEnCode(), id)) {
            return ActionResult.fail("编码不能重复");
        }
        boolean flag = dictionaryTypeService.update(id, entity);
        if (!flag) {
            return ActionResult.success("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除字典分类
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除字典分类")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("systemData.dictionary")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        DictionaryTypeEntity entity = dictionaryTypeService.getInfo(id);
        if (entity != null) {
            boolean isOk = dictionaryTypeService.delete(entity);
            if (isOk) {
                return ActionResult.success("删除成功");
            } else {
                return ActionResult.fail("字典类型下面有字典值禁止删除");
            }
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

}
