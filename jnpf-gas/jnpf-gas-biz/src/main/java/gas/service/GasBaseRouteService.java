package gas.service;

import gas.entity.*;
import jnpf.base.service.SuperService;
import gas.model.gasbaseroute.*;
import java.util.*;

/**
 * 路线管理
 * 版本： V3.6
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2024-05-21
 */
public interface GasBaseRouteService extends SuperService<GasBaseRouteEntity> {
    List<GasBaseRouteEntity> getList(GasBaseRoutePagination gasBaseRoutePagination);

    List<GasBaseRouteEntity> getTypeList(GasBaseRoutePagination gasBaseRoutePagination,String dataType);

    GasBaseRouteEntity getInfo(String id);

    void delete(GasBaseRouteEntity entity);

    void create(GasBaseRouteEntity entity);

    boolean update(String id, GasBaseRouteEntity entity);

	String checkForm(GasBaseRouteForm form,int i);

    void saveOrUpdate(GasBaseRouteForm gasBaseRouteForm,String id, boolean isSave) throws Exception;
}
