package gas.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import gas.service.*;
import gas.entity.*;
import jnpf.util.*;
import gas.model.gasbaseservice.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;
import jnpf.annotation.JnpfField;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.ProvinceEntity;
import java.io.IOException;
import java.util.stream.Collectors;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.exception.WorkFlowException;
import jnpf.model.visualJson.UploaderTemplateModel;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务区设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Slf4j
@RestController
@Tag(name = "服务区设置" , description = "gas")
@RequestMapping("/api/gas/GasBaseService")
public class GasBaseServiceController {

    @Autowired
    private GeneraterSwapUtil generaterSwapUtil;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private GasBaseServiceService gasBaseServiceService;



    /**
    * 列表
    *
    * @param gasBaseServicePagination
    * @return
    */
    @Operation(summary = "获取列表")
    @PostMapping("/getList")
    public ActionResult list(@RequestBody GasBaseServicePagination gasBaseServicePagination)throws IOException{
        List<GasBaseServiceEntity> list= gasBaseServiceService.getList(gasBaseServicePagination);
        List<Map<String, Object>> realList=new ArrayList<>();
        for (GasBaseServiceEntity entity : list) {
        Map<String, Object> gasBaseServiceMap=JsonUtil.entityToMap(entity);
        gasBaseServiceMap.put("id", gasBaseServiceMap.get("id"));
        //副表数据
        //子表数据
            realList.add(gasBaseServiceMap);
        }
        //数据转换
        realList = generaterSwapUtil.swapDataList(realList, GasBaseServiceConstant.getFormData(), GasBaseServiceConstant.getColumnData(), gasBaseServicePagination.getModuleId(),false);

        //返回对象
        PageListVO vo = new PageListVO();
        vo.setList(realList);
        PaginationVO page = JsonUtil.getJsonToBean(gasBaseServicePagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }
    /**
    * 创建
    *
    * @param gasBaseServiceForm
    * @return
    */
    @PostMapping()
    @Operation(summary = "创建")
    public ActionResult create(@RequestBody @Valid GasBaseServiceForm gasBaseServiceForm) {
        String b = gasBaseServiceService.checkForm(gasBaseServiceForm,0);
        if (StringUtil.isNotEmpty(b)){
            return ActionResult.fail(b );
        }
        try{
            gasBaseServiceService.saveOrUpdate(gasBaseServiceForm, null ,true);
        }catch(Exception e){
            return ActionResult.fail("新增数据失败");
        }
        return ActionResult.success("创建成功");
    }
    /**
    * 编辑
    * @param id
    * @param gasBaseServiceForm
    * @return
    */
    @PutMapping("/{id}")
    @Operation(summary = "更新")
    public ActionResult update(@PathVariable("id") String id,@RequestBody @Valid GasBaseServiceForm gasBaseServiceForm,
                                @RequestParam(value = "isImport", required = false) boolean isImport){
        if (!isImport) {
            String b =  gasBaseServiceService.checkForm(gasBaseServiceForm,1);
            if (StringUtil.isNotEmpty(b)){
                return ActionResult.fail(b );
            }
        }
        GasBaseServiceEntity entity= gasBaseServiceService.getInfo(id);
        if(entity!=null){
            try{
                gasBaseServiceService.saveOrUpdate(gasBaseServiceForm,id,false);
            }catch(Exception e){
                return ActionResult.fail("修改数据失败");
            }
            return ActionResult.success("更新成功");
        }else{
            return ActionResult.fail("更新失败，数据不存在");
        }
    }
    /**
    * 删除
    * @param id
    * @return
    */
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id){
        GasBaseServiceEntity entity= gasBaseServiceService.getInfo(id);
        if(entity!=null){
            //主表数据删除
            gasBaseServiceService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }
    /**
    * 表单信息(详情页)
    * 详情页面使用-转换数据
    * @param id
    * @return
    */
    @Operation(summary = "表单信息(详情页)")
    @GetMapping("/detail/{id}")
    public ActionResult detailInfo(@PathVariable("id") String id){
        GasBaseServiceEntity entity= gasBaseServiceService.getInfo(id);
        if(entity==null){
            return ActionResult.fail("表单数据不存在！");
        }
        Map<String, Object> gasBaseServiceMap=JsonUtil.entityToMap(entity);
        gasBaseServiceMap.put("id", gasBaseServiceMap.get("id"));
        //副表数据
        //子表数据
        gasBaseServiceMap = generaterSwapUtil.swapDataDetail(gasBaseServiceMap,GasBaseServiceConstant.getFormData(),"562603695003410309",false);
        return ActionResult.success(gasBaseServiceMap);
    }
    /**
    * 获取详情(编辑页)
    * 编辑页面使用-不转换数据
    * @param id
    * @return
    */
    @Operation(summary = "信息")
    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id){
        GasBaseServiceEntity entity= gasBaseServiceService.getInfo(id);
        if(entity==null){
            return ActionResult.fail("表单数据不存在！");
        }
        Map<String, Object> gasBaseServiceMap=JsonUtil.entityToMap(entity);
        gasBaseServiceMap.put("id", gasBaseServiceMap.get("id"));
        //副表数据
        //子表数据
        gasBaseServiceMap = generaterSwapUtil.swapDataForm(gasBaseServiceMap,GasBaseServiceConstant.getFormData(),GasBaseServiceConstant.TABLEFIELDKEY,GasBaseServiceConstant.TABLERENAMES);
        return ActionResult.success(gasBaseServiceMap);
    }

}
