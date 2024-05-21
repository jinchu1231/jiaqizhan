package jnpf.permission.model.authorize;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeDataModel extends SumTree {
    private  String id;
    private String fullName;
    private String icon;
    private Boolean showcheck;
    private Integer checkstate;
    private String title;
    private String moduleId;
    private String type;
    private Date creatorTime;
    private String category;
    private boolean disabled;
    private Long sortCode=9999L;
    private String systemId;
}
