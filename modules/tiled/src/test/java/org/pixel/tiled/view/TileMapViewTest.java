package org.pixel.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.pixel.core.Camera2D;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileLayer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObjectGroup;

import java.util.ArrayList;
import java.util.List;

public class TileMapViewTest {
    @Test
    public void emptyConstructor() {
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView tileMapView = new TileMapView(spriteBatch, camera2D);

        Assertions.assertEquals(TileLayerView.class, tileMapView.layerView.getClass());
    }

    @Test
    public void draw() {
        TileMap tileMap = new TileMap();
        List<Layer> layerList = new ArrayList<>();
        TileLayer layer1 = Mockito.mock(TileLayer.class);
        TileLayer layer2 = Mockito.mock(TileLayer.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);
        TiledObjectGroup group2 = Mockito.mock(TiledObjectGroup.class);
        Camera2D camera2D = Mockito.mock(Camera2D.class);
        TiledView<TileLayer> layerTiledView = (TiledView<TileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);

        Mockito.when(camera2D.screenToVirtualCoordinates(Mockito.anyFloat(), Mockito.anyFloat())).thenReturn(new Vector2(0, 0));

        layerList.add(layer1);
        layerList.add(group2);
        layerList.add(layer2);
        layerList.add(group);

        tileMap.setLayers(layerList);

        TileMapView tileMapView = new TileMapView(layerTiledView, objectGroupTiledView, camera2D);
        tileMapView.draw(tileMap, 10);

        InOrder inOrder = Mockito.inOrder(layer1, layer2, group, group2);

        inOrder.verify(layer1).draw(tileMapView);
        inOrder.verify(group2).draw(tileMapView);
        inOrder.verify(layer2).draw(tileMapView);
        inOrder.verify(group).draw(tileMapView);

        Assertions.assertEquals(10L, tileMapView.frame);

        tileMapView.draw(tileMap, 3);

        Assertions.assertEquals(13L, tileMapView.frame);
    }

    @Test
    public void drawTileLayer() {
        TiledView<TileLayer> layerTiledView = (TiledView<TileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);
        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView mapView = new TileMapView(layerTiledView, objectGroupTiledView, camera2D);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TileLayer tileLayer = Mockito.mock(TileLayer.class);


        mapView.draw(tileLayer);

        Mockito.verify(layerTiledView).draw(tileLayer, 0);
    }

    @Test
    public void drawObjectGroupLayer() {
        TiledView<TileLayer> layerTiledView = (TiledView<TileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);
        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView mapView = new TileMapView(layerTiledView, objectGroupTiledView, camera2D);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);

        mapView.draw(group);

        Mockito.verify(objectGroupTiledView).draw(group, 0);
    }
}