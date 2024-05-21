package jnpf.base.model.module;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 功能
 */
@Data
public class ModuleModel implements Serializable {
    private String id;
    private String parentId;
    private String fullName;
    private String icon;
    //1-类别、2-页面
    private int type;
    private String urlAddress;
    private String linkTarget;
    private String category;
    private String description;
    private Long sortCode=999999L;
    private String enCode;
    private String propertyJson;

    private String systemId;
    private Long creatorTime;
    private Date creatorTimes;

    public Long getCreatorTime() {
        if (this.creatorTimes != null && this.creatorTime == null) {
            return this.getCreatorTimes().getTime();
        } else if (this.creatorTime != null){
            return this.creatorTime;
        }
        return 0L;
    }
}
