package jnpf.base.model.advancedquery;

import lombok.Data;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.util.List;

/**
 *
 * dynamicSql模型
 * @author JNPF开发平台组
 * @version V3.4.4
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2022/9/27
 */
@Data
public class OnlineDynamicSqlModel {
	private List<BasicColumn> columns;
	private SqlTable sqlTable;
	private String tableName;
	private boolean isMain;
	private String foreign;
	private String relationKey;

	public OnlineDynamicSqlModel(){

	}

	public OnlineDynamicSqlModel(SqlTable sqlTable, List<BasicColumn> sqlColumns) {
		this.sqlTable = sqlTable;
		this.columns = sqlColumns;
		this.isMain = true;
	}

	public OnlineDynamicSqlModel(SqlTable sqlTable, List<BasicColumn> sqlColumns, String foreign, String relationKey, boolean b) {
		this.sqlTable = sqlTable;
		this.columns = sqlColumns;
		this.foreign = foreign;
		this.relationKey = relationKey;
		this.isMain = b;
	}
}
