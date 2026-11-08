package org.pixel.content;

import org.junit.jupiter.api.Test;
import org.pixel.content.importer.settings.FontImporterSettings;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ContentManagerTest {

    /** Returns the file content plus the settings it was imported with. */
    @ContentImporterInfo(type = String.class, extension = ".txt")
    static class EchoImporter implements ContentImporter<String> {
        @Override
        public String process(ImportContext ctx) {
            return new String(ctx.getData(), StandardCharsets.UTF_8) + "|" + ctx.getSettings();
        }
    }

    static class NotAnnotatedImporter implements ContentImporter<Integer> {
        @Override
        public Integer process(ImportContext ctx) {
            return 1;
        }
    }

    private static ContentManager manager() {
        return new ContentManager(path -> path.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void cacheReturnsSameAssetForSameSettings() {
        ContentManager cm = manager();
        cm.addContentImporter(new EchoImporter());

        String a = cm.load("a.txt", String.class, new FontImporterSettings(10, 1), true);
        String b = cm.load("a.txt", String.class, new FontImporterSettings(10, 1), true);

        assertSame(a, b);
    }

    @Test
    void cacheDistinguishesImporterSettings() {
        ContentManager cm = manager();
        cm.addContentImporter(new EchoImporter());

        String small = cm.load("a.txt", String.class, new FontImporterSettings(10, 1), true);
        String large = cm.load("a.txt", String.class, new FontImporterSettings(40, 1), true);

        assertNotEquals(small, large);
    }

    @Test
    void unannotatedImporterDoesNotPreventOthersFromRegistering() {
        ContentManager cm = manager();
        cm.addContentImporter(new NotAnnotatedImporter(), new EchoImporter());

        assertNotNull(cm.load("a.txt", String.class));
    }
}
