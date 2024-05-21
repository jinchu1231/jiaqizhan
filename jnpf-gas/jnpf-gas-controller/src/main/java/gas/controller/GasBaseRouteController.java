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
import gas.model.gasbaseroute.*;
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
 * 路线管理
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Slf4j
@RestController
@Tag(name = "路线管理" , description = "gas")
@RequestMapping("/api/gas/GasBaseRoute")
public class GasBaseRouteController {


    @Autowired
    private GeneraterSwapUtil generaterSwapUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private GasBaseRouteService gasBaseRouteService;


    /**
    * 列表
    * @param gasBaseRoutePagination
    * @return
    */
    @Operation(summary = "获取列表")
    @PostMapping("/getList")
    public ActionResult list(@RequestBody GasBaseRoutePagination gasBaseRoutePagination)throws IOException{
        List<GasBaseRouteEntity> list= gasBaseRouteService.getList(gasBaseRoutePagination);
        List<Map<String, Object>> realList=new ArrayList<>();
        for (GasBaseRouteEntity entity : list) {
        Map<String, Object> gasBaseRouteMap=JsonUtil.entityToMap(entity);
        gasBaseRouteMap.put("id", gasBaseRouteMap.get("id"));
        //副表数据
        //子表数据
            realList.add(gasBaseRouteMap);
        }
        //数据转换
        realList = generaterSwapUtil.swapDataList(realList, GasBaseRouteConstant.getFormData(), GasBaseRouteConstant.getColumnData(), gasBaseRoutePagination.getModuleId(),false);
        //返回对象
        PageListVO vo = new PageListVO();
        vo.setList(realList);
        PaginationVO page = JsonUtil.getJsonToBean(gasBaseRoutePagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }
    /**
    * 创建
    *
    * @param gasBaseRouteForm
    * @return
    */
    @PostMapping()
    @Operation(summary = "创建")
    public ActionResult create(@RequestBody @Valid GasBaseRouteForm gasBaseRouteForm) {
        String b = gasBaseRouteService.checkForm(gasBaseRouteForm,0);
        if (StringUtil.isNotEmpty(b)){
            return ActionResult.fail(b );
        }
        try{
            gasBaseRouteService.saveOrUpdate(gasBaseRouteForm, null ,true);
        }catch(Exception e){
            e.printStackTrace();
            return ActionResult.fail("新增数据失败");
        }
        return ActionResult.success("创建成功");
    }
    /**
    * 编辑
    * @param id
    * @param gasBaseRouteForm
    * @return
    */
    @PutMapping("/{id}")
    @Operation(summary = "更新")
    public ActionResult update(@PathVariable("id") String id,@RequestBody @Valid GasBaseRouteForm gasBaseRouteForm,
                                @RequestParam(value = "isImport", required = false) boolean isImport){
        if (!isImport) {
            String b =  gasBaseRouteService.checkForm(gasBaseRouteForm,1);
            if (StringUtil.isNotEmpty(b)){
                return ActionResult.fail(b );
            }
        }
        GasBaseRouteEntity entity= gasBaseRouteService.getInfo(id);
        if(entity!=null){
            try{
                gasBaseRouteService.saveOrUpdate(gasBaseRouteForm,id,false);
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
        GasBaseRouteEntity entity= gasBaseRouteService.getInfo(id);
        if(entity!=null){
            //假删除
            entity.setDeleteMark(1);
            entity.setDeleteUserId(userProvider.get().getUserId());
            entity.setDeleteTime(new Date());
            gasBaseRouteService.update(id,entity);
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
        GasBaseRouteEntity entity= gasBaseRouteService.getInfo(id);
        if(entity==null){
            return ActionResult.fail("表单数据不存在！");
        }
        Map<String, Object> gasBaseRouteMap=JsonUtil.entityToMap(entity);
        gasBaseRouteMap.put("id", gasBaseRouteMap.get("id"));
        //副表数据
        //子表数据
        gasBaseRouteMap = generaterSwapUtil.swapDataDetail(gasBaseRouteMap,GasBaseRouteConstant.getFormData(),"562562709497847685",false);
        return ActionResult.success(gasBaseRouteMap);
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
        GasBaseRouteEntity entity= gasBaseRouteService.getInfo(id);
        if(entity==null){
            return ActionResult.fail("表单数据不存在！");
        }
        Map<String, Object> gasBaseRouteMap=JsonUtil.entityToMap(entity);
        gasBaseRouteMap.put("id", gasBaseRouteMap.get("id"));
        //副表数据
        //子表数据
        gasBaseRouteMap = generaterSwapUtil.swapDataForm(gasBaseRouteMap,GasBaseRouteConstant.getFormData(),GasBaseRouteConstant.TABLEFIELDKEY,GasBaseRouteConstant.TABLERENAMES);
        return ActionResult.success(gasBaseRouteMap);
    }

}
