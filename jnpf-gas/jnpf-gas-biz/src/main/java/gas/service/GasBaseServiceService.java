package gas.service;

import gas.entity.*;
import jnpf.base.service.SuperService;
import gas.model.gasbaseservice.*;
import java.util.*;

/**
 * 服务区设置
 * 版本： V3.6
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2024-05-21
 */
public interface GasBaseServiceService extends SuperService<GasBaseServiceEntity> {
    List<GasBaseServiceEntity> getList(GasBaseServicePagination gasBaseServicePagination);

    List<GasBaseServiceEntity> getTypeList(GasBaseServicePagination gasBaseServicePagination,String dataType);

    GasBaseServiceEntity getInfo(String id);

    void delete(GasBaseServiceEntity entity);

    void create(GasBaseServiceEntity entity);

    boolean update(String id, GasBaseServiceEntity entity);

	String checkForm(GasBaseServiceForm form,int i);

    void saveOrUpdate(GasBaseServiceForm gasBaseServiceForm,String id, boolean isSave) throws Exception;
}
