package jnpf.base.model.button;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 按钮
 */
@Data
public class ButtonModel implements Serializable {
    private String id;
    private String parentId;
    private String fullName;
    private String enCode;
    private String icon;
    private String urlAddress;
    private String moduleId;
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
