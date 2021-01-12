/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons;

import java.nio.charset.Charset;

/**
 * @author João Filipe Alves
 */
public class DataBuffer {

    private byte[] source;
    private int offset;
    private int length;
    private int readerIndex;

    /**
     * Constructor
     */
    public DataBuffer() {
        this.set(null, 0, 0);
    }

    /**
     * Constructor
     *
     * @param that
     */
    public DataBuffer(DataBuffer that) {
        this.set(that.source, that.offset, that.length);
    }

    /**
     * Constructor
     *
     * @param source
     */
    public DataBuffer(byte[] source) {
        this.set(source, 0, 0);
    }

    /**
     * Constructor
     *
     * @param source
     * @param offset
     * @param length
     */
    public DataBuffer(byte[] source, int offset, int length) {
        this.set(source, offset, length);
    }

    /**
     * Constructor
     *
     * @param source
     */
    public DataBuffer(String source) {
        this.set(source.getBytes(), 0, source.length());
    }

    /**
     * Set byte buffer params
     *
     * @param source
     * @param offset
     * @param length
     */
    public void set(byte[] source, int offset, int length) {
        this.source = source;
        this.offset = offset;
        this.length = length;
        this.readerIndex = 0;
    }

    /**
     * Reset the reader index
     */
    public void resetReaderIndex() {
        this.readerIndex = 0;
    }

    /**
     * Reset offset, length and reader index. No modifications are applied to source buffer
     */
    public void reset() {
        this.offset = 0;
        this.length = 0;
        this.readerIndex = 0;
    }

    /**
     * Set reader index
     *
     * @param value
     */
    public void setReaderIndex(int value) {
        this.readerIndex = value;
    }

    /**
     * Read the next byte
     *
     * @return
     */
    public byte readByte() {
        return this.source[offset + readerIndex++]; // note: ++ after is important in this scenario, dont change!
    }

    /**
     * Peek the next byte (does not change reader index)
     *
     * @return
     */
    public byte peekByte() {
        return this.source[offset + readerIndex];
    }

    /**
     * Peek the next byte at given position (does not change reader index)
     *
     * @return
     */
    public byte peekByte(int pos) {
        return this.source[offset + readerIndex + pos];
    }

    /**
     * Read unsigned int
     *
     * @return
     */
    public long readUnsignedInt() {
        return readLong(4);
    }

    /**
     * Get 'size' bytes of from the source buffer starting on the reader index
     *
     * @param size
     * @return
     */
    public DataBuffer getBytes(int size) {
        //checkOverflow(size);
        return new DataBuffer(source, offset + readerIndex, size);
    }

    /**
     * Get the number of readable bytes
     *
     * @return
     */
    public int getReadableBytes() {
        return this.length - this.readerIndex;
    }

    /**
     * Get the number of available bytes after the readable bytes amount
     *
     * @return
     */
    public int getUnassignedBytes() {
        return this.source.length - (this.offset + this.length);
    }

    /**
     * Get readable org.pixel.text
     *
     * @return
     */
    public String getReadableText() {
        return new String(source, offset, length, Charset.defaultCharset());
    }

    /**
     * Resize current source buffer (overflow bytes are clipped)
     *
     * @param length
     */
    public void resize(int length) {
        if (length == this.source.length) {
            return; // nothing to do, already this size!
        }

        int copyLength = Math.min(length, source.length);
        byte[] buffer = new byte[length];
        System.arraycopy(this.source, 0, buffer, 0, copyLength);

        this.source = buffer;
    }

    /**
     * Appends given bytes to the end of the current source (considers current length and offset values).
     *
     * @param source source of data
     * @param offset offset of the source data
     * @param length length of the source data to obtain
     */
    public void append(byte[] source, int offset, int length) {
        if (this.offset + this.length + length > this.source.length) {
            int delta = length - (this.source.length - this.offset + this.length);
            this.resize(this.source.length + delta);
        }

        System.arraycopy(source, offset, this.source, this.offset + this.length, length);
        this.length += length;
    }

    /**
     * Append given byte buffer
     *
     * @param value
     */
    public void append(DataBuffer value) {
        this.append(value.getSource(), value.getOffset(), value.getLength());
    }

    /**
     * Append given byte
     *
     * @param value
     */
    public void append(byte value) {
        if (this.offset + this.length + 1 > this.source.length) {
            this.resize(source.length + 1);
        }

        this.source[this.offset + this.length] = value;
        this.length += 1;
    }

    /**
     * Peek the next bytes (does not change reader index)
     *
     * @param bytes
     * @return
     */
    public long peekLong(int bytes) {
        long result = 0;
        for (int i = readerIndex; i < readerIndex + bytes; ++i) {
            result <<= Byte.SIZE;
            result |= (source[offset + i] & 0xFF);
        }

        return result;
    }

    /**
     * Read long from source buffer
     *
     * @param bytes
     * @return
     */
    public synchronized long readLong(int bytes) {
        long result = peekLong(bytes);
        readerIndex += bytes;

        return result;
    }

    /**
     * Peek the next bytes (does not change reader index)
     *
     * @param bytes
     * @return
     */
    public int peekInt(int bytes) {
        int result = 0;
        for (int i = readerIndex; i < readerIndex + bytes; ++i) {
            result <<= Byte.SIZE;
            result |= (source[offset + i] & 0xFF);
        }

        return result;
    }

    /**
     * Read int from source buffer
     *
     * @param bytes
     * @return
     */
    public synchronized int readInt(int bytes) {
        int result = peekInt(bytes);
        readerIndex += bytes;

        return result;
    }

    /**
     * @param size
     */
    private void checkOverflow(int size) {
        if (readerIndex > offset + length + size) {
            throw new RuntimeException("Buffer overflow");
        }
    }

    /**
     * @return
     */
    public int getLength() {
        return length;
    }

    /**
     * @param value
     */
    public void setLength(int value) {
        this.length = value;
    }

    /**
     * @param value
     */
    public void setOffset(int value) {
        this.offset = value;
    }

    /**
     * Increases current reader index by given value
     *
     * @param value
     */
    public void increaseReaderIndex(int value) {
        this.readerIndex += value;

        if (this.readerIndex > this.length) {
            this.readerIndex = this.length;
        }
    }

    /**
     * Decreases current reader index by given value
     *
     * @param value
     */
    public void decreaseReaderIndex(int value) {
        this.readerIndex += value;

        if (this.readerIndex < 0) {
            this.readerIndex = 0;
        }
    }

    /**
     * Increases current offset by given value
     *
     * @param value
     */
    public void increaseOffset(int value) {
        this.offset += value;
        if (value <= length) {
            this.length -= value;

        } else {
            this.length = 0;
        }

        if (this.offset > this.source.length) {
            this.offset = this.source.length; // clip values!
        }
    }

    /**
     * Decreases current offset by given value
     *
     * @param value
     */
    public void decreaseOffset(int value) {
        this.offset += value;

        if (this.offset < 0) {
            this.offset = 0;
        }
    }

    /**
     * @return
     */
    public int getSourceLength() {
        if (source == null) return 0;
        return source.length;
    }

    /**
     * @return
     */
    public int getReaderIndex() {
        return readerIndex;
    }

    public byte[] getSource() {
        return source;
    }

    public int getOffset() {
        return offset;
    }
}
