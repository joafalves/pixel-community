package org.pixel.ext.ldtk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LdtkGameIntLayer extends LdtkGameLayer {

    private List<LayerCoordinate> layerCoordinateList;

    @Getter
    @AllArgsConstructor
    public static class LayerCoordinate {
        private int id;
        private int x;
        private int y;
        private int value;
    }
}
