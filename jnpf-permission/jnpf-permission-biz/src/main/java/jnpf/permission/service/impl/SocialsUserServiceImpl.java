package jnpf.permission.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.UserInfo;
import jnpf.base.service.SuperServiceImpl;
import jnpf.config.ConfigValueUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.permission.entity.SocialsUserEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.mapper.SocialsUserMapper;
import jnpf.permission.model.socails.SocialsUserVo;
import jnpf.permission.service.SocialsUserService;
import jnpf.permission.service.UserService;
import jnpf.permission.util.socials.AuthSocialsUtil;
import jnpf.permission.util.socials.SocialsAuthEnum;
import jnpf.permission.util.socials.SocialsConfig;
import jnpf.util.JsonUtil;
import jnpf.util.wxutil.HttpUtil;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/7/14 9:33:16
 */
@Service
public class SocialsUserServiceImpl extends SuperServiceImpl<SocialsUserMapper, SocialsUserEntity> implements SocialsUserService {

    @Autowired
    private SocialsConfig socialsConfig;
    @Autowired
    private AuthSocialsUtil authSocialsUtil;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private SocialsUserService socialsUserService;

    @Override
    public List<SocialsUserEntity> getListByUserId(String userId) {
        QueryWrapper<SocialsUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SocialsUserEntity::getUserId,userId);
        return this.list(queryWrapper);
    }

    @Override
    public List<SocialsUserEntity> getUserIfnoBySocialIdAndType(String socialId, String socialType) {
        QueryWrapper<SocialsUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SocialsUserEntity::getSocialId,socialId);
        queryWrapper.lambda().eq(SocialsUserEntity::getSocialType,socialType);
        return this.list(queryWrapper);
    }

    @Override
    public List<SocialsUserEntity> getListByUserIdAndSource(String userId, String socialType) {
        QueryWrapper<SocialsUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SocialsUserEntity::getUserId,userId);
        queryWrapper.lambda().eq(SocialsUserEntity::getSocialType,socialType);
        return this.list(queryWrapper);
    }

    @Override
    public SocialsUserEntity getInfoBySocialId(String socialId,String socialType){
        QueryWrapper<SocialsUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SocialsUserEntity::getSocialId,socialId);
        queryWrapper.lambda().eq(SocialsUserEntity::getSocialType,socialType);
        return this.getOne(queryWrapper);
    }
    @Override
    public List<SocialsUserVo> getLoginList(String ticket) {
        if (!socialsConfig.isSocialsEnabled()) return null;
        List<Map<String, Object>> platformInfos = SocialsAuthEnum.getPlatformInfos();
        String s = JSONArray.toJSONString(platformInfos);
        List<SocialsUserVo> socialsUserVos = JsonUtil.getJsonToList(s, SocialsUserVo.class);
        List<SocialsConfig.Config> config = socialsConfig.getConfig();
        List<SocialsUserVo> res = new ArrayList<>();
        config.stream().forEach(item -> {
            socialsUserVos.stream().forEach(item2 -> {
                if (item2.getEnname().toLowerCase().equals(item.getProvider())) {
                    AuthRequest authRequest = authSocialsUtil.getAuthRequest(item2.getEnname(), null, true, ticket, null);
                    String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
                    item2.setRenderUrl(authorizeUrl);
                    res.add(item2);
                }
            });
        });
        return res;
    }

    @Override
    public void loginAutoBinding(@RequestParam("socialType") String socialType,
                                 @RequestParam("socialUnionid") String socialUnionid,
                                 @RequestParam("socialName") String socialName,
                                 @RequestParam("userId") String userId,
                                 @RequestParam(value = "tenantId", required = false) String tenantId ) {
        if ("wechat_applets".equals(socialType)) {
            socialType = "wechat_open";
        }
        //租户开启时-切换租户库
        if (configValueUtil.isMultiTenancy()) {
            setTenantData(tenantId, new UserInfo());
        }
        List<SocialsUserEntity> list = socialsUserService.getListByUserIdAndSource(userId, socialType);
        if(CollectionUtil.isNotEmpty(list)){//账号已绑定该第三方其他账号，则不绑定
            return;
        }
        SocialsUserEntity socialsUserEntity = new SocialsUserEntity();
        socialsUserEntity.setUserId(userId);
        socialsUserEntity.setSocialType(socialType);
        socialsUserEntity.setSocialName(socialName);
        socialsUserEntity.setSocialId(socialUnionid);
        socialsUserEntity.setCreatorTime(new Date());
        boolean save = socialsUserService.save(socialsUserEntity);
        //租户开启时-添加租户库绑定数据
        if (configValueUtil.isMultiTenancy() && save) {
            JSONObject params = (JSONObject) JSONObject.toJSON(socialsUserEntity);
            UserEntity info = userService.getInfo(userId);
            params.put("tenantId", tenantId);
            params.put("account", info.getAccount());
            params.put("accountName", info.getRealName() + "/" + info.getAccount());
            JSONObject object = HttpUtil.httpRequest(configValueUtil.getMultiTenancyUrl() + "socials", "POST", params.toJSONString());

        }
    }

    /**
     * 设置租户库
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/9/8
     */
    private boolean setTenantData(String tenantId, UserInfo userInfo) {
        try{
            TenantDataSourceUtil.switchTenant(tenantId);
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
