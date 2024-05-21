package jnpf.base.model.printdev;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 * 类功能
 *
 * @author JNPF开发平台组 YanYu
 * @version V3.3
 * @copyright 引迈信息技术有限公司
 * @date 2022-07-22
 */
@Data
public class PrintTableTreeModel extends SumTree<PrintTableTreeModel> {

    private String fullName;

}
