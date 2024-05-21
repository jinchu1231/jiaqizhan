package jnpf.portal.model;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 * 按钮
 */
@Data
public class PortalModel extends SumTree {
    private String fullName;
    private String icon;
    private String systemId;
    private boolean disabled;
    private Long creatorTime;
    private Long sortCode;
}
