package gas.model.gasbaserroad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jnpf.base.Pagination;
import java.util.List;

/**
 *
 * 路段设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaserRoadPagination extends Pagination {
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
    /** 路段名称 */
    @JsonProperty("name")
    private Object name;
}
