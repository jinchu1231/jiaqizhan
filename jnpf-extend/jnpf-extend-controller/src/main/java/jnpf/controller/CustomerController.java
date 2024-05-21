package jnpf.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.PageListVO;
import jnpf.entity.CustomerEntity;
import jnpf.model.customer.CustomerCrForm;
import jnpf.model.customer.CustomerInfoVO;
import jnpf.model.customer.CustomerListVO;
import jnpf.model.customer.CustomerUpForm;
import jnpf.service.CustomerService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
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
 * 客户信息
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 14:09:05
 */
@Slf4j
@RestController
@Tag(name = "客户信息", description = "Customer")
@RequestMapping("/api/extend/saleOrder/Customer")
public class CustomerController extends SuperController<CustomerService, CustomerEntity> {

    @Autowired
    private CustomerService customerService;

    /**
     * 列表
     *
     * @param pagination 分页模型
     * @return
     */
    @GetMapping
    @Operation(summary = "列表")
    public ActionResult<PageListVO<CustomerListVO>> list(Pagination pagination) {
        pagination.setPageSize(50);
        pagination.setCurrentPage(1);
        List<CustomerEntity> list = customerService.getList(pagination);
        List<CustomerListVO> listVO = JsonUtil.getJsonToList(list, CustomerListVO.class);
        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 创建
     *
     * @param customerCrForm 新建模型
     * @return
     */
    @PostMapping
    @Operation(summary = "创建")
    @Parameters({
            @Parameter(name = "customerCrForm", description = "客户模型",required = true),
    })
    public ActionResult create(@RequestBody @Valid CustomerCrForm customerCrForm) {
        CustomerEntity entity = JsonUtil.getJsonToBean(customerCrForm, CustomerEntity.class);
        customerService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<CustomerInfoVO> info(@PathVariable("id") String id) {
        CustomerEntity entity = customerService.getInfo(id);
        CustomerInfoVO vo = JsonUtil.getJsonToBean(entity, CustomerInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 更新
     *
     * @param id             主键
     * @param customerUpForm 修改模型
     * @return
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "customerUpForm", description = "客户模型", required = true),
    })
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid CustomerUpForm customerUpForm) {
        CustomerEntity entity = JsonUtil.getJsonToBean(customerUpForm, CustomerEntity.class);
        boolean ok = customerService.update(id, entity);
        if (ok) {
            return ActionResult.success("更新成功");
        }
        return ActionResult.fail("更新失败，数据不存在");
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult delete(@PathVariable("id") String id) {
        CustomerEntity entity = customerService.getInfo(id);
        if (entity != null) {
            customerService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }


    /**
     * 创建
     *
     * @return
     */
    @Operation(summary = "创建客户：SEATA调用示例成功接口")
    @PostMapping("/createSuccess")
    public ActionResult createSuccess() {
        CustomerCrForm customerCrForm1 = new CustomerCrForm();
        customerCrForm1.setCustomerName(RandomUtil.uuId());
        customerCrForm1.setCode(RandomUtil.uuId());
        customerCrForm1.setName(RandomUtil.uuId());
        customerCrForm1.setAddress(RandomUtil.uuId());
        return create(customerCrForm1);
    }


    /**
     * 创建
     *
     * @return
     */
    @Operation(summary = "创建客户：SEATA调用示例失败接口")
    @PostMapping("/createFail")
    public ActionResult createFail() {
        CustomerCrForm customerCrForm1 = new CustomerCrForm();
        customerCrForm1.setCustomerName(RandomUtil.uuId());
        customerCrForm1.setCode(RandomUtil.uuId());
        customerCrForm1.setName(RandomUtil.uuId());
        customerCrForm1.setAddress(RandomUtil.uuId());
        ActionResult r = create(customerCrForm1);
        Assert.isNull(r, "主动报错");
        return r;
    }


}
