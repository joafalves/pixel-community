package org.pixel.ext.tiled.content;

public enum TiledFlipMasks {
    HORIZONTAL_FLIP_FLAG(0x80000000),
    VERTICAL_FLIP_FLAG(0x40000000),
    DIAGONAL_FLIP_FLAG(0x20000000);

    private long bits;

    TiledFlipMasks(long bits) {
        this.bits = bits;
    }

    public long getBits() {
        return bits;
    }
}
