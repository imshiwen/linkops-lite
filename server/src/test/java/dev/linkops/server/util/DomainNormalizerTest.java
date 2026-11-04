package dev.linkops.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DomainNormalizerTest {

    @Test
    void normalizesUrlsAndBareDomains() {
        assertEquals("cursivefontgenerator.online", DomainNormalizer.normalize("https://www.CursiveFontGenerator.online/fonts"));
        assertEquals("cursivefontgenerator.online", DomainNormalizer.normalize(" cursivefontgenerator.online/ "));
        assertThrows(IllegalArgumentException.class, () -> DomainNormalizer.normalize("https:///x"));
    }
}
