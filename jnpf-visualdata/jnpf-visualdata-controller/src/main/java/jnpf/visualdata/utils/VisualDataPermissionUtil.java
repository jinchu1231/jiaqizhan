package jnpf.visualdata.utils;

import cn.dev33.satoken.stp.StpUtil;
import jnpf.properties.SecurityProperties;
import jnpf.util.ServletUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VisualDataPermissionUtil {

    private static SecurityProperties securityProperties;
    private static final String[] refererPath = new String[]{"**/DataV/view/{id}", "**/DataV/build/{id}"};

    public VisualDataPermissionUtil(SecurityProperties securityProperties) {
        VisualDataPermissionUtil.securityProperties = securityProperties;
    }

    public static void checkByReferer() {
        if (securityProperties.isEnablePreAuth()) {
            String referer = ServletUtil.getHeader("Referer");
            String id = null;
            for (String s : refererPath) {
                Map<String, String> pathVariables = ServletUtil.getPathVariables(s, referer);
                id = pathVariables.get("id");
                if (id != null) {
                    id = id.split("[?]")[0];
                    break;
                }
            }
            StpUtil.checkPermissionOr("onlineDev.dataScreen", id);
        }
    }
}
