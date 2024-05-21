package jnpf.service;

import jnpf.base.ActionResult;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.FlowFormDataModel;
import jnpf.permission.entity.UserEntity;

import java.sql.SQLException;
import java.util.Map;

/**
 * 表单数据操作
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 18:01
 */
public interface FormDataService{

	/**
	 * 新增
	 *
	 * @param formId 表单id
	 * @param id     主键id
	 * @param map 数据
	 * @return ignore
	 */
	void create(String formId, String id, Map<String, Object> map) throws Exception;

	/**
	 * 修改
	 *
	 * @param formId 表单id
	 * @param id     主键id
	 * @param map 数据
	 * @return ignore
	 */
	void update(String formId, String id, Map<String, Object> map) throws WorkFlowException, SQLException, DataException;


	void saveOrUpdate(FlowFormDataModel flowFormDataModel) throws WorkFlowException;
	void saveOrUpdate(String formId, String id, Map<String, Object> map, UserEntity delegateUser) throws WorkFlowException;

	/**
	 * 删除
	 *
	 * @param formId 表单id
	 * @param id     主键id
	 * @return ignore
	 */
	boolean delete(String formId, String id) throws Exception;

	/**
	 * 信息
	 *
	 * @param formId 表单id
	 * @param id     主键id
	 * @return ignore
	 */
	ActionResult info(String formId, String id);
}
