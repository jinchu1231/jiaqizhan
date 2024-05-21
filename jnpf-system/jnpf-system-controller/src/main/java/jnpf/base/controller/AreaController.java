package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.model.province.AreaModel;
import jnpf.base.model.province.PaginationProvince;
import jnpf.base.model.province.ProvinceCrForm;
import jnpf.base.model.province.ProvinceInfoVO;
import jnpf.base.model.province.ProvinceListVO;
import jnpf.base.model.province.ProvinceSelectListVO;
import jnpf.base.model.province.ProvinceSelectModel;
import jnpf.base.model.province.ProvinceUpForm;
import jnpf.base.service.ProvinceService;
import jnpf.base.vo.ListVO;
import jnpf.exception.DataException;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.treeutil.ListToTreeUtil;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 行政区划
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "行政区划" , description = "Area" )
@RestController
@RequestMapping("/api/system/Area" )
public class AreaController extends SuperController<ProvinceService, ProvinceEntity> {

    @Autowired
    private ProvinceService provinceService;

    /**
     * 列表（异步加载）
     *
     * @param nodeId 节点主键
     * @param page   关键字
     * @return
     */
    @Operation(summary = "列表（异步加载）" )
    @Parameters({
            @Parameter(name = "nodeId" , description = "节点主键" , required = true)
    })
    @SaCheckPermission("system.area" )
    @GetMapping("/{nodeId}" )
    public ActionResult<ListVO<ProvinceListVO>> list(@PathVariable("nodeId" ) String nodeId, PaginationProvince page) {
        List<ProvinceEntity> data = provinceService.getList(nodeId, page);
        List<ProvinceEntity> dataAll = data;
        List<ProvinceEntity> result = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data, dataAll), ProvinceEntity.class);
        List<ProvinceListVO> treeList = JsonUtil.getJsonToList(result, ProvinceListVO.class);
        int i = 0;
        for (ProvinceListVO entity : treeList) {
            boolean childNode = provinceService.getList(entity.getId()).size() <= 0;
            ProvinceListVO provinceListVO = JsonUtil.getJsonToBean(entity, ProvinceListVO.class);
            provinceListVO.setIsLeaf(childNode);
            provinceListVO.setHasChildren(!childNode);
            treeList.set(i, provinceListVO);
            i++;
        }
        ListVO<ProvinceListVO> vo = new ListVO<>();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 获取行政区划下拉框数据
     *
     * @param id  主键
     * @param ids 主键集合
     * @return
     */
    @Operation(summary = "获取行政区划下拉框数据" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true),
            @Parameter(name = "ids" , description = "主键集合" , required = true)
    })
    @GetMapping("/{id}/Selector/{ids}" )
    public ActionResult<ListVO<ProvinceSelectListVO>> selectList(@PathVariable("id" ) String id, @PathVariable("ids" ) String ids) {
        List<ProvinceEntity> data = provinceService.getList(id);
        data = data.stream().filter(t -> t.getEnabledMark() == 1).collect(Collectors.toList());
        if (!"0".equals(ids)) {
            //排除子集
            filterData(data, new ArrayList<>(Arrays.asList(new String[]{ids})));
        }
        List<ProvinceSelectListVO> treeList = JsonUtil.getJsonToList(data, ProvinceSelectListVO.class);
        int i = 0;
        for (ProvinceSelectListVO entity : treeList) {
//            boolean childNode = provinceService.getList(entity.getId()).size() <= 0;
            ProvinceSelectListVO provinceListVO = JsonUtil.getJsonToBean(entity, ProvinceSelectListVO.class);
            provinceListVO.setIsLeaf(false);
            treeList.set(i, provinceListVO);
            i++;
        }
        ListVO<ProvinceSelectListVO> vo = new ListVO<>();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 递归排除子集
     *
     * @param data
     * @param id
     */
    private void filterData(List<ProvinceEntity> data, List<String> id) {
        List<ProvinceEntity> collect = null;
        //获取子集信息
        for (String ids : id) {
            collect = data.stream().filter(t -> ids.equals(t.getParentId())).collect(Collectors.toList());
            data.removeAll(collect);
        }
        //递归移除子集的子集
        if (collect.size() > 0) {
            filterData(data, collect.stream().map(t -> t.getId()).collect(Collectors.toList()));
        }
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取行政区划信息" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @SaCheckPermission("system.area" )
    @GetMapping("/{id}/Info" )
    public ActionResult<ProvinceInfoVO> info(@PathVariable("id" ) String id) throws DataException {
        ProvinceEntity entity = provinceService.getInfo(id);
        ProvinceInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, ProvinceInfoVO.class);
        if (!"-1".equals(entity.getParentId())) {
            ProvinceEntity parent = provinceService.getInfo(entity.getParentId());
            vo.setParentName(parent.getFullName());
        }
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param provinceCrForm 实体对象
     * @return
     */
    @Operation(summary = "添加行政区划" )
    @Parameters({
            @Parameter(name = "provinceCrForm" , description = "实体对象" , required = true)
    })
    @SaCheckPermission("system.area" )
    @PostMapping
    public ActionResult create(@RequestBody @Valid ProvinceCrForm provinceCrForm) {
        ProvinceEntity entity = JsonUtil.getJsonToBean(provinceCrForm, ProvinceEntity.class);
        if (provinceService.isExistByEnCode(provinceCrForm.getEnCode(), entity.getId())) {
            return ActionResult.fail("区域编码不能重复" );
        }
        if (StringUtil.isEmpty(provinceCrForm.getParentId())) {
            entity.setParentId("-1" );
        }
        if (entity.getParentId().equals("-1" )) {
            entity.setType("1" );
        } else {
            ProvinceEntity info = provinceService.getInfo(provinceCrForm.getParentId());
            int i = Integer.valueOf(info.getType()) + 1;
            entity.setType(String.valueOf(i));
        }
        provinceService.create(entity);
        return ActionResult.success("新建成功" );
    }

    /**
     * 更新
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "修改行政区划" )
    @Parameters({
            @Parameter(name = "id" , description = "主键值" , required = true),
            @Parameter(name = "provinceUpForm" , description = "实体对象" , required = true)
    })
    @SaCheckPermission("system.area" )
    @PutMapping("/{id}" )
    public ActionResult update(@PathVariable("id" ) String id, @RequestBody @Valid ProvinceUpForm provinceUpForm) {
        ProvinceEntity entity = JsonUtil.getJsonToBean(provinceUpForm, ProvinceEntity.class);
        if (provinceService.isExistByEnCode(provinceUpForm.getEnCode(), id)) {
            return ActionResult.fail("区域编码不能重复" );
        }
        boolean flag = provinceService.update(id, entity);
        if (!flag) {
            return ActionResult.fail("更新失败，数据不存在" );
        }
        return ActionResult.success("更新成功" );
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除" )
    @Parameters({
            @Parameter(name = "id" , description = "主键值" , required = true)
    })
    @SaCheckPermission("system.area" )
    @DeleteMapping("/{id}" )
    public ActionResult delete(@PathVariable("id" ) String id) {
        if (provinceService.getList(id).size() == 0) {
            ProvinceEntity entity = provinceService.getInfo(id);
            if (entity != null) {
                provinceService.delete(entity);
                return ActionResult.success("删除成功" );
            }
            return ActionResult.fail("删除失败，数据不存在" );
        } else {
            return ActionResult.fail("删除失败，当前有子节点数据" );
        }
    }

    /**
     * 更新行政区划状态
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "更新行政区划状态" )
    @Parameters({
            @Parameter(name = "id" , description = "主键值" , required = true)
    })
    @SaCheckPermission("system.area" )
    @PutMapping("/{id}/Actions/State" )
    public ActionResult upState(@PathVariable("id" ) String id) {
        ProvinceEntity entity = provinceService.getInfo(id);
        if (entity.getEnabledMark() == null || "1".equals(String.valueOf(entity.getEnabledMark()))) {
            entity.setEnabledMark(0);
        } else {
            entity.setEnabledMark(1);
        }
        boolean flag = provinceService.update(id, entity);
        if (!flag) {
            return ActionResult.fail("更新失败，数据不存在" );
        }
        return ActionResult.success("更新成功" );
    }

    /**
     * 行政区划id转名称
     *
     * @param model 二维数组
     * @return ignore
     */
    @Operation(summary = "行政区划id转名称" )
    @Parameters({
            @Parameter(name = "model" , description = "二维数组" , required = true)
    })
    @PostMapping("/GetAreaByIds" )
    public ActionResult<List<List<String>>> getAreaByIds(@RequestBody AreaModel model) {
        // 返回给前端的list
        List<List<String>> list = new LinkedList<>();
        for (List<String> idList : model.getIdsList()) {
            List<ProvinceEntity> proList = provinceService.getProList(idList);
            List<String> collect = proList.stream().map(ProvinceEntity::getFullName).collect(Collectors.toList());
            list.add(collect);
        }
        return ActionResult.success(list);
    }


    @Operation(summary = "获取行政区划下拉框数据-新" )
    @Parameters({
            @Parameter(name = "provinceSelectModel" , description = "省市区下拉参数模型" )
    })
    @PostMapping("/SelectorNew" )
    public ActionResult<ListVO<ProvinceSelectListVO>> selectListNew(@RequestBody ProvinceSelectModel provinceSelectModel) {
        List<ProvinceEntity> data = provinceService.getList(provinceSelectModel.getPid());
        data = data.stream().filter(t -> t.getEnabledMark() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(provinceSelectModel.getIds())) {
            List<List<String>> ids = provinceSelectModel.getIds();
            List<String> allId=new ArrayList<>();
            for (List<String> arr : ids) {
                allId.addAll(arr);
            }
            data=data.stream().filter(t -> allId.contains(t.getId())).collect(Collectors.toList());
        }
        List<ProvinceSelectListVO> treeList = JsonUtil.getJsonToList(data, ProvinceSelectListVO.class);
        int i = 0;
        for (ProvinceSelectListVO entity : treeList) {
            entity.setIsLeaf(false);
            treeList.set(i, entity);
            i++;
        }
        ListVO<ProvinceSelectListVO> vo = new ListVO<>();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 获取行政区划列表
     *
     * @param id
     * @return
     */
    
    @GetMapping("/getList/{id}" )
    public List<ProvinceEntity> getList(@PathVariable("id" ) String id) {
        List<ProvinceEntity> list = provinceService.getList(id);
        return list;
    }

    
    @PostMapping("/getByIdList" )
    public List<ProvinceEntity> getByIdList(@RequestBody List<String> ids) {
        List<ProvinceEntity> proList = provinceService.getProList(ids);
        return proList;
    }

    
    @GetMapping("/getAllProList" )
    public List<ProvinceEntity> getAllProList() {
        List<ProvinceEntity> proList = provinceService.getAllProList();
        return proList;
    }

    
    public List<ProvinceEntity> getProListBytype(String type) {
        return provinceService.getProListBytype(type);
    }

    
    @PostMapping("/getProvinceByParent" )
    public ProvinceEntity getProListBytype(@RequestParam("id" ) String id, @RequestBody List<String> parentIds) {
        return provinceService.getInfo(id, parentIds);
    }


}
