package jnpf.yozo.utils;

public class HttpRequestUtils {
    public HttpRequestUtils() {
    }

    public static class StringUtils {
        public StringUtils() {
        }

        public static boolean isNotEmpty(String data) {
            return data != null && data.trim().length() > 0;
        }

        public static boolean equals(String cs1, String cs2) {
            if (cs1 == cs2) {
                return true;
            } else if (cs1 != null && cs2 != null) {
                return cs1.length() != cs2.length() ? false : cs1.equals(cs2);
            } else {
                return false;
            }
        }
    }
}
