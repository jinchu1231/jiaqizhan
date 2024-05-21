package jnpf.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.entity.ContractEntity;
import jnpf.model.ContractForm;
import jnpf.model.ContractInfoVO;
import jnpf.model.ContractListVO;
import jnpf.service.ContractService;
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
 * Contract
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司(https://www.jnpfsoft.com)
 * 作者： JNPF开发平台组
 * 日期： 2020-12-31
 */
@RestController
@Tag(name = "示例接口", description = "Contract")
@RequestMapping("/Contract")
public class ContractController extends SuperController<ContractService, ContractEntity> {

    @Autowired
    private ContractService contractService;

    /**
     * 获取列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取列表")
    @GetMapping("/List")
    public ActionResult<PageListVO<ContractListVO>> list(Pagination pagination) {
        List<ContractEntity> entity = contractService.getlist(pagination);
        List<ContractListVO> listVo = JsonUtil.getJsonToList(JsonUtil.getObjectToStringDateFormat(entity, "yyyy-MM-dd HH:mm:ss"), ContractListVO.class);
        PaginationVO vo = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVo, vo);
    }

    /**
     * 获取详情
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<ContractInfoVO> info(@PathVariable("id") String id) {
        ContractEntity entity = contractService.getInfo(id);
        ContractInfoVO vo = JsonUtil.getJsonToBean(entity, ContractInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param contractForm 新建模型
     * @return
     */
    @Operation(summary = "新建")
    @PostMapping
    @Parameters({
            @Parameter(name = "contractForm", description = "示例模型",required = true),
    })
    public ActionResult create(@RequestBody @Valid ContractForm contractForm) {
        ContractEntity entity = JsonUtil.getJsonToBean(contractForm, ContractEntity.class);
        contractService.create(entity);
        return ActionResult.success("保存成功");
    }

    /**
     * @param id           主键
     * @param contractForm 修改模型
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "contractForm", description = "示例模型",required = true),
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ContractForm contractForm) {
        ContractEntity entity = JsonUtil.getJsonToBean(contractForm, ContractEntity.class);
        contractService.update(id, entity);
        return ActionResult.success("修改成功");
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult delete(@PathVariable("id") String id) {
        ContractEntity entity = contractService.getInfo(id);
        contractService.delete(entity);
        return ActionResult.success("删除成功");
    }

}
