package jnpf.base.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import jnpf.base.entity.PortalManageEntity;
import jnpf.base.model.portalManage.PortalManagePage;
import jnpf.base.model.portalManage.PortalManagePageDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 虎门管理
 *
 * @author JNPF开发平台组 YanYu
 * @version V3.4.6
 * @copyright 引迈信息技术有限公司
 * @date 2023.02.17
 */
public interface PortalManageMapper extends SuperMapper<PortalManageEntity> {

    @Select("SELECT F_Full_Name FROM base_portal WHERE F_Id = #{portalId}")
    String getPortalFullName(String portalId);

    @Select("SELECT F_Category FROM base_portal WHERE F_Id = #{portalId}")
    String getPortalCategoryId(String portalId);

    PageDTO<PortalManagePageDO> selectPortalManageDoPage(PageDTO<PortalManagePageDO> page, @Param("pmPage") PortalManagePage pmPage);

    List<PortalManagePageDO> selectPortalManageDoList(@Param("pmPage") PortalManagePage pmPage);

    List<PortalManagePageDO> selectPortalBySystemIds(@Param("systemIds") List<String> systemIds, @Param("collect") List<String> collect);
}
