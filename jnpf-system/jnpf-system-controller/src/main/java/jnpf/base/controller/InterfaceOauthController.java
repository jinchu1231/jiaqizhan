package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.entity.DataInterfaceLogEntity;
import jnpf.base.entity.DataInterfaceUserEntity;
import jnpf.base.entity.InterfaceOauthEntity;
import jnpf.base.model.InterfaceOauth.IdentDataInterfaceLogVO;
import jnpf.base.model.InterfaceOauth.IdentInterfaceListModel;
import jnpf.base.model.InterfaceOauth.InterfaceIdentForm;
import jnpf.base.model.InterfaceOauth.InterfaceIdentListVo;
import jnpf.base.model.InterfaceOauth.InterfaceIdentVo;
import jnpf.base.model.InterfaceOauth.InterfaceUserForm;
import jnpf.base.model.InterfaceOauth.InterfaceUserVo;
import jnpf.base.model.InterfaceOauth.PaginationIntrfaceLog;
import jnpf.base.model.InterfaceOauth.PaginationOauth;
import jnpf.base.model.datainterface.DataInterfaceVo;
import jnpf.base.service.DataInterfaceLogService;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DataInterfaceUserService;
import jnpf.base.service.InterfaceOauthService;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * 接口认证控制器
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/8
 */
@Tag(name = "接口认证", description = "interfaceoauth")
@RestController
@RequestMapping(value = "/api/system/InterfaceOauth")
public class InterfaceOauthController extends SuperController<InterfaceOauthService, InterfaceOauthEntity> {
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private DataInterfaceLogService dataInterfaceLogService;

    @Autowired
    private InterfaceOauthService interfaceOauthService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private DataInterfaceUserService dataInterfaceUserService;


    /**
     * 获取接口认证列表(分页)
     *
     * @param pagination 分页参数
     * @return ignore
     */
    @Operation(summary = "获取接口认证列表(分页)")
    @SaCheckPermission("systemData.interfaceAuth")
    @GetMapping
    public ActionResult<PageListVO<InterfaceIdentListVo>> getList(PaginationOauth pagination) {
        List<InterfaceOauthEntity> data = interfaceOauthService.getList(pagination);
        List<InterfaceIdentListVo> jsonToList = JsonUtil.getJsonToList(data, InterfaceIdentListVo.class);
        jsonToList.forEach(item -> {
            if (StringUtil.isNotEmpty(userProvider.get().getTenantId())) {
                item.setTenantId(userProvider.get().getTenantId());
            }
            if (item.getCreatorUserId() != null) {
                item.setCreatorUser(userService.getInfo(item.getCreatorUserId()).getRealName());
            }
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(jsonToList, paginationVO);
    }

    /**
     * 添加接口认证
     *
     * @param interfaceIdentForm 添加接口认证模型
     * @return ignore
     */
    @Operation(summary = "添加接口认证")
    @Parameter(name = "interfaceIdentForm", description = "添加接口认证模型", required = true)
    @SaCheckPermission("systemData.interfaceAuth")
    @PostMapping
    public ActionResult create(@RequestBody @Valid InterfaceIdentForm interfaceIdentForm) {
        InterfaceOauthEntity entity = JsonUtil.getJsonToBean(interfaceIdentForm, InterfaceOauthEntity.class);
        if (interfaceOauthService.isExistByAppName(entity.getAppName(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (interfaceOauthService.isExistByAppId(entity.getAppId(), entity.getId())) {
            return ActionResult.fail("内容不能重复");
        }
        interfaceOauthService.create(entity);


        return ActionResult.success("接口认证创建成功");
    }


    /**
     * 修改接口认证
     *
     * @param interfaceIdentForm 添加接口认证模型
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "修改接口认证")
    @Parameters({
            @Parameter(name = "interfaceIdentForm", description = "添加接口认证模型", required = true),
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.interfaceAuth")
    @PutMapping("/{id}")
    public ActionResult update(@RequestBody @Valid InterfaceIdentForm interfaceIdentForm, @PathVariable("id") String id) throws DataException {
        InterfaceOauthEntity entity = JsonUtil.getJsonToBean(interfaceIdentForm, InterfaceOauthEntity.class);
        if (interfaceOauthService.isExistByAppName(entity.getAppName(), id)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (interfaceOauthService.isExistByAppId(entity.getAppId(), id)) {
            return ActionResult.fail("内容不能重复");
        }
        boolean flag = interfaceOauthService.update(entity, id);
        if (flag == false) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除接口认证
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除接口")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.interfaceAuth")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable String id) {
        InterfaceOauthEntity entity = interfaceOauthService.getInfo(id);
        if (entity != null) {
            interfaceOauthService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 获取秘钥
     *
     * @return
     */
    @Operation(summary = "获取接口认证密钥")
    @SaCheckPermission("systemData.interfaceAuth")
    @GetMapping("/getAppSecret")
    public ActionResult getAppSecret() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return ActionResult.success("获取成功", uuid);
    }


    /**
     * 保存綁定认证接口
     *
     * @param identInterfaceListModel 授权接口列表模型
     * @return
     */
    @Operation(summary = "保存綁定认证接口")
    @Parameters({
            @Parameter(name = "identInterfaceListModel", description = "授权接口列表模型", required = true)
    })
    @SaCheckPermission("systemData.interfaceAuth")
    @PostMapping("/saveInterfaceList")
    public ActionResult getInterfaceList(@RequestBody IdentInterfaceListModel identInterfaceListModel) {
        InterfaceOauthEntity entity = new InterfaceOauthEntity();
        entity.setId(identInterfaceListModel.getInterfaceIdentId());
        entity.setDataInterfaceIds(identInterfaceListModel.getDataInterfaceIds());
        boolean b = interfaceOauthService.updateById(entity);
        if (b) {
            return ActionResult.success(MsgCode.SU002.get());
        }
        return ActionResult.success(MsgCode.FA101.get());
    }

    /**
     * 获取接口授权绑定接口列表
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取认证基础信息及接口列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.interfaceAuth")
    @GetMapping("/{id}")
    public ActionResult getInterfaceList(@PathVariable("id") String id) {
        InterfaceOauthEntity entity = interfaceOauthService.getInfo(id);
        InterfaceIdentVo bean = JsonUtil.getJsonToBean(entity, InterfaceIdentVo.class);
        if (StringUtils.isNotEmpty(bean.getDataInterfaceIds())) {
            List<DataInterfaceVo> listDataInterfaceVo = new ArrayList<>();
            List<DataInterfaceEntity> list = dataInterfaceService.getList(false);
            list.forEach(item -> {
                if (bean.getDataInterfaceIds().contains(item.getId())) {
                    DataInterfaceVo dataInterfaceVo = JsonUtil.getJsonToBean(item, DataInterfaceVo.class);
                    listDataInterfaceVo.add(dataInterfaceVo);
                }
            });
            bean.setList(listDataInterfaceVo);
        }

        //添加授权用户信息
        List<InterfaceUserVo> listIuv =new ArrayList<>();
        List<DataInterfaceUserEntity> select = dataInterfaceUserService.select(id);
        for(DataInterfaceUserEntity diue:select){
            String userId = diue.getUserId();
            UserEntity info = userService.getInfo(userId);
            InterfaceUserVo iuv=new InterfaceUserVo();
            iuv.setUserId(userId);
            iuv.setUserKey(diue.getUserKey());
            iuv.setUserName(info.getRealName()+"/"+info.getAccount());
            listIuv.add(iuv);
        }
        bean.setUserList(listIuv);
        return ActionResult.success("获取成功", bean);
    }

    /**
     * 获取日志列表
     *
     * @param id 主键
     * @param paginationIntrfaceLog 分页参数
     * @return
     */
    @Operation(summary = "获取日志列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.interfaceAuth")
    @GetMapping("/dataInterfaceLog/{id}")
    public ActionResult<PageListVO<IdentDataInterfaceLogVO>> getInterfaceList(@PathVariable("id") String id,PaginationIntrfaceLog paginationIntrfaceLog) {
        InterfaceOauthEntity entity = interfaceOauthService.getInfo(id);
        List<IdentDataInterfaceLogVO> voList = null;
        PaginationVO vo = null;
        if (entity!=null&&StringUtils.isNotEmpty(entity.getDataInterfaceIds())) {
            String dataInterfaceIds = entity.getDataInterfaceIds();
            String[] split = dataInterfaceIds.split(",");
            List<String> list = Arrays.asList(split);
            List<DataInterfaceLogEntity> listByIds = dataInterfaceLogService.getListByIds(entity.getAppId(),list, paginationIntrfaceLog);
            voList = JsonUtil.getJsonToList(listByIds, IdentDataInterfaceLogVO.class);
            List<DataInterfaceEntity> listDataInt = dataInterfaceService.getList(false);
            for (IdentDataInterfaceLogVO invo : voList) {
                if (StringUtil.isNotEmpty(userProvider.get().getTenantId())) {
                    invo.setTenantId(userProvider.get().getTenantId());
                }
                //绑定用户
                UserEntity userEntity = userService.getInfo(invo.getUserId());
                if (userEntity != null) {
                    invo.setUserId(userEntity.getRealName() + "/" + userEntity.getAccount());
                }
                //绑定接口基础数据
                listDataInt.forEach(item -> {
                    if (invo.getInvokId().contains(item.getId())) {
                        DataInterfaceVo dataInterfaceVo = JsonUtil.getJsonToBean(item, DataInterfaceVo.class);
                        invo.setFullName(dataInterfaceVo.getFullName());
                        invo.setEnCode(dataInterfaceVo.getEnCode());
                    }
                });
            }
            vo = JsonUtil.getJsonToBean(paginationIntrfaceLog, PaginationVO.class);

        }
        return ActionResult.page(voList, vo);
    }

    @Operation(summary = "授权用户")
    @SaCheckPermission("systemData.interfaceAuth")
    @PostMapping("/SaveUserList")
    public ActionResult saveUserList(@RequestBody InterfaceUserForm interfaceUserForm) {
        dataInterfaceUserService.saveUserList(interfaceUserForm);
        return ActionResult.success(MsgCode.SU002.get());
    }
}
