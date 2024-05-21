package jnpf.model.form;

import com.alibaba.fastjson.JSONArray;
import jnpf.model.visualJson.analysis.FormAllModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualTableModel {
    private  JSONArray jsonArray;
    private List<FormAllModel> formAllModel=new ArrayList<>();
    private String table;
    private String linkId;
    private String fullName;
    private boolean concurrency = false;
    private Integer primaryKey = 1;
    //逻辑删除
    private Boolean logicalDelete = false;
}
