package jnpf.permission.model.authorize;

import jnpf.base.model.advancedquery.OnlineDynamicSqlModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizeConditionModel<T> implements Serializable{
	private Object obj;
	private String moduleId;
	private String tableName;
	private List<OnlineDynamicSqlModel> sqlModelList;
	private QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder formS;

	public AuthorizeConditionModel(Object obj, String moduleId, String tableName) {
		this.obj = obj;
		this.moduleId = moduleId;
		this.tableName = tableName;
	}
}
