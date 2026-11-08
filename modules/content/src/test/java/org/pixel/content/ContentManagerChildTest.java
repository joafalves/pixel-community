package org.pixel.content;

import org.junit.jupiter.api.Test;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.importer.TexturePackImporter;
import org.pixel.pipeline.DataPipeline;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ContentManager#child()}: shared lookups, ownership, unload and disposal.
 */
class ContentManagerChildTest {

    /** A disposable test asset. */
    static class Asset implements Disposable {
        final String path;
        final List<Asset> dependencies = new ArrayList<>();
        boolean disposed;

        Asset(String path) {
            this.path = path;
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }

    /** Loads an {@link Asset}; a file "a+b" also loads asset "b" through the context, like a texture pack's image. */
    @ContentImporterInfo(type = Asset.class, extension = ".asset")
    static final class AssetImporter implements ContentImporter<Asset>, Disposable {
        int imports;
        boolean disposed;

        @Override
        public Asset process(ImportContext ctx) {
            imports++;
            Asset asset = new Asset(ctx.getFilepath());
            int plus = ctx.getFilepath().indexOf('+');
            if (plus >= 0) {
                asset.dependencies.add(ctx.getContentManager().load(ctx.getFilepath().substring(plus + 1),
                        Asset.class));
            }
            return asset;
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }

    private final AssetImporter importer = new AssetImporter();
    private final ContentManager root = new ContentManager(path -> path.getBytes(StandardCharsets.UTF_8), importer);

    @Test
    void changingSettingsAfterALoadKeepsTheCachedAsset() {
        // one settings object reused for several loads (a common pattern): the cache keeps its own copy
        var settings = new org.pixel.content.importer.settings.FontImporterSettings();
        settings.setFontSize(32);
        Asset small = root.load("font", Asset.class, settings);
        settings.setFontSize(48);
        Asset large = root.load("font", Asset.class, settings);
        settings.setFontSize(32);

        assertNotSame(small, large);
        assertSame(small, root.load("font", Asset.class, settings));
        settings.setFontSize(48);
        assertSame(large, root.load("font", Asset.class, settings));
        assertEquals(2, importer.imports);
    }

    @Test
    void childrenShareWhatTheirAncestorsLoaded() {
        Asset shared = root.load("ui", Asset.class);
        ContentManager level = root.child("level-1");
        ContentManager room = level.child("room");

        assertSame(shared, level.load("ui", Asset.class));
        assertSame(shared, room.load("ui", Asset.class));
        assertEquals(1, importer.imports);
        assertSame(root, level.getParent());
        assertEquals("level-1", level.getName());
    }

    @Test
    void whatAChildLoadsIsItsOwnAndInvisibleToItsParent() {
        ContentManager level = root.child();
        Asset boss = level.load("boss", Asset.class);

        assertSame(boss, level.load("boss", Asset.class));
        Asset rootBoss = root.load("boss", Asset.class);
        assertNotSame(boss, rootBoss); // the parent loads (and owns) its own copy
        assertEquals(2, importer.imports);
    }

    @Test
    void unloadFreesOnlyTheChildsAssetsAndKeepsItUsable() {
        Asset shared = root.load("ui", Asset.class);
        ContentManager level = root.child();
        Asset tiles = level.load("tiles", Asset.class);
        level.load("ui", Asset.class); // borrowed from the root

        level.unload();

        assertTrue(tiles.disposed);
        assertFalse(shared.disposed);
        Asset reloaded = level.load("tiles", Asset.class); // usable again, starts from scratch
        assertNotSame(tiles, reloaded);
        assertFalse(reloaded.disposed);
        assertFalse(level.isDisposed());
    }

    @Test
    void dependenciesLoadedByAnImporterBelongToTheLoadingChild() {
        Asset shared = root.load("atlas", Asset.class);
        ContentManager level = root.child();
        Asset pack = level.load("pack+sheet", Asset.class);
        Asset packUsingShared = level.load("pack2+atlas", Asset.class);

        Asset sheet = pack.dependencies.get(0);
        assertSame(sheet, level.load("sheet", Asset.class));
        assertSame(shared, packUsingShared.dependencies.get(0)); // found in the root: not loaded again
        level.unload();
        assertTrue(sheet.disposed);
        assertFalse(shared.disposed);
    }

    @Test
    void unloadingOrDisposingAParentDoesTheSameToItsChildrenFirst() {
        ContentManager level = root.child();
        ContentManager room = level.child();
        Asset roomAsset = room.load("chest", Asset.class);

        level.unload();
        assertTrue(roomAsset.disposed);
        assertFalse(room.isDisposed()); // unload keeps children usable

        Asset again = room.load("chest", Asset.class);
        root.dispose();
        assertTrue(again.disposed);
        assertTrue(level.isDisposed());
        assertTrue(room.isDisposed());
        assertThrows(IllegalStateException.class, () -> room.load("chest", Asset.class));
        assertThrows(IllegalStateException.class, root::child);
        assertTrue(importer.disposed); // registered on the root, disposed with it
    }

    @Test
    void disposingAChildDetachesItAndLeavesInheritedImportersAlone() {
        ContentManager level = root.child();
        level.load("tiles", Asset.class);
        level.dispose();

        assertTrue(level.isDisposed());
        assertFalse(importer.disposed);
        assertTrue(root.toString().contains("0 children"));
        assertNotNull(root.load("tiles", Asset.class)); // the root keeps working
        level.dispose(); // idempotent
    }

    @Test
    void importersAndDataPipelineAreInheritedButOverridesStayLocal() {
        ContentManager level = root.child();
        AssetImporter local = new AssetImporter();
        level.addContentImporter(local);
        level.load("x", Asset.class);
        root.load("x", Asset.class);
        assertEquals(1, local.imports);   // the child's own importer
        assertEquals(1, importer.imports); // the root keeps its own

        DataPipeline<byte[]> pipeline = new DataPipeline<>((ctx, data) -> ctx.next(data));
        root.setDataPipeline(pipeline);
        assertSame(pipeline, level.getDataPipeline());
        DataPipeline<byte[]> own = new DataPipeline<>();
        level.setDataPipeline(own);
        assertSame(own, level.getDataPipeline());
        assertSame(pipeline, root.getDataPipeline());

        level.dispose();
        assertTrue(local.disposed);
        assertFalse(importer.disposed);
    }

    @Test
    void uncachedLoadsAreNotOwned() {
        ContentManager level = root.child();
        Asset mine = level.load("temp", Asset.class, null, false);
        level.dispose();
        assertFalse(mine.disposed); // the caller owns it
    }

    /** A texture that records its disposal. */
    static final class TestTexture extends Texture {
        boolean disposed;

        TestTexture() {
            super(64, 64);
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }

    @ContentImporterInfo(type = Texture.class, extension = ".png")
    static final class TestTextureImporter implements ContentImporter<Texture> {
        @Override
        public Texture process(ImportContext ctx) {
            return new TestTexture();
        }
    }

    @Test
    void aChildsTexturePackNeverDisposesATextureItBorrowed() {
        String json = "{\"frames\":{\"a\":{\"frame\":{\"x\":0,\"y\":0,\"w\":8,\"h\":8}}},"
                + "\"meta\":{\"image\":\"atlas.png\"}}";
        ContentManager game = new ContentManager(path -> (path.endsWith(".json") ? json : "png")
                .getBytes(StandardCharsets.UTF_8), new TestTextureImporter(), new TexturePackImporter());
        TestTexture hudAtlas = (TestTexture) game.load("sprites/atlas.png", Texture.class);
        ContentManager level = game.child();
        TexturePack pack = level.load("sprites/atlas.json", TexturePack.class);

        assertSame(hudAtlas, pack.getTexture()); // borrowed from the game's manager
        level.unload();
        assertFalse(hudAtlas.disposed);

        TexturePack uncached = game.load("sprites/atlas.json", TexturePack.class, null, false);
        uncached.dispose(); // the caller disposes its pack: the cached texture stays valid
        assertFalse(hudAtlas.disposed);
        game.dispose();
        assertTrue(hudAtlas.disposed); // released once, by its owner
    }

    @Test
    void aFailingDisposeDoesNotStopTheRest() {
        @ContentImporterInfo(type = Asset.class, extension = ".asset")
        final class FragileImporter implements ContentImporter<Asset> {
            @Override
            public Asset process(ImportContext ctx) {
                if (ctx.getFilepath().equals("broken")) {
                    return new Asset("broken") {
                        @Override
                        public void dispose() {
                            throw new IllegalStateException("driver error");
                        }
                    };
                }
                return new Asset(ctx.getFilepath());
            }
        }
        ContentManager manager = new ContentManager(path -> new byte[1], new FragileImporter());
        ContentManager level = manager.child();
        Asset other = level.load("fine", Asset.class);
        level.load("broken", Asset.class);
        Asset third = level.load("fine2", Asset.class);

        manager.dispose();

        assertTrue(other.disposed);
        assertTrue(third.disposed);
        assertTrue(level.isDisposed());
    }

    @Test
    void anAssetFinishingAfterDisposalIsReleasedAndTheLoadFails() {
        AtomicReference<ContentManager> target = new AtomicReference<>();
        List<Asset> created = new ArrayList<>();
        @ContentImporterInfo(type = Asset.class, extension = ".asset")
        final class SlowImporter implements ContentImporter<Asset> {
            @Override
            public Asset process(ImportContext ctx) {
                target.get().dispose(); // another thread disposes the manager while this import runs
                Asset asset = new Asset(ctx.getFilepath());
                created.add(asset);
                return asset;
            }
        }
        ContentManager level = new ContentManager(path -> new byte[1]).child();
        level.addContentImporter(new SlowImporter());
        target.set(level);

        assertThrows(IllegalStateException.class, () -> level.load("late", Asset.class));
        assertTrue(created.get(0).disposed);
    }

    @Test
    void unnamedChildrenGetDistinctNames() {
        assertNotEquals(root.child().getName(), root.child().getName());
        assertEquals("root/child-3", root.child().getName());
    }
}
