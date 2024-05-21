package jnpf.base.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 同步菜单类型
 *
 * @author JNPF开发平台组
 * @version V3.4.1
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2022/4/14
 */
@Data
@Schema(description="发布功能参数" )
public class VisualDevPubModel {
	@Schema(description = "pc" )
	private Integer pc;
	@Schema(description = "app" )
	private Integer app;
	@Schema(description = "pc菜单父id" )
	private String pcModuleParentId;
	@Schema(description = "app菜单父id" )
	private String appModuleParentId;
	@Schema(description = "pc系统id" )
	private String pcSystemId;
	@Schema(description = "app系统id" )
	private String appSystemId;
}
