package jnpf.yozo.client;

import java.util.Map;

public interface AppAuthenticator {
    String generateSign(String var1, Map<String, String[]> var2) throws Exception;
}
