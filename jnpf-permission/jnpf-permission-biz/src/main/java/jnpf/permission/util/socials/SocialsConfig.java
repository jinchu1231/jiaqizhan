package jnpf.permission.util.socials;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/9/6 14:58:23
 */
@Component
@ConfigurationProperties(prefix = SocialsConfig.PREFIX)
public class SocialsConfig {
    public static final String PREFIX = "socials";

    private boolean socialsEnabled = false;

    private List<Config> config;

    private Map<String, Config> socialMap;

    public SocialsConfig() {
    }

    public SocialsConfig(List<Config> config, Map<String, Config> socialMap) {
        this.config = config;
        this.socialMap = socialMap;
    }

    public boolean isSocialsEnabled() {
        return socialsEnabled;
    }

    public void setSocialsEnabled(boolean socialsEnabled) {
        this.socialsEnabled = socialsEnabled;
    }

    public void setConfig(List<Config> config) {
        this.config = config;
        this.socialMap = new HashMap<>();
        config.stream().forEach(item -> {
            this.socialMap.put(item.getProvider(), item);
        });
    }

    public List<Config> getConfig() {
        return config;
    }

    public Map<String, Config> getSocialMap() {
        return socialMap;
    }

    public static class Config {
        private String provider;
        private String clientId;
        private String clientSecret;
        private String agentId;

        public Config() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Config config = (Config) o;
            return Objects.equals(provider, config.provider) && Objects.equals(clientId, config.clientId) && Objects.equals(clientSecret, config.clientSecret) && Objects.equals(agentId, config.agentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(provider, clientId, clientSecret, agentId);
        }

        @Override
        public String toString() {
            return "Config{" +
                    "provider='" + provider + '\'' +
                    ", clientId='" + clientId + '\'' +
                    ", clientSecret='" + clientSecret + '\'' +
                    ", agentId='" + agentId + '\'' +
                    '}';
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public Config(String provider, String clientId, String clientSecret, String agentId) {
            this.provider = provider;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.agentId = agentId;
        }
    }

}
