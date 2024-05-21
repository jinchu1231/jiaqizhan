package jnpf.permission.model.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Data
public class UserIdModel {
    /**
     * 用户id集合
     */
    @Schema(description = "用户id集合")
    private Object ids;

    public List<String> getIds() {
        List<String> ids = new ArrayList<>(16);
        if (this.ids != null) {
            if (this.ids instanceof List) {
                List list = (List) this.ids;
                Object object = list.size() > 0 ? list.get(0) : null;
                if (Objects.nonNull(object) && object instanceof String) {
                    ids.addAll(list);
                }
            } else {
                String userIds = (String) this.ids;
                ids.add(userIds);
            }
        }
        return ids;
    }
}
