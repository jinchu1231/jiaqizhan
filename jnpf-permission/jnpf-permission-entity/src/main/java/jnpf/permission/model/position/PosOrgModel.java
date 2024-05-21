package jnpf.permission.model.position;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PosOrgModel extends SumTree {

   @Schema(description ="名称")
   private String fullName;
   @Schema(description ="状态")
   private Integer enabledMark;
   @JSONField(name = "category")
   private String type;
   @Schema(description ="图标")
   private String icon;
   @Schema(description ="排序")
   private String sortCode;
   @Schema(description ="创建时间")
   private Date creatorTime;


   private String organize;
   @Schema(description ="组织id树")
   private List<String> organizeIds;
}
