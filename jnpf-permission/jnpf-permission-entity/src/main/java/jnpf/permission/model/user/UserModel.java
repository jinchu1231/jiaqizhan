package jnpf.permission.model.user;

import jnpf.base.Pagination;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/5/6 15:00
 */
@Data
public class UserModel implements Serializable {
    private Pagination pagination;
    private List<String> id;
}
