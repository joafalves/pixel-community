/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons;

import java.nio.charset.Charset;

public class DataBuffer {

    private byte[] source;
    private int offset;
    private int length;
    private int readerIndex;

    /**
     * Constructor.
     */
    public DataBuffer() {
        this.set(null, 0, 0);
    }

    /**
     * Constructor.
     *
     * @param that The other buffer.
     */
    public DataBuffer(DataBuffer that) {
        this.set(that.source, that.offset, that.length);
    }

    /**
     * Constructor.
     *
     * @param source The source buffer.
     */
    public DataBuffer(byte[] source) {
        this.set(source, 0, 0);
    }

    /**
     * Constructor.
     *
     * @param source The source buffer.
     * @param offset The source buffer offset.
     * @param length The length of the data to be read.
     */
    public DataBuffer(byte[] source, int offset, int length) {
        this.set(source, offset, length);
    }

    /**
     * Constructor.
     *
     * @param source The source buffer.
     */
    public DataBuffer(String source) {
        this.set(source.getBytes(), 0, source.length());
    }

    /**
     * Set byte buffer data by given offset and length.
     *
     * @param source The source buffer.
     * @param offset The source buffer offset.
     * @param length The length of the data to be read.
     */
    public void set(byte[] source, int offset, int length) {
        this.source = source;
        this.offset = offset;
        this.length = length;
        this.readerIndex = 0;
    }

    /**
     * Reset the reader index.
     */
    public void resetReaderIndex() {
        this.readerIndex = 0;
    }

    /**
     * Reset offset, length and reader index. No modifications are applied to source buffer.
     */
    public void reset() {
        this.offset = 0;
        this.length = 0;
        this.readerIndex = 0;
    }

    /**
     * Set reader index.
     *
     * @param value The new reader index.
     */
    public void setReaderIndex(int value) {
        this.readerIndex = value;
    }

    /**
     * Read a byte from the source buffer starting on the reader index.
     *
     * @return The next byte.
     */
    public byte readByte() {
        return this.source[offset + readerIndex++]; // note: ++ after is important in this scenario, dont change!
    }

    /**
     * Peek the next byte (does not change reader index).
     *
     * @return The next byte.
     */
    public byte peekByte() {
        return this.source[offset + readerIndex];
    }

    /**
     * Peek the next byte at given position (does not change reader index).
     *
     * @return The next byte.
     */
    public byte peekByte(int pos) {
        return this.source[offset + readerIndex + pos];
    }

    /**
     * Read unsigned int from the source buffer starting on the reader index
     *
     * @return The next unsigned int.
     */
    public long readUnsignedInt() {
        return readLong(4);
    }

    /**
     * Get the number of bytes that can be read from the source buffer starting on the reader index.
     *
     * @param size The number of bytes to read.
     * @return The next bytes encapsulated in a DataBuffer.
     */
    public DataBuffer getBytes(int size) {
        //checkOverflow(size);
        return new DataBuffer(source, offset + readerIndex, size);
    }

    /**
     * Get the number of readable bytes.
     *
     * @return The number of readable bytes.
     */
    public int getReadableBytes() {
        return this.length - this.readerIndex;
    }

    /**
     * Get the number of available bytes after the readable bytes amount.
     *
     * @return The number of available bytes.
     */
    public int getUnassignedBytes() {
        return this.source.length - (this.offset + this.length);
    }

    /**
     * Get the assignable content of the buffer as a String.
     *
     * @return The content of the buffer as a String.
     */
    public String getReadableText() {
        return new String(source, offset, length, Charset.defaultCharset());
    }

    /**
     * Resize current source buffer (overflow bytes are clipped).
     *
     * @param length The new length of the buffer.
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
     * @param source Source buffer to append.
     * @param offset Offset of the source buffer.
     * @param length The length of the data to be read.
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
     * Append given byte buffer to the end of the current source (considers current length and offset values).
     *
     * @param value Byte buffer to append.
     */
    public void append(DataBuffer value) {
        this.append(value.getSource(), value.getOffset(), value.getLength());
    }

    /**
     * Append given byte to the end of the current source (considers current length and offset values).
     *
     * @param value Byte to append.
     */
    public void append(byte value) {
        if (this.offset + this.length + 1 > this.source.length) {
            this.resize(source.length + 1);
        }

        this.source[this.offset + this.length] = value;
        this.length += 1;
    }

    /**
     * Peek the next bytes at the reader index position.
     *
     * @param bytes The number of bytes to peek.
     * @return The next bytes.
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
     * Read a long value from the source buffer starting on the reader index.
     *
     * @param bytes The number of bytes to read.
     * @return The next long value.
     */
    public synchronized long readLong(int bytes) {
        long result = peekLong(bytes);
        readerIndex += bytes;

        return result;
    }

    /**
     * Peek the next int value at the reader index position.
     *
     * @param bytes The number of bytes to peek.
     * @return The next int value.
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
     * Read an int value from the source buffer starting on the reader index.
     *
     * @param bytes The number of bytes to read.
     * @return The next int value.
     */
    public synchronized int readInt(int bytes) {
        int result = peekInt(bytes);
        readerIndex += bytes;

        return result;
    }

    /**
     * Get the length of the buffer.
     *
     * @return The length of the buffer.
     */
    public int getLength() {
        return length;
    }

    /**
     * Set the length of the buffer.
     *
     * @param value The length of the buffer.
     */
    public void setLength(int value) {
        this.length = value;
    }

    /**
     * Set the offset of the buffer.
     *
     * @param value The offset of the buffer.
     */
    public void setOffset(int value) {
        this.offset = value;
    }

    /**
     * Increases current reader index by given value.
     *
     * @param value The value to increase the reader index by.
     */
    public void increaseReaderIndex(int value) {
        this.readerIndex += value;

        if (this.readerIndex > this.length) {
            this.readerIndex = this.length;
        }
    }

    /**
     * Decreases current reader index by given value.
     *
     * @param value The value to decrease the reader index by.
     */
    public void decreaseReaderIndex(int value) {
        this.readerIndex += value;

        if (this.readerIndex < 0) {
            this.readerIndex = 0;
        }
    }

    /**
     * Increases current offset by given value.
     *
     * @param value The value to increase the offset by.
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
     * Decreases current offset by given value.
     *
     * @param value The value to decrease the offset by.
     */
    public void decreaseOffset(int value) {
        this.offset += value;

        if (this.offset < 0) {
            this.offset = 0;
        }
    }

    /**
     * Get the source buffer length.
     *
     * @return The source buffer length.
     */
    public int getSourceLength() {
        if (source == null) {
            return 0;
        }
        return source.length;
    }

    /**
     * Get the reader index.
     *
     * @return The reader index.
     */
    public int getReaderIndex() {
        return readerIndex;
    }

    /**
     * Get the source buffer bytes.
     *
     * @return The source buffer bytes.
     */
    public byte[] getSource() {
        return source;
    }

    /**
     * Get the source buffer offset.
     *
     * @return The source buffer offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Check buffer overflow.
     *
     * @param size The size to check.
     */
    private void checkOverflow(int size) {
        if (readerIndex > offset + length + size) {
            throw new RuntimeException("Buffer overflow");
        }
    }
}
