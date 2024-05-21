package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.ComFieldsEntity;
import jnpf.base.model.comfields.ComFieldsCrForm;
import jnpf.base.model.comfields.ComFieldsInfoVO;
import jnpf.base.model.comfields.ComFieldsListVO;
import jnpf.base.model.comfields.ComFieldsUpForm;
import jnpf.base.service.ComFieldsService;
import jnpf.base.vo.ListVO;
import jnpf.exception.DataException;
import jnpf.util.JsonUtil;
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
import java.util.List;

/**
 * 常用字段表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-15 10:29
 */
@RestController
@Tag(name = "常用字段", description = "CommonFields")
@RequestMapping("/api/system/CommonFields")
public class CommonFieldsController extends SuperController<ComFieldsService, ComFieldsEntity> {

    @Autowired
    private ComFieldsService comFieldsService;

    /**
     * 常用字段列表
     *
     * @return
     */
    @Operation(summary = "常用字段列表")
    @SaCheckPermission("systemData.dataModel")
    @GetMapping
    public ActionResult<ListVO<ComFieldsListVO>> list() {
        List<ComFieldsEntity> data = comFieldsService.getList();
        List<ComFieldsListVO> list= JsonUtil.getJsonToList(data,ComFieldsListVO.class);
        ListVO<ComFieldsListVO> vo=new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 常用字段详情
     *
     * @param id 主键
     * @return
     * @throws DataException
     */
    @Operation(summary = "常用字段详情")
    @Parameter(name = "id", description = "主键", required = true)
    @SaCheckPermission("systemData.dataModel")
    @GetMapping("/{id}")
    public ActionResult<ComFieldsInfoVO> info(@PathVariable("id") String id) throws DataException {
        ComFieldsEntity entity = comFieldsService.getInfo(id);
        ComFieldsInfoVO vo= JsonUtil.getJsonToBeanEx(entity,ComFieldsInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建常用字段
     *
     * @param comFieldsCrForm 新建模型
     * @return
     */
    @Operation(summary = "新建常用字段")
    @Parameter(name = "comFieldsCrForm", description = "新建模型", required = true)
    @SaCheckPermission("systemData.dataModel")
    @PostMapping
    public ActionResult create(@RequestBody @Valid ComFieldsCrForm comFieldsCrForm) {
        ComFieldsEntity entity = JsonUtil.getJsonToBean(comFieldsCrForm, ComFieldsEntity.class);
        if (comFieldsService.isExistByFullName(entity.getField(),entity.getId())){
            return ActionResult.fail("名称不能重复");
        }
        comFieldsService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改常用字段
     *
     * @param id 主键
     * @param comFieldsUpForm 修改模型
     * @return
     */
    @Operation(summary = "修改常用字段")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "comFieldsUpForm", description = "修改模型", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ComFieldsUpForm comFieldsUpForm) {
        ComFieldsEntity entity = JsonUtil.getJsonToBean(comFieldsUpForm, ComFieldsEntity.class);
        if (comFieldsService.isExistByFullName(entity.getField(),id)){
            return ActionResult.fail("名称不能重复");
        }
        boolean flag = comFieldsService.update(id, entity);
        if (flag==false){
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除常用字段
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除常用字段")
    @Parameter(name = "id", description = "主键", required = true)
    @SaCheckPermission("systemData.dataModel")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ComFieldsEntity entity = comFieldsService.getInfo(id);
        if (entity != null) {
            comFieldsService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败,数据不存在");
    }
}

