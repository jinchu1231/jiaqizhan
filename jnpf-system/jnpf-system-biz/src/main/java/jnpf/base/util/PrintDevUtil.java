package jnpf.base.util;

import jnpf.base.model.enums.ResultColumnKeysEnum;
import jnpf.base.model.printdev.PrintFieldModel;
import jnpf.database.model.dto.PrepSqlDTO;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DynamicDataSourceUtil;
import lombok.Cleanup;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 打印模板-工具类
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
public class PrintDevUtil {

    public static String compareGetColumnComment(DbLinkEntity dbLinkEntity, String table, String columnName){
        List<PrintFieldModel> printFieldModelList = new ArrayList<>();
        try {
            new PrepSqlDTO().withConn(dbLinkEntity).switchConn();
            @Cleanup Connection conn = DynamicDataSourceUtil.getCurrentConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            @Cleanup ResultSet rs = metaData.getColumns(conn.getCatalog(), "%", table, "%");
            while (rs.next()) {
                PrintFieldModel printFieldModel = new PrintFieldModel();
                for (Map.Entry<ResultColumnKeysEnum, Consumer<String>> resultColumnEnumEntry : ResultColumnKeysEnum.getCommon(printFieldModel).entrySet()) {
                    try{
                        resultColumnEnumEntry.getValue().accept(rs.getString(resultColumnEnumEntry.getKey().name()));
                    }catch (Exception e){
                        continue;
                    }
                }
                printFieldModelList.add(printFieldModel);
            }
            for(PrintFieldModel printFieldModel : printFieldModelList){
                if(printFieldModel.getColumnName() != null && printFieldModel.getColumnName().equalsIgnoreCase(columnName)){
                    return printFieldModel.getReMarks();
                }
            }
        } catch (SQLException e) {
        }finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return null;
    }

}
