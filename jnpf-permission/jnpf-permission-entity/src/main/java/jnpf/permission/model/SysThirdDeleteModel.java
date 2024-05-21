package jnpf.permission.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-05
 */
@Data
@NoArgsConstructor
public class SysThirdDeleteModel {
    private Boolean isBatch;
    private String userId;
    private String accessToken;

    public SysThirdDeleteModel(Boolean isBatch, String userId, String accessToken) {
        this.isBatch = isBatch;
        this.userId = userId;
        this.accessToken = accessToken;
    }
}
