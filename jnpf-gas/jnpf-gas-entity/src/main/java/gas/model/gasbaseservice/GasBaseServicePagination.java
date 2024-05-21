package gas.model.gasbaseservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jnpf.base.Pagination;
import java.util.List;

/**
 *
 * 服务区设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaseServicePagination extends Pagination {
    /** 关键词搜索 */
    private String jnpfKeyword;
    /** 查询key */
	private String[] selectKey;
    /** 选中数据数组id */
    private String[] selectIds;
    /** json */
	private String json;
    /** 数据类型 0-当前页，1-全部数据 */
	private String dataType;
    /** 高级查询 */
	private String superQueryJson;
    /** 功能id */
    private String moduleId;
    /** 菜单id */
    private String menuId;
    /** 服务区名 */
    @JsonProperty("name")
    private Object name;
    /** 管理单位 */
    @JsonProperty("managementunit")
    private Object managementunit;
    /** 经营单位 */
    @JsonProperty("businessunit")
    private Object businessunit;
    /** 所处地市 */
    @JsonProperty("city")
    private Object city;
    /** 公路类型 */
    @JsonProperty("highwayType")
    private Object highwayType;
    /** 服务类型 */
    @JsonProperty("type")
    private Object type;
    /** 方向 */
    @JsonProperty("direction")
    private Object direction;
    /** 行车方向 */
    @JsonProperty("directiondriving")
    private Object directiondriving;
    /** 是否入省口 */
    @JsonProperty("entrance")
    private Object entrance;
    /** 是否出省口 */
    @JsonProperty("export")
    private Object export;
}
