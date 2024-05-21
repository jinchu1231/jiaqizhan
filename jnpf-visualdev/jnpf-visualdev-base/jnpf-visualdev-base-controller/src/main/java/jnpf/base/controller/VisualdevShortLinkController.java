package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.entity.VisualdevShortLinkEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.model.VisualWebTypeEnum;
import jnpf.base.model.shortLink.VisualdevShortLinkConfigVo;
import jnpf.base.model.shortLink.VisualdevShortLinkForm;
import jnpf.base.model.shortLink.VisualdevShortLinkModel;
import jnpf.base.model.shortLink.VisualdevShortLinkPwd;
import jnpf.base.model.shortLink.VisualdevShortLinkVo;
import jnpf.base.service.VisualdevReleaseService;
import jnpf.base.service.VisualdevService;
import jnpf.base.service.VisualdevShortLinkService;
import jnpf.base.util.VisualUtil;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.config.JnpfOauthConfig;
import jnpf.constant.MsgCode;
import jnpf.consts.DeviceType;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.exception.LoginException;
import jnpf.exception.WorkFlowException;
import jnpf.onlinedev.model.DataInfoVO;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.onlinedev.model.VisualdevModelDataCrForm;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualDevInfoService;
import jnpf.onlinedev.service.VisualDevListService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlineSwapDataUtils;
import jnpf.util.AuthUtil;
import jnpf.util.DesUtil;
import jnpf.util.JsonUtil;
import jnpf.util.Md5Util;
import jnpf.util.NoDataSourceBind;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import jnpf.util.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线开发表单外链Controller
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/12/30 11:33:17
 */
@Tag(name = "表单外链", description = "BaseShortLink")
@RestController
@RequestMapping("/api/visualdev/ShortLink")
public class VisualdevShortLinkController extends SuperController<VisualdevShortLinkService, VisualdevShortLinkEntity> {

    @Autowired
    private VisualdevShortLinkService visualdevShortLinkService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private JnpfOauthConfig oauthConfig;

    @Autowired
    private ConfigValueUtil configValueUtil;

    @Autowired
    protected AuthUtil authUtil;

    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevReleaseService visualdevReleaseService;
    @Autowired
    private VisualDevInfoService visualDevInfoService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;
    @Autowired
    private OnlineSwapDataUtils onlineSwapDataUtils;
    @Autowired
    private VisualDevListService visualDevListService;

    @Operation(summary = "获取外链信息" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @GetMapping("/{id}" )
    @SaCheckPermission("onlineDev.webDesign" )
    public ActionResult getInfo(@PathVariable("id" ) String id) {
        VisualdevShortLinkEntity info = visualdevShortLinkService.getById(id);
        VisualdevShortLinkVo vo;
        if (info != null) {
            vo = JsonUtil.getJsonToBean(info, VisualdevShortLinkVo.class);
            vo.setAlreadySave(true);
        } else {
            vo = new VisualdevShortLinkVo();
            vo.setId(id);
        }
        vo.setFormLink(geturl(id, "form" ));
        vo.setColumnLink(geturl(id, "list" ));
        return ActionResult.success(vo);
    }

    /**
     * 获取url
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/3/9
     */
    private String geturl(String id, String type) {
        String url = oauthConfig.getJnpfDomain() + "/api/visualdev/ShortLink/trigger/" + id + "?encryption=";
        UserInfo userInfo = userProvider.get();
        JSONObject obj = new JSONObject();
        obj.put("type" , type);
        if (configValueUtil.isMultiTenancy()) {
            obj.put("tenantId" , userInfo.getTenantId());
        }
        //参数加密
        String encryption = DesUtil.aesOrDecode(obj.toJSONString(), true,true);
        url += encryption;
        return url;
    }


    @Operation(summary = "修改外链信息" )
    @PutMapping("" )
    @SaCheckPermission("onlineDev.webDesign" )
    public ActionResult saveOrupdate(@RequestBody VisualdevShortLinkForm data) {
        VisualdevShortLinkEntity entity = JsonUtil.getJsonToBean(data, VisualdevShortLinkEntity.class);
        if(entity.getFormLink().contains(oauthConfig.getJnpfDomain())){
            entity.setFormLink(entity.getFormLink().replace(oauthConfig.getJnpfDomain(),""));
        }
        if(entity.getColumnLink().contains(oauthConfig.getJnpfDomain())){
            entity.setColumnLink(entity.getColumnLink().replace(oauthConfig.getJnpfDomain(),""));
        }
        VisualdevShortLinkEntity info = visualdevShortLinkService.getById(data.getId());
        UserInfo userInfo = userProvider.get();
        if (info != null) {
            entity.setLastModifyTime(new Date());
            entity.setLastModifyUserId(userInfo.getUserId());
        } else {
            entity.setCreatorTime(new Date());
            entity.setCreatorUserId(userInfo.getUserId());
        }

        String pcLink = "/formShortLink";
        String appLink ="/pages/formShortLink/index";
        entity.setRealPcLink(pcLink);
        entity.setRealAppLink(appLink);
        entity.setUserId(userInfo.getUserId());
        visualdevShortLinkService.saveOrUpdate(entity);
        return ActionResult.success(MsgCode.SU002.get());
    }

    /**
     * 参数解密切换数据源
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/3/9
     */
    private VisualdevShortLinkModel aesDecodeMatchDatabase(String encryption) throws LoginException {
        //参数解密
        String str = DesUtil.aesOrDecode(encryption, false,true);
        if (StringUtil.isEmpty(str)) {
            throw new LoginException("参数解析错误!" );
        }
        VisualdevShortLinkModel model = JsonUtil.getJsonToBean(str, VisualdevShortLinkModel.class);
        if (configValueUtil.isMultiTenancy()) {
            if (StringUtil.isNotEmpty(model.getTenantId())) {
                //切换成租户库
                TenantDataSourceUtil.switchTenant(model.getTenantId());
            } else {
                throw new LoginException("缺少租户信息!" );
            }
        }
        return model;
    }

    @NoDataSourceBind
    @Operation(summary = "外链请求入口" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @GetMapping("/trigger/{id}" )
    public ActionResult getlink(@PathVariable("id" ) String id,
                                @RequestParam(value = "encryption" ) String encryption,
                                HttpServletResponse response) throws LoginException, IOException {
        VisualdevShortLinkModel model = aesDecodeMatchDatabase(encryption);
        String link = "";
        VisualdevShortLinkEntity entity = visualdevShortLinkService.getById(id);
        DeviceType deviceType = UserProvider.getDeviceForAgent();
        if (entity != null) {
            if (DeviceType.PC.equals(deviceType)) {
                link = oauthConfig.getJnpfFrontDomain() + entity.getRealPcLink();
            } else {
                link =  oauthConfig.getJnpfAppDomain() + entity.getRealAppLink();
            }
        } else {
            return ActionResult.fail("无效链接" );
        }
        JSONObject obj = new JSONObject();
        obj.put("modelId" , id);
        obj.put("type" , model.getType());
        if (configValueUtil.isMultiTenancy()) {
            obj.put("tenantId" , model.getTenantId());
        }
        //新链接参数加密
        String encryptionNew = DesUtil.aesOrDecode(obj.toJSONString(), true,true);
        link += "?encryption=" + encryptionNew;
//        link += "&modelId=" + id;
        response.sendRedirect(link);
        return ActionResult.success(MsgCode.SU000.get());
    }

    @NoDataSourceBind
    @Operation(summary = "获取外链配置" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @GetMapping("/getConfig/{id}" )
    public ActionResult getConfig(@PathVariable("id" ) String id, @RequestParam("encryption" ) String encryption) throws LoginException {
        aesDecodeMatchDatabase(encryption);

        VisualdevShortLinkEntity info = visualdevShortLinkService.getById(id);
        VisualdevShortLinkConfigVo vo = JsonUtil.getJsonToBean(info, VisualdevShortLinkConfigVo.class);
        vo.setFormLink(geturl(id, "form" ));
        vo.setColumnLink(geturl(id, "list" ));
        return ActionResult.success(vo);
    }

    @NoDataSourceBind
    @Operation(summary = "密码验证" )
    @PostMapping("/checkPwd" )
    public ActionResult checkPwd(@RequestBody VisualdevShortLinkPwd form) throws LoginException {
        //参数解密
        VisualdevShortLinkModel model = aesDecodeMatchDatabase(form.getEncryption());

        VisualdevShortLinkEntity info = visualdevShortLinkService.getById(form.getId());
        boolean flag = false;
        if (OnlineDevData.STATE_ENABLE.equals(info.getFormPassUse()) && 0 == form.getType()) {
            if (Md5Util.getStringMd5(info.getFormPassword()).equals(form.getPassword())) {
                flag = true;
            }
        } else if (OnlineDevData.STATE_ENABLE.equals(info.getColumnPassUse()) && 1 == form.getType()) {
            if (Md5Util.getStringMd5(info.getColumnPassword()).equals(form.getPassword())) {
                flag = true;
            }
        }
        if (flag) {
            return ActionResult.success();
        }
        return ActionResult.fail("密码错误！" );
    }

    @NoDataSourceBind
    @Operation(summary = "获取列表表单配置JSON" )
    @GetMapping("/{modelId}/Config" )
    public ActionResult getData(@PathVariable("modelId" ) String modelId, @RequestParam(value = "type" , required = false) String type,
                                @RequestParam("encryption" ) String encryption) throws WorkFlowException, LoginException {
        aesDecodeMatchDatabase(encryption);
        VisualdevEntity entity;
        //线上版本
        if ("0".equals(type)) {
            entity = visualdevService.getInfo(modelId);
        } else {
            VisualdevReleaseEntity releaseEntity = visualdevReleaseService.getById(modelId);
            entity = JsonUtil.getJsonToBean(releaseEntity, VisualdevEntity.class);
        }
        if (entity == null) {
            return ActionResult.fail("未找到该功能表单" );
        }
        String s = VisualUtil.checkPublishVisualModel(entity, "预览" );
        if (s != null) {
            return ActionResult.fail(s);
        }
        DataInfoVO vo = JsonUtil.getJsonToBean(entity, DataInfoVO.class);
        return ActionResult.success(vo);
    }

    @NoDataSourceBind
    @Operation(summary = "外链数据列表" )
    @Parameters({
            @Parameter(name = "modelId" , description = "模板id" ),
    })
    @PostMapping("/{modelId}/ListLink" )
    public ActionResult ListLink(@PathVariable("modelId" ) String modelId, @RequestParam("encryption" ) String encryption,
                                 @RequestBody PaginationModel paginationModel) throws WorkFlowException, LoginException {
        aesDecodeMatchDatabase(encryption);

        VisualdevReleaseEntity visualdevEntity = visualdevReleaseService.getById(modelId);
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }
        List<Map<String, Object>> realList;
        if (VisualWebTypeEnum.DATA_VIEW.getType().equals(visualdevEntity.getWebType())) {//
            //数据视图的接口数据获取、
            ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            realList = onlineSwapDataUtils.getInterfaceData(visualdevEntity, paginationModel, columnDataModel);
        } else {
            realList = visualDevListService.getDataListLink(visualJsonModel, paginationModel);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationModel, PaginationVO.class);
        return ActionResult.page(realList, paginationVO);
    }

    @NoDataSourceBind
    @Operation(summary = "获取数据信息(带转换数据)" )
    @Parameters({
            @Parameter(name = "modelId" , description = "模板id" ),
            @Parameter(name = "id" , description = "数据id" ),
    })
    @GetMapping("/{modelId}/{id}/DataChange" )
    public ActionResult infoWithDataChange(@PathVariable("modelId" ) String modelId, @PathVariable("id" ) String id,
                                           @RequestParam("encryption" ) String encryption) throws DataException, ParseException, IOException, SQLException, LoginException {
        aesDecodeMatchDatabase(encryption);

        modelId = XSSEscape.escape(modelId);
        id = XSSEscape.escape(id);
        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        //有表
        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            VisualdevModelDataInfoVO vo = visualDevInfoService.getDetailsDataInfo(id, visualdevEntity);
            return ActionResult.success(vo);
        }
        //无表
        VisualdevModelDataInfoVO vo = visualdevModelDataService.infoDataChange(id, visualdevEntity);
        return ActionResult.success(vo);
    }

    //**********以下微服务和单体不同
    @NoDataSourceBind
    @Operation(summary = "添加数据" )
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "encryption", description = "加密参数"),
            @Parameter(name = "visualdevModelDataCrForm", description = "功能数据创建表单"),
    })
    @PostMapping("/{modelId}" )
    public ActionResult create(@PathVariable("modelId" ) String modelId, @RequestParam("encryption" ) String encryption,
                               @RequestBody VisualdevModelDataCrForm visualdevModelDataCrForm) throws WorkFlowException, LoginException {
        VisualdevShortLinkModel visualdevShortLinkModel = aesDecodeMatchDatabase(encryption);
        VisualdevShortLinkEntity info = visualdevShortLinkService.getById(modelId);
        if(1!=info.getFormUse()){
            return   ActionResult.fail("未开启表单外链！");
        }
        visualdevModelDataCrForm.setIsLink(true);
        ActionResult data = visualdevModelDataService.createData(modelId,visualdevShortLinkModel.getTenantId(), visualdevModelDataCrForm);
        return data;
    }
}
