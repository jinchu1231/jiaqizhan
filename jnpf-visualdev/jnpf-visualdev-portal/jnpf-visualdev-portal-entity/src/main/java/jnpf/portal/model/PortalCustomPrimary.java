package jnpf.portal.model;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.MyBatisPrimaryBase;
import jnpf.base.UserInfo;
import jnpf.portal.constant.PortalConst;
import jnpf.portal.entity.PortalDataEntity;
import jnpf.util.UserProvider;
import jnpf.util.context.SpringContext;
import lombok.Data;

/**
 * 类功能
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.8
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-04-21
 */
@Data
public class PortalCustomPrimary extends MyBatisPrimaryBase<PortalDataEntity> {

    /** 平台 */
    private String platform = PortalConst.WEB;
    /** 门户ID */
    private String portalId;
    /** 系统ID */
    private String systemId;
    /** 用户ID */
    private String creatorId;
    /** 类型（mod：模型、custom：自定义） */
    private String type = PortalConst.CUSTOM;

    public PortalCustomPrimary(String platform, String portalId, String systemId, String userId) {
        if(platform != null) this.platform = platform;
        this.portalId = portalId;
        this.systemId = systemId;
        this.creatorId = userId;
    }

    public PortalCustomPrimary(String platform, String portalId){
        if(platform != null) this.platform = platform;
        this.portalId = portalId;
        UserInfo userInfo = SpringContext.getBean(UserProvider.class).get();
        this.systemId = userInfo.getSystemId();
        this.creatorId = userInfo.getUserId();
    }

    public QueryWrapper<PortalDataEntity> getQuery(){
        queryWrapper.lambda().eq(PortalDataEntity::getType, type);
        if(this.platform != null) queryWrapper.lambda().eq(PortalDataEntity::getPlatform, platform);
        if(this.portalId != null) queryWrapper.lambda().eq(PortalDataEntity::getPortalId, portalId);
        if(this.systemId != null) queryWrapper.lambda().eq(PortalDataEntity::getSystemId, systemId);
        if(this.creatorId != null) queryWrapper.lambda().eq(PortalDataEntity::getCreatorUserId, creatorId);
        return queryWrapper;
    }

}
