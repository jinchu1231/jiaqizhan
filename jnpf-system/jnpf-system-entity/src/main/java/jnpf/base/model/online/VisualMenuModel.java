package jnpf.base.model.online;

import lombok.Data;

/**
 * 可视化菜单对象
 *
 * @author JNPF开发平台组
 * @version V3.4
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2022/4/6
 */
@Data
public class VisualMenuModel {
	/**
	 * 功能id
	 */
	private String id;

	/**
	 * pc 按钮配置
	 */
	private PerColModels pcPerCols;

	/**
	 * app 按钮配置
	 */
	private PerColModels appPerCols;

	/**
	 * 功能名
	 */
	private String fullName;

	/**
	 * 功能编码
	 */
	private String encode;

	private Integer pc;

	private Integer app;

	private String pcModuleParentId;

	private String appModuleParentId;

	private String pcSystemId;

	private String appSystemId;

	private Integer type;
}
