package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.model.dblink.DbLinkBaseForm;
import jnpf.base.model.dblink.DbLinkCreUpForm;
import jnpf.base.model.dblink.DbLinkInfoVO;
import jnpf.base.model.dblink.DbLinkListVO;
import jnpf.base.model.dblink.DbLinkModel;
import jnpf.base.model.dblink.DbLinkSelectorListVO;
import jnpf.base.model.dblink.PaginationDbLink;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.XSSEscape;
import jnpf.util.enums.DictionaryDataEnum;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据连接
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "数据连接", description = "DataSource")
@RestController
@RequestMapping("/api/system/DataSource")
public class DbLinkController extends SuperController<DbLinkService, DbLinkEntity> {

    @Autowired
    private DbLinkService dblinkService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private ConfigValueUtil configValueUtil;


    /**
     * 列表
     *
     * @param type 类型
     * @return
     */
    @GetMapping("/Selector")
    @Parameters({
            @Parameter(name = "type", description = "类型")
    })
    @Operation(summary = "获取数据连接下拉框列表")
    public ActionResult<ListVO<DbLinkSelectorListVO>> selectorList(@RequestParam(value = "type", required = false) String type) {
        List<DbLinkModel> modelAll = new LinkedList<>();
        //将主节点添加到容器当中=======
        List<DbLinkEntity> data = dblinkService.getList();
        //连接中类型所对应的字典集合
        List<DictionaryDataEntity> dataEntityList = dictionaryDataService.getList(dictionaryTypeService.getInfoByEnCode(DictionaryDataEnum.SYSTEM_DBLINK.getDictionaryTypeId()).getId());
        for (DictionaryDataEntity entity : dataEntityList) {
            DbLinkModel model = new DbLinkModel();
            model.setFullName(entity.getFullName());
            model.setParentId("-1");
            model.setId(entity.getId());
            Long num = data.stream().filter(t -> t.getDbType().equals(entity.getEnCode())).count();
            model.setNum(num);
            if (num > 0) {
                modelAll.add(model);
            }
        }
        //字典里存在则添加到容器当中=======
        for (DbLinkEntity entity : data) {
            DbLinkModel model = JsonUtil.getJsonToBean(entity, DbLinkModel.class);
            DictionaryDataEntity dataEntity = dataEntityList.stream().filter(t -> t.getEnCode().equals(entity.getDbType())).findFirst().orElse(null);
            if (dataEntity != null) {
                model.setParentId(dataEntity.getId());
                modelAll.add(model);
            }
        }
        List<SumTree<DbLinkModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<DbLinkSelectorListVO> list = new ArrayList<>();
        // type不为空时不返回默认库
        if(type == null){
            DbLinkSelectorListVO defaultDb = new DbLinkSelectorListVO();
            defaultDb.setFullName("");
            DbLinkListVO dbLink = new DbLinkListVO();
            dbLink.setFullName("默认数据库");
            dbLink.setId("0");
            dbLink.setDbType(dataSourceUtil.getDbType());
            defaultDb.setChildren(Collections.singletonList(dbLink));
            list.add(defaultDb);
        }
        list.addAll(JsonUtil.getJsonToList(trees, DbLinkSelectorListVO.class));
        ListVO<DbLinkSelectorListVO> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 2:列表
     *
     * @param page 关键字
     * @return ignore
     */
    @GetMapping
    @Operation(summary = "获取数据连接列表")
    public ActionResult<PageListVO<DbLinkListVO>> getList(PaginationDbLink page) {
        List<DbLinkEntity> data = dblinkService.getList(page);
        List<String> userId = data.stream().map(t -> t.getCreatorUserId()).collect(Collectors.toList());
        List<String> lastUserId = data.stream().map(t -> t.getLastModifyUserId()).collect(Collectors.toList());
        List<UserEntity> userEntities = userService.getUserName(userId);
        List<UserEntity> lastUserIdEntities = userService.getUserName(lastUserId);
        List<DictionaryDataEntity> typeList = dictionaryDataService.getList(dictionaryTypeService.getInfoByEnCode(DictionaryDataEnum.SYSTEM_DBLINK.getDictionaryTypeId()).getId());
        List<DbLinkListVO> jsonToList = JsonUtil.getJsonToList(data, DbLinkListVO.class);
        for (DbLinkListVO vo : jsonToList) {
            //存在类型的字典对象
            DictionaryDataEntity dataEntity = typeList.stream().filter(t -> t.getEnCode().equals(vo.getDbType())).findFirst().orElse(null);
            if (dataEntity != null) {
                vo.setDbType(dataEntity.getFullName());
            } else {
                vo.setDbType("");
            }
            //创建者
            UserEntity creatorUser = userEntities.stream().filter(t -> t.getId().equals(vo.getCreatorUser())).findFirst().orElse(null);
            vo.setCreatorUser(creatorUser != null ? creatorUser.getRealName() + "/" + creatorUser.getAccount() : vo.getCreatorUser());
            //修改人
            UserEntity lastModifyUser = lastUserIdEntities.stream().filter(t -> t.getId().equals(vo.getLastModifyUser())).findFirst().orElse(null);
            vo.setLastModifyUser(lastModifyUser != null ? lastModifyUser.getRealName() + "/" + lastModifyUser.getAccount() : vo.getLastModifyUser());
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(page, PaginationVO.class);
        return ActionResult.page(jsonToList , paginationVO);
    }

    /**
     * 3:单条数据连接
     *
     * @param id 主键
     * @return ignore
     * @throws DataException ignore
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取数据连接")
    @Parameters({
            @Parameter(name = "id", description = "主键")
    })
    @SaCheckPermission("systemData.dataSource")
    public ActionResult<DbLinkInfoVO> get(@PathVariable("id") String id) throws DataException {
        return ActionResult.success(new DbLinkInfoVO().getDbLinkInfoVO(dblinkService.getInfo(XSSEscape.escape(id))));
    }

    /**
     * 4:新建数据连接
     *
     * @param dbLinkCreUpForm 新建数据连接表单对象
     * @return ignore
     */
    @PostMapping
    @Operation(summary = "添加数据连接")
    @Parameters({
            @Parameter(name = "dbLinkCreUpForm", description = "新建数据连接表单对象", required = true)
    })
    @SaCheckPermission("systemData.dataSource")
    public ActionResult<String> create(@RequestBody @Valid DbLinkCreUpForm dbLinkCreUpForm) {
        DbLinkEntity entity = dbLinkCreUpForm.getDbLinkEntity(dbLinkCreUpForm);
        if (dblinkService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        dblinkService.create(entity);
        return ActionResult.success("创建成功");
    }

    /**
     * 5:更新数据连接
     *
     * @param id              主键
     * @param dbLinkCreUpForm dto实体
     * @return ignore
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改数据连接")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "dbLinkCreUpForm", description = "新建数据连接表单对象", required = true)
    })
    @SaCheckPermission("systemData.dataSource")
    public ActionResult<String> update(@PathVariable("id") String id, @RequestBody @Valid DbLinkCreUpForm dbLinkCreUpForm) {
        id = XSSEscape.escape(id);
        DbLinkEntity entity = dbLinkCreUpForm.getDbLinkEntity(dbLinkCreUpForm);
        if (dblinkService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (!dblinkService.update(id, entity)) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 6:删除
     *
     * @param id 主键
     * @return ignore
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据连接")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.dataSource")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        DbLinkEntity entity = dblinkService.getInfo(id);
        if (entity != null) {
            dblinkService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 7:测试连接
     *
     * @param dbLinkBaseForm 数据连接参数
     * @return ignore
     * @throws DataException ignore
     */
    @PostMapping("/Actions/Test")
    @Operation(summary = "测试连接")
    @Parameters({
            @Parameter(name = "dbLinkBaseForm", description = "数据连接参数", required = true)
    })
    @SaCheckPermission("systemData.dataSource")
    public ActionResult<String> test(@RequestBody DbLinkBaseForm dbLinkBaseForm) throws Exception {
        boolean data = dblinkService.testDbConnection(dbLinkBaseForm.getDbLinkEntity(dbLinkBaseForm));
        if (data) {
            return ActionResult.success("连接成功");
        } else {
            return ActionResult.fail("连接失败");
        }
    }

    /**
     * 信息
     * @param id 主键
     * @return
     */
    
    @GetMapping("/{id}/info")
    public DbLinkEntity getInfo(@PathVariable("id") String id) {
        DbLinkEntity entity = dblinkService.getInfo(id);
        return entity;
    }

    
    @GetMapping("/info/{id}/{tenantId}")
    public Object getInfo(@PathVariable("id") String id, @PathVariable("tenantId") String tenantId) {
        // 判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            //切换成租户库
            try{
                TenantDataSourceUtil.switchTenant(tenantId);
            }catch (Exception e){
                return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
            }
        }
        return dblinkService.getInfo(id) != null ? dblinkService.getInfo(id) : JsonUtil.getJsonToBean(dataSourceUtil, DbLinkEntity.class);
    }

    
    @GetMapping("/infoByFullName")
    public DbLinkEntity getInfoByFullName(String fullName) {
        return dblinkService.getInfoByFullName(fullName);
    }

    
    @NoDataSourceBind
    @GetMapping("/getResource")
    public DbLinkEntity getResource(@RequestParam("dbLinkId") String dbLinkId, @RequestParam(name = "tenantId", required = false) String tenantId) throws Exception {
        // 判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        return dblinkService.getResource(dbLinkId);
    }

}
