package com.forwardauction.gateway.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HateoasHelperTest {

    @Test
    void link_returnsHrefMap() {
        Map<String, Object> link = HateoasHelper.link("/test");
        assertEquals("/test", link.get("href"));
        assertFalse(link.containsKey("templated"));
    }

    @Test
    void templatedLink_includesTemplatedFlag() {
        Map<String, Object> link = HateoasHelper.templatedLink("/items/{id}");
        assertEquals("/items/{id}", link.get("href"));
        assertEquals(true, link.get("templated"));
    }

    @Test
    void linksBuilder_buildsMultipleLinks() {
        Map<String, Object> links = HateoasHelper.links()
                .add("self", "/test")
                .add("next", "/test/next")
                .addTemplated("item", "/items/{id}")
                .build();

        assertEquals(3, links.size());
        assertTrue(links.containsKey("self"));
        assertTrue(links.containsKey("next"));
        assertTrue(links.containsKey("item"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void linksBuilder_linkValuesAreCorrect() {
        Map<String, Object> links = HateoasHelper.links()
                .add("self", "/test")
                .addTemplated("item", "/items/{id}")
                .build();

        Map<String, Object> selfLink = (Map<String, Object>) links.get("self");
        assertEquals("/test", selfLink.get("href"));

        Map<String, Object> itemLink = (Map<String, Object>) links.get("item");
        assertEquals("/items/{id}", itemLink.get("href"));
        assertEquals(true, itemLink.get("templated"));
    }

    @Test
    void linksBuilder_emptyBuilder_returnsEmptyMap() {
        Map<String, Object> links = HateoasHelper.links().build();
        assertTrue(links.isEmpty());
    }

    @Test
    void link_returnedMapIsImmutable() {
        Map<String, Object> link = HateoasHelper.link("/test");
        assertThrows(UnsupportedOperationException.class, () -> link.put("extra", "val"));
    }
}
