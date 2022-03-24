package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.pixel.core.Camera2D;
import org.pixel.ext.tiled.content.*;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class TiledMapViewTest {
    @Test
    public void emptyConstructor() {
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView tileMapView = new TileMapView(spriteBatch, camera2D);

        Assertions.assertEquals(TileLayerView.class, tileMapView.layerView.getClass());
    }

    @Test
    public void draw() {
        TiledMap tileMap = new TiledMap();
        List<TiledLayer> layerList = new ArrayList<>();
        TiledTileLayer layer1 = Mockito.mock(TiledTileLayer.class);
        TiledTileLayer layer2 = Mockito.mock(TiledTileLayer.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);
        TiledObjectGroup group2 = Mockito.mock(TiledObjectGroup.class);
        TiledImageLayer imageLayer = Mockito.mock(TiledImageLayer.class);
        Camera2D camera2D = Mockito.mock(Camera2D.class);
        TiledView<TiledTileLayer> layerTiledView = (TiledView<TiledTileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);
        TiledView<TiledImageLayer> tiledImageLayerTiledView = (TiledView<TiledImageLayer>) Mockito.mock(TiledView.class);


        Mockito.when(camera2D.screenToVirtualCoordinates(Mockito.anyFloat(), Mockito.anyFloat())).thenReturn(new Vector2(0, 0));

        layerList.add(layer1);
        layerList.add(group2);
        layerList.add(layer2);
        layerList.add(group);

        tileMap.setLayers(layerList);

        TileMapView tileMapView = new TileMapView(layerTiledView, objectGroupTiledView, tiledImageLayerTiledView, camera2D);
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
        TiledView<TiledTileLayer> layerTiledView = (TiledView<TiledTileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);
        TiledView<TiledImageLayer> tiledImageLayerTiledView = (TiledView<TiledImageLayer>) Mockito.mock(TiledView.class);

        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView mapView = new TileMapView(layerTiledView, objectGroupTiledView, tiledImageLayerTiledView, camera2D);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledTileLayer tileLayer = Mockito.mock(TiledTileLayer.class);


        mapView.draw(tileLayer);

        Mockito.verify(layerTiledView).draw(tileLayer, 0);
    }

    @Test
    public void drawObjectGroupLayer() {
        TiledView<TiledTileLayer> layerTiledView = (TiledView<TiledTileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);
        TiledView<TiledImageLayer> tiledImageLayerTiledView = (TiledView<TiledImageLayer>) Mockito.mock(TiledView.class);

        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView mapView = new TileMapView(layerTiledView, objectGroupTiledView, tiledImageLayerTiledView, camera2D);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);

        mapView.draw(group);

        Mockito.verify(objectGroupTiledView).draw(group, 0);
    }

    @Test
    public void drawImageLayer() {
        TiledView<TiledTileLayer> layerTiledView = (TiledView<TiledTileLayer>) Mockito.mock(TiledView.class);
        TiledView<TiledObjectGroup> objectGroupTiledView = (TiledView<TiledObjectGroup>) Mockito.mock(TiledView.class);
        TiledView<TiledImageLayer> tiledImageLayerTiledView = (TiledView<TiledImageLayer>) Mockito.mock(TiledView.class);

        Camera2D camera2D = Mockito.mock(Camera2D.class);

        TileMapView mapView = new TileMapView(layerTiledView, objectGroupTiledView, tiledImageLayerTiledView, camera2D);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledImageLayer imageLayer = Mockito.mock(TiledImageLayer.class);

        mapView.draw(imageLayer);

        Mockito.verify(tiledImageLayerTiledView).draw(imageLayer, 0);
    }
}