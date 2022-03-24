package org.pixel.ext.tiled.view;

import org.pixel.core.Camera2D;
import org.pixel.ext.tiled.content.*;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Vector2;

public class TileMapView implements GenericTileMapView {
    protected long frame;
    TiledView<TiledTileLayer> layerView;
    TiledView<TiledObjectGroup> groupView;
    TiledView<TiledImageLayer> imageLayerView;
    Camera2D camera2D;
    Boundary boundary;

    public TileMapView(SpriteBatch spriteBatch, Camera2D camera2D) {
        this.boundary = new Boundary(0, 0, 0, 0);
        this.layerView = new TileLayerView(spriteBatch, boundary);
        this.groupView = new TiledObjectGroupView(spriteBatch, boundary);
        this.imageLayerView = new TiledImageLayerView(spriteBatch, boundary);

        frame = 0;
        this.camera2D = camera2D;
    }

    public TileMapView(TiledView<TiledTileLayer> layerView, TiledView<TiledObjectGroup> groupView, TiledView<TiledImageLayer> imageView, Camera2D camera2D) {
        this.layerView = layerView;
        this.groupView = groupView;
        this.imageLayerView = imageView;

        frame = 0;
        this.camera2D = camera2D;
        this.boundary = new Boundary(0, 0, 0, 0);
    }

    @Override
    public void draw(TiledTileLayer layer) {
        layerView.draw(layer, frame);
    }

    @Override
    public void draw(TiledObjectGroup layer) {
        groupView.draw(layer, frame);
    }

    @Override
    public void draw(TiledImageLayer layer) {
        imageLayerView.draw(layer, frame);
    }

    @Override
    public void draw(TiledMap tileMap, long frame) {
        this.frame += frame;
        Vector2 topLeft = camera2D.screenToVirtualCoordinates(0, 0);
        Vector2 bottomRight = camera2D.screenToVirtualCoordinates(camera2D.getWidth(), camera2D.getHeight());

        boundary.set(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(), bottomRight.getY() - topLeft.getY());

        for (TiledLayer layer : tileMap.getLayers()) {
            layer.draw(this);
        }
    }
}
