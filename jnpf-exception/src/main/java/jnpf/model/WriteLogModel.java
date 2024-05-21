package jnpf.model;

import jnpf.base.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WriteLogModel implements Serializable {
    private String userId;
    private String userName;
    private String abstracts;
    private UserInfo userInfo;
    private int loginMark;
    private Integer loginType;
    private long requestDuration;


}
