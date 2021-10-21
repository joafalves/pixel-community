package org.pixel.tiled.content;

public enum TiledConstants {
    HORIZONTAL_FLIP_FLAG(0x80000000),
    VERTICAL_FLIP_FLAG(0x40000000),
    DIAGONAL_FLIP_FLAG(0x20000000);

    long bits;

    TiledConstants(long bits) {
        this.bits = bits;
    }

    public long getBits() {
        return bits;
    }
}
