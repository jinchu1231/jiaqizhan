package gas.service;

import gas.entity.*;
import jnpf.base.service.SuperService;
import gas.model.gasbaserroad.*;
import java.util.*;

/**
 * 路段设置
 * 版本： V3.6
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2024-05-21
 */
public interface GasBaserRoadService extends SuperService<GasBaserRoadEntity> {
    List<GasBaserRoadEntity> getList(GasBaserRoadPagination gasBaserRoadPagination);

    List<GasBaserRoadEntity> getTypeList(GasBaserRoadPagination gasBaserRoadPagination,String dataType);

    GasBaserRoadEntity getInfo(String id);

    void delete(GasBaserRoadEntity entity);

    void create(GasBaserRoadEntity entity);

    boolean update(String id, GasBaserRoadEntity entity);

	String checkForm(GasBaserRoadForm form,int i);

    void saveOrUpdate(GasBaserRoadForm gasBaserRoadForm,String id, boolean isSave) throws Exception;
}
