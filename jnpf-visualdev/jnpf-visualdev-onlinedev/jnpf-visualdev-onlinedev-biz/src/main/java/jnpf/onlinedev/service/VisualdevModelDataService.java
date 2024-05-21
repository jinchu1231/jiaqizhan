package jnpf.onlinedev.service;


import jnpf.base.ActionResult;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.FormDataField;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.service.SuperService;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.DataModel;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.onlinedev.model.PaginationModelExport;
import jnpf.onlinedev.model.VisualdevModelDataCrForm;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * 0代码功能数据表
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-07-24 11:59
 */
public interface VisualdevModelDataService extends SuperService<VisualdevModelDataEntity> {

    /**
     * 获取表单主表属性下拉框
     * @return
     */
    List<FormDataField> fieldList(String id, Integer filterType);

    /**
     * 弹窗数据分页
     * @param visualdevEntity
     * @param paginationModel
     * @return
     */
    List<Map<String,Object>> getPageList(VisualdevEntity visualdevEntity, PaginationModel paginationModel);

    List<VisualdevModelDataEntity> getList(String modelId);

    VisualdevModelDataEntity getInfo(String id);

    VisualdevModelDataInfoVO infoDataChange(String id, VisualdevEntity visualdevEntity) throws IOException, ParseException, DataException, SQLException;

    void delete(VisualdevModelDataEntity entity);

    boolean tableDelete(String id, VisualDevJsonModel visualDevJsonModel) throws Exception;

    ActionResult tableDeleteMore(List<String> id, VisualDevJsonModel visualDevJsonModel) throws Exception;

    List<Map<String, Object>> exportData(String[] keys, PaginationModelExport paginationModelExport, VisualDevJsonModel visualDevJsonModel) throws IOException, ParseException, SQLException, DataException;

    DataModel visualCreate(VisualdevEntity visualdevEntity,Map<String, Object> map) throws Exception;

    DataModel visualUpdate(VisualdevEntity visualdevEntity,Map<String, Object> map,String id) throws Exception;

    DataModel visualCreate(VisualdevEntity visualdevEntity,Map<String, Object> map,boolean isLink) throws Exception;

    DataModel visualCreate(VisualdevEntity visualdevEntity,Map<String, Object> map,boolean isLink,boolean isUpload) throws Exception;

    DataModel visualUpdate(VisualdevEntity visualdevEntity,Map<String, Object> map,String id,boolean isUpload) throws Exception;

    void visualDelete(VisualdevEntity visualdevEntity,List<String> id) throws Exception;

    /**
     * 创建
     * @param modelId
     * @param tenantId
     * @param visualdevModelDataCrForm
     * @return
     */
    ActionResult createData(String modelId, String tenantId, VisualdevModelDataCrForm visualdevModelDataCrForm) throws WorkFlowException;
}
