package org.pixel.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;

import java.util.ArrayList;
import java.util.List;

public class TileMapViewTest {
    @Test
    public void emptyConstructor() {
        TileMapView tileMapView = new TileMapView();

        Assertions.assertEquals(LayerView.class, tileMapView.layerView.getClass());
    }

    @Test
    public void draw() {
        TileMap tileMap = new TileMap();
        List<Layer> layerList = new ArrayList<>();
        Layer layer1 = Mockito.mock(Layer.class);
        Layer layer2 = Mockito.mock(Layer.class);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledView<Layer> layerTiledView = (TiledView<Layer>) Mockito.mock(TiledView.class);

        layerList.add(layer1);
        layerList.add(layer2);

        tileMap.setLayers(layerList);

        TileMapView tileMapView = new TileMapView(layerTiledView);
        tileMapView.draw(spriteBatch, tileMap);

        InOrder inOrder = Mockito.inOrder(layerTiledView);

        inOrder.verify(layerTiledView).draw(spriteBatch, layer1);
        inOrder.verify(layerTiledView).draw(spriteBatch, layer2);
    }
}