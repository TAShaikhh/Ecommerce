package com.forwardauction.gateway.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HateoasHelper {

    private HateoasHelper() {}

    public static Map<String, Object> link(String href) {
        return Map.of("href", href);
    }

    public static Map<String, Object> templatedLink(String href) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("href", href);
        m.put("templated", true);
        return m;
    }

    public static LinksBuilder links() {
        return new LinksBuilder();
    }

    public static class LinksBuilder {
        private final LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        public LinksBuilder add(String rel, String href) {
            map.put(rel, link(href));
            return this;
        }

        public LinksBuilder addTemplated(String rel, String href) {
            map.put(rel, templatedLink(href));
            return this;
        }

        public Map<String, Object> build() {
            return Map.copyOf(map);
        }
    }
}
