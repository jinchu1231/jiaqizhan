package jnpf.base.service;


import jnpf.base.service.SuperService;
import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.PaginationVisualdev;
import jnpf.exception.WorkFlowException;

import java.util.List;
import java.util.Map;

/**
 *
 * 可视化开发功能表
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-02
 */
public interface VisualdevService extends SuperService<VisualdevEntity> {

    List<VisualdevEntity> getList(PaginationVisualdev paginationVisualdev);

    List<VisualdevEntity> getPageList(PaginationVisualdev paginationVisualdev);

    List<VisualdevEntity> getList();

    VisualdevEntity getInfo(String id);


    /**
     * 获取已发布的版本, 若未发布获取当前版本
     * @param id
     * @return
     */
    VisualdevEntity getReleaseInfo(String id);

    /**
     * 获取动态设计子表名和实际库表名的对应
     * @param formData
     * @return
     */
    Map<String,String> getTableMap(String formData);

    Boolean create(VisualdevEntity entity);

    boolean update(String id, VisualdevEntity entity) throws Exception;

    void delete(VisualdevEntity entity) throws WorkFlowException;

    /**
     * 根据encode判断是否有相同值
     * @param encode
     * @return
     */
    Integer getObjByEncode (String encode, Integer type);

    /**
     * 根据name判断是否有相同值
     * @param name
     * @return
     */
    Integer getCountByName (String name, Integer type);

    /**
     * 无表生成有表
     * @param entity
     */
    void createTable(VisualdevEntity entity) throws WorkFlowException;

    Map<String,String> getTableNameToKey(String modelId);

    Boolean getPrimaryDbField(String linkId, String  table) throws Exception;

    List<VisualdevEntity> selectorList();
}
