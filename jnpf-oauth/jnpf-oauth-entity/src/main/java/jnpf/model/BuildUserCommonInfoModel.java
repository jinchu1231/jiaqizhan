package jnpf.model;

import jnpf.base.UserInfo;
import jnpf.base.entity.SystemEntity;
import jnpf.permission.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildUserCommonInfoModel implements Serializable {

    private UserInfo userInfo;
    private SystemEntity mainSystemEntity;
    private SystemEntity workSystemEntity;
    private UserEntity userEntity;
    private BaseSystemInfo baseSystemInfo;
    private String systemId;

}
