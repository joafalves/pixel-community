package org.pixel.ext.ldtk;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LdtkGameIntLayer extends LdtkGameLayer {

    private List<Coordinate> coordinateList;

    @Getter
    @AllArgsConstructor
    public static class Coordinate {
        private int id;
        private int x;
        private int y;
        private int value;
    }
}
