package jnpf.visualdata.service;

import jnpf.base.service.SuperService;
import jnpf.visualdata.entity.VisualCategoryEntity;
import jnpf.visualdata.model.VisualPagination;

import java.util.List;

/**
 * 大屏
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
public interface VisualCategoryService extends SuperService<VisualCategoryEntity> {

    /**
     * 列表
     *
     * @param pagination 条件
     * @return 大屏分类列表（分页）
     */
    List<VisualCategoryEntity> getList(VisualPagination pagination);

    /**
     * 列表
     *
     * @return 大屏分类列表
     */
    List<VisualCategoryEntity> getList();

    /**
     * 验证值
     *
     * @param value 名称
     * @param id    主键值
     * @return ignore
     */
    boolean isExistByValue(String value, String id);

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    VisualCategoryEntity getInfo(String id);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(VisualCategoryEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return ignore
     */
    boolean update(String id, VisualCategoryEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(VisualCategoryEntity entity);
}
