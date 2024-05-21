package jnpf.onlinedev.service;

import jnpf.base.entity.VisualdevEntity;
import jnpf.onlinedev.model.OnlineInfoModel;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

/**
 *
 * 功能设计表单数据
 * @author JNPF开发平台组
 * @version V3.2
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/10/26
 */
public interface VisualDevInfoService {

	/**
	 *	编辑页数据回显
	 * @param id 主键id
	 * @param visualdevEntity 可视化实体
	 * @return
	 */
	VisualdevModelDataInfoVO getEditDataInfo(String id, VisualdevEntity visualdevEntity);

	/**
	 * 详情页数据
	 * @param id
	 * @param visualdevEntity
	 * @return
	 */
	VisualdevModelDataInfoVO getDetailsDataInfo(String id, VisualdevEntity visualdevEntity);

	/**
	 * 详情页数据(过滤字段)
	 * @param id
	 * @param visualdevEntity
	 * @return
	 */
	VisualdevModelDataInfoVO getDetailsDataInfo(String id, VisualdevEntity visualdevEntity, OnlineInfoModel model);

	/**
	 *
	 * @param id
	 * @param modelId
	 * @return
	 */
	VisualdevModelDataInfoVO getDetailsDataInfoNew(String id, String modelId);

	/**
	 *
	 * @param id
	 * @param modelId
	 * @return
	 */
	VisualdevModelDataInfoVO infoDataChangeNew(String id, String modelId) throws SQLException, IOException, ParseException;
}
