package jnpf.base.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import jnpf.base.entity.PortalManageEntity;
import jnpf.base.model.portalManage.PortalManagePage;
import jnpf.base.model.portalManage.PortalManagePageDO;
import jnpf.base.model.portalManage.PortalManagePrimary;
import jnpf.base.model.portalManage.PortalManageVO;

import java.util.List;

/**
 * <p>
 * 门户管理 服务类
 * </p>
 *
 * @author YanYu
 * @since 2023-02-16
 */
public interface PortalManageService extends SuperService<PortalManageEntity> {

    void checkCreUp(PortalManageEntity portalManageEntity) throws Exception;

    PortalManageVO convertVO(PortalManageEntity entity);

    List<PortalManageVO> getList(PortalManagePrimary primary);

    PageDTO<PortalManagePageDO> getPage(PortalManagePage portalPagination);

    List<PortalManagePageDO> getSelectList(PortalManagePage pmPage);

    List<PortalManagePageDO> selectPortalBySystemIds(List<String> systemIds, List<String> collect);

    void createBatch(List<PortalManagePrimary> primaryLit) throws Exception;

    List<PortalManageVO> getListByEnable(PortalManagePrimary primary);
}
