package jnpf.base.model.column;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 列表
 */
@Data
public class ColumnModel implements Serializable {
    private String id;
    private String parentId;
    private String fullName;
    private String enCode;
    private String bindTable;
    private String bindTableName;
    private String moduleId;
    private String icon;
    private String systemId;
    private Long sortCode=999999L;
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
