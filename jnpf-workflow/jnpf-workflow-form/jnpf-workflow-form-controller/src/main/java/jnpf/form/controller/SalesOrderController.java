package jnpf.form.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.constant.MsgCode;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.exception.WorkFlowException;
import jnpf.form.entity.SalesOrderEntity;
import jnpf.form.entity.SalesOrderEntryEntity;
import jnpf.form.model.salesorder.SalesOrderEntryEntityInfoModel;
import jnpf.form.model.salesorder.SalesOrderForm;
import jnpf.form.model.salesorder.SalesOrderInfoVO;
import jnpf.form.service.SalesOrderService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 销售订单
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
@Tag(name = "销售订单", description = "SalesOrder")
@RestController
@RequestMapping("/api/workflow/Form/SalesOrder")
public class SalesOrderController extends SuperController<SalesOrderService, SalesOrderEntity> {

    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * 获取销售订单信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取销售订单信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult info(@PathVariable("id") String id) {
        SalesOrderEntity entity = salesOrderService.getInfo(id);
        List<SalesOrderEntryEntity> entityList = salesOrderService.getSalesEntryList(id);
        SalesOrderInfoVO vo = JsonUtil.getJsonToBean(entity, SalesOrderInfoVO.class);
        if (vo != null) {
            vo.setEntryList(JsonUtil.getJsonToList(entityList, SalesOrderEntryEntityInfoModel.class));
        }
        return ActionResult.success(vo);
    }

    /**
     * 新建销售订单
     *
     * @param salesOrderForm 表单对象
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "新建销售订单")
    @PostMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "salesOrderForm", description = "销售模型", required = true),
    })
    public ActionResult create(@RequestBody SalesOrderForm salesOrderForm, @PathVariable("id") String id) throws WorkFlowException {
        SalesOrderEntity sales = JsonUtil.getJsonToBean(salesOrderForm, SalesOrderEntity.class);
        List<SalesOrderEntryEntity> salesEntryList = JsonUtil.getJsonToList(salesOrderForm.getEntryList(), SalesOrderEntryEntity.class);
        if (FlowStatusEnum.save.getMessage().equals(salesOrderForm.getStatus())) {
            salesOrderService.save(id, sales, salesEntryList, salesOrderForm);
            return ActionResult.success(MsgCode.SU002.get());
        }
        salesOrderService.submit(id, sales, salesEntryList, salesOrderForm);
        return ActionResult.success(MsgCode.SU006.get());
    }

    /**
     * 修改销售订单
     *
     * @param salesOrderForm 表单对象
     * @param id             主键
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "修改销售订单")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "salesOrderForm", description = "销售模型", required = true),
    })
    public ActionResult update(@RequestBody SalesOrderForm salesOrderForm, @PathVariable("id") String id) throws WorkFlowException {
        SalesOrderEntity sales = JsonUtil.getJsonToBean(salesOrderForm, SalesOrderEntity.class);
        sales.setId(id);
        List<SalesOrderEntryEntity> salesEntryList = JsonUtil.getJsonToList(salesOrderForm.getEntryList(), SalesOrderEntryEntity.class);
        if (FlowStatusEnum.save.getMessage().equals(salesOrderForm.getStatus())) {
            salesOrderService.save(id, sales, salesEntryList, salesOrderForm);
            return ActionResult.success(MsgCode.SU002.get());
        }
        salesOrderService.submit(id, sales, salesEntryList, salesOrderForm);
        return ActionResult.success(MsgCode.SU006.get());
    }
}
