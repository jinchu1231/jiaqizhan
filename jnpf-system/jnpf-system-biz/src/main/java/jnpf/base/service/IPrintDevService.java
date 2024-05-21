package jnpf.base.service;

import jnpf.base.entity.OperatorRecordEntity;
import jnpf.base.entity.PrintDevEntity;
import jnpf.base.model.printdev.PrintOption;
import jnpf.base.model.printdev.PrintPagination;
import jnpf.base.model.printdev.PrintTableTreeModel;
import jnpf.base.model.printdev.vo.PrintDevVO;
import jnpf.util.treeutil.SumTree;

import java.util.List;
import java.util.Map;

/**
 * 打印模板-服务类
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
public interface IPrintDevService extends SuperService<PrintDevEntity> {

    /**
     * 列表
     *
     * @return 打印实体类
     */
    List<PrintDevEntity> getList(PrintPagination paginationPrint);

    /**
     * 列表
     *
     * @return 打印实体类
     */
    List<PrintOption> getPrintTemplateOptions(List<String> ids);



    /**
     * 获取打印模板对象树形模型
     *
     * @return 打印模型树
     * @throws Exception 字典分类不存在BUG
     */
    List<PrintDevVO> getTreeModel() throws Exception;

    /**
     * 获取打印模板对象树形模型(selector)
     *
     * @param type 打印模板类型
     * @return 打印模型树
     * @throws Exception 字典分类不存在BUG
     */
    List<PrintDevVO> getTreeModel(Integer type);

    /**
     * 重名验证
     * @param fullName 全名
     * @param id 模板id
     * @return true:存在 、false:不存在
     */
    Boolean checkNameExist(String fullName,String id);

    /**
     * 获取流程经办记录集合
     * @param taskId 任务ID
     * @return 经办记录集合
     */
    List<OperatorRecordEntity> getFlowTaskOperatorRecordList(String taskId);

    /**
     * sql获取打印内容
     * @param dbLinkId 数据连接ID
     * @param sqlTempLate SQL语句数组
     * @return 打印内容
     * @throws Exception ignore
     */
    Map<String,Object> getDataBySql(String dbLinkId, String sqlTempLate) throws Exception;

    /**
     * 获取打印表字段结构
     * @param dbLinkId 数据连接ID
     * @param sqlTempLate SQL语句数组
     * @return 打印树形模型
     * @throws Exception ignore
     */
    List<SumTree<PrintTableTreeModel>> getPintTabFieldStruct(String dbLinkId, String sqlTempLate) throws Exception;

    /**
     * 新增更新校验
     * @param printDevEntity 打印模板对象
     * @param fullNameCheck 重名校验开关
     * @param encodeCheck 重码校验开关
     */
    void creUpdateCheck(PrintDevEntity printDevEntity, Boolean fullNameCheck, Boolean encodeCheck);

    Map<String, Object> getDataMap(PrintDevEntity entity, String id);
}
