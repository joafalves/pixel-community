package org.pixel.commons.data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A utility class for managing a byte array with read and write indices.
 */
public class ByteArray {
    private byte[] sourceBuffer;
    private int readerIndex;
    private int writerIndex;
    private int markIndex;

    /**
     * Default constructor. Initializes the byte array with a length of 0.
     */
    public ByteArray() {
        this(0);
    }

    /**
     * Constructor. Initializes the byte array with the specified length.
     *
     * @param length The initial length of the byte array.
     */
    public ByteArray(int length) {
        this.sourceBuffer = new byte[length];
    }

    /**
     * Constructor. Initializes the byte array with the specified byte array.
     *
     * @param byteArray The byte array to initialize with.
     */
    public ByteArray(byte[] byteArray) {
        this.sourceBuffer = Arrays.copyOf(byteArray, byteArray.length);
    }

    /**
     * Copy constructor. Initializes the byte array with another ByteArray instance.
     *
     * @param that The ByteArray instance to copy from.
     */
    public ByteArray(ByteArray that) {
        this.sourceBuffer = Arrays.copyOf(that.sourceBuffer, that.sourceBuffer.length);
        this.readerIndex = that.readerIndex;
        this.writerIndex = that.writerIndex;
        this.markIndex = that.markIndex;
    }

    /**
     * Gets the current reader index.
     *
     * @return The current reader index.
     */
    public int getReaderIndex() {
        return readerIndex;
    }

    /**
     * Resets the reader index to 0.
     */
    public void resetReaderIndex() {
        readerIndex = 0;
    }

    /**
     * Sets the reader index to the specified value.
     *
     * @param value The new reader index value.
     */
    public void setReaderIndex(int value) {
        readerIndex = value;
    }

    /**
     * Increments the reader index by the specified value.
     *
     * @param value The value to increment the reader index by.
     */
    public void incrementReaderIndex(int value) {
        readerIndex += value;
    }

    /**
     * Gets the current writer index.
     *
     * @return The current writer index.
     */
    public int getWriterIndex() {
        return writerIndex;
    }

    /**
     * Resets the writer index to 0.
     */
    public void resetWriterIndex() {
        writerIndex = 0;
    }

    /**
     * Sets the writer index to the specified value.
     *
     * @param value The new writer index value.
     */
    public void setWriterIndex(int value) {
        writerIndex = value;
    }

    /**
     * Increments the writer index by the specified value.
     *
     * @param value The value to increment the writer index by.
     */
    public void incrementWriterIndex(int value) {
        writerIndex += value;
    }

    /**
     * Gets the number of readable bytes.
     *
     * @return The number of readable bytes.
     */
    public int getReadableBytes() {
        return sourceBuffer.length - readerIndex;
    }

    /**
     * Gets the capacity of the byte array.
     *
     * @return The capacity of the byte array.
     */
    public int capacity() {
        return sourceBuffer.length;
    }

    /**
     * Resizes the byte array to the specified new length.
     *
     * @param newLength The new length of the byte array.
     */
    public void resize(int newLength) {
        if (newLength != sourceBuffer.length) {
            byte[] newBuffer = new byte[newLength];
            int copyLength = Math.min(sourceBuffer.length, newLength);
            System.arraycopy(sourceBuffer, 0, newBuffer, 0, copyLength);
            sourceBuffer = newBuffer;
            if (writerIndex > newLength) writerIndex = newLength;
            if (readerIndex > newLength) readerIndex = newLength;
        }
    }

    /**
     * Puts the specified byte array into the byte array.
     *
     * @param source The byte array to put.
     */
    public void put(byte[] source) {
        put(source, 0, source.length);
    }

    /**
     * Puts the specified byte array into the byte array with the specified offset and length.
     *
     * @param source The byte array to put.
     * @param offset The offset in the source array.
     * @param length The number of bytes to put.
     */
    public void put(byte[] source, int offset, int length) {
        if (writerIndex + length > sourceBuffer.length) {
            resize(writerIndex + length);
        }
        System.arraycopy(source, offset, sourceBuffer, writerIndex, length);
        writerIndex += length;
    }

    /**
     * Puts the specified byte into the byte array.
     *
     * @param value The byte to put.
     */
    public void put(byte value) {
        if (writerIndex + 1 > sourceBuffer.length) {
            resize(sourceBuffer.length + 1);
        }
        sourceBuffer[writerIndex++] = value;
    }

    /**
     * Puts the specified unsigned byte into the byte array.
     *
     * @param value The unsigned byte to put.
     */
    public void putUByte(int value) {
        put((byte) (value & 0xFF));
    }

    /**
     * Puts the specified integer into the byte array.
     *
     * @param value The integer to put.
     */
    public void putInt(int value) {
        byte[] b = {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified unsigned integer into the byte array.
     *
     * @param value The unsigned integer to put.
     */
    public void putUInt(long value) {
        byte[] b = {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified 24-bit unsigned integer into the byte array.
     *
     * @param value The 24-bit unsigned integer to put.
     */
    public void putUInt24(long value) {
        byte[] b = {
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified 48-bit unsigned integer into the byte array.
     *
     * @param value The 48-bit unsigned integer to put.
     */
    public void putUInt48(long value) {
        byte[] b = {
                (byte) (value >> 40),
                (byte) (value >> 32),
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified short into the byte array.
     *
     * @param value The short to put.
     */
    public void putShort(short value) {
        byte[] b = {
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified unsigned short into the byte array.
     *
     * @param value The unsigned short to put.
     */
    public void putUShort(int value) {
        byte[] b = {
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified long into the byte array.
     *
     * @param value The long to put.
     */
    public void putLong(long value) {
        byte[] b = {
                (byte) (value >> 56),
                (byte) (value >> 48),
                (byte) (value >> 40),
                (byte) (value >> 32),
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Puts the specified unsigned long into the byte array.
     *
     * @param value The unsigned long to put.
     */
    public void putULong(long value) {
        byte[] b = {
                (byte) (value >> 56),
                (byte) (value >> 48),
                (byte) (value >> 40),
                (byte) (value >> 32),
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
        put(b);
    }

    /**
     * Gets a copy of the source byte array.
     *
     * @return A copy of the source byte array.
     */
    public byte[] getSource() {
        return Arrays.copyOf(sourceBuffer, sourceBuffer.length);
    }

    /**
     * Gets the size of the byte array.
     *
     * @return The size of the byte array.
     */
    public int getSize() {
        return sourceBuffer.length;
    }

    /**
     * Marks the current reader index.
     */
    public void mark() {
        markIndex = readerIndex;
    }

    /**
     * Resets the reader index to the marked index.
     */
    public void reset() {
        readerIndex = markIndex;
    }

    /**
     * Transposes the specified buffer with the specified offset and length.
     *
     * @param buffer The buffer to transpose.
     * @param offset The offset in the buffer.
     * @param length The number of bytes to transpose.
     */
    public void transpose(byte[] buffer, int offset, int length) {
        System.arraycopy(sourceBuffer, readerIndex, buffer, offset, length);
    }

    /**
     * Slices the byte array from the current reader index to the end.
     *
     * @return A new ByteArray instance representing the slice.
     */
    public ByteArray slice() {
        return slice(readerIndex, sourceBuffer.length - readerIndex);
    }

    /**
     * Slices the byte array from the specified start index with the specified length.
     *
     * @param start  The start index.
     * @param length The length of the slice.
     * @return A new ByteArray instance representing the slice.
     */
    public ByteArray slice(int start, int length) {
        byte[] newBuffer = new byte[length];
        System.arraycopy(sourceBuffer, start, newBuffer, 0, length);
        return new ByteArray(newBuffer);
    }

    /**
     * Reads a byte from the current reader index and increments the reader index.
     *
     * @return The byte read.
     */
    public byte read() {
        byte value = get(readerIndex);
        readerIndex++;
        return value;
    }

    /**
     * Reads the specified number of bytes from the current reader index and increments the reader index.
     *
     * @param length The number of bytes to read.
     * @return The bytes read.
     */
    public byte[] read(int length) {
        byte[] value = new byte[length];
        System.arraycopy(sourceBuffer, readerIndex, value, 0, length);
        readerIndex += length;
        return value;
    }

    /**
     * Reads an unsigned byte from the current reader index and increments the reader index.
     *
     * @return The unsigned byte read.
     */
    public int readUByte() {
        int value = getUByte(readerIndex);
        readerIndex++;
        return value;
    }

    /**
     * Gets a byte at the specified index.
     *
     * @param index The index to get the byte from.
     * @return The byte at the specified index.
     */
    public byte get(int index) {
        checkIndex(index);
        return sourceBuffer[index];
    }

    /**
     * Gets an unsigned byte at the specified index.
     *
     * @param index The index to get the unsigned byte from.
     * @return The unsigned byte at the specified index.
     */
    public int getUByte(int index) {
        checkIndex(index);
        return sourceBuffer[index] & 0xFF;
    }

    /**
     * Reads an integer from the current reader index and increments the reader index.
     *
     * @return The integer read.
     */
    public int readInt() {
        int value = getInt(readerIndex);
        readerIndex += 4;
        return value;
    }

    /**
     * Gets an integer at the specified index.
     *
     * @param index The index to get the integer from.
     * @return The integer at the specified index.
     */
    public int getInt(int index) {
        checkIndex(index + 3);
        return (sourceBuffer[index] & 0xFF) << 24
                | (sourceBuffer[index + 1] & 0xFF) << 16
                | (sourceBuffer[index + 2] & 0xFF) << 8
                | (sourceBuffer[index + 3] & 0xFF);
    }

    /**
     * Reads an unsigned integer from the current reader index and increments the reader index.
     *
     * @return The unsigned integer read.
     */
    public long readUInt() {
        long value = getUInt(readerIndex);
        readerIndex += 4;
        return value;
    }

    /**
     * Gets an unsigned integer at the specified index.
     *
     * @param index The index to get the unsigned integer from.
     * @return The unsigned integer at the specified index.
     */
    public long getUInt(int index) {
        checkIndex(index + 3);
        return ((long) (sourceBuffer[index] & 0xFF) << 24)
                | ((sourceBuffer[index + 1] & 0xFF) << 16)
                | ((sourceBuffer[index + 2] & 0xFF) << 8)
                | (sourceBuffer[index + 3] & 0xFF);
    }

    /**
     * Reads a short from the current reader index and increments the reader index.
     *
     * @return The short read.
     */
    public short readShort() {
        short value = getShort(readerIndex);
        readerIndex += 2;
        return value;
    }

    /**
     * Gets a short at the specified index.
     *
     * @param index The index to get the short from.
     * @return The short at the specified index.
     */
    public short getShort(int index) {
        checkIndex(index + 1);
        return (short) ((sourceBuffer[index] & 0xFF) << 8
                | (sourceBuffer[index + 1] & 0xFF));
    }

    /**
     * Reads an unsigned short from the current reader index and increments the reader index.
     *
     * @return The unsigned short read.
     */
    public int readUShort() {
        int value = getUShort(readerIndex);
        readerIndex += 2;
        return value;
    }

    /**
     * Gets an unsigned short at the specified index.
     *
     * @param index The index to get the unsigned short from.
     * @return The unsigned short at the specified index.
     */
    public int getUShort(int index) {
        checkIndex(index + 1);
        return (sourceBuffer[index] & 0xFF) << 8
                | (sourceBuffer[index + 1] & 0xFF);
    }

    /**
     * Decodes the byte array to a string using UTF-8 encoding.
     *
     * @return The decoded string.
     */
    public String decodeToString() {
        return new String(sourceBuffer, StandardCharsets.UTF_8);
    }

    /**
     * Reads a 24-bit unsigned integer from the current reader index and increments the reader index.
     *
     * @return The 24-bit unsigned integer read.
     */
    public long readUInt24() {
        long value = getUInt24(readerIndex);
        readerIndex += 3;
        return value;
    }

    /**
     * Gets a 24-bit unsigned integer at the specified index.
     *
     * @param index The index to get the 24-bit unsigned integer from.
     * @return The 24-bit unsigned integer at the specified index.
     */
    public long getUInt24(int index) {
        checkIndex(index + 2);
        return ((long) (sourceBuffer[index] & 0xFF) << 16)
                | ((sourceBuffer[index + 1] & 0xFF) << 8)
                | (sourceBuffer[index + 2] & 0xFF);
    }

    /**
     * Reads a 48-bit unsigned integer from the current reader index and increments the reader index.
     *
     * @return The 48-bit unsigned integer read.
     */
    public long readUInt48() {
        long value = getUInt48(readerIndex);
        readerIndex += 6;
        return value;
    }

    /**
     * Gets a 48-bit unsigned integer at the specified index.
     *
     * @param index The index to get the 48-bit unsigned integer from.
     * @return The 48-bit unsigned integer at the specified index.
     */
    public long getUInt48(int index) {
        checkIndex(index + 5);
        return ((long) (sourceBuffer[index] & 0xFF) << 40)
                | ((long) (sourceBuffer[index + 1] & 0xFF) << 32)
                | ((long) (sourceBuffer[index + 2] & 0xFF) << 24)
                | ((long) (sourceBuffer[index + 3] & 0xFF) << 16)
                | ((long) (sourceBuffer[index + 4] & 0xFF) << 8)
                | (sourceBuffer[index + 5] & 0xFF);
    }

    /**
     * Checks if the specified index is within the bounds of the byte array.
     *
     * @param index The index to check.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= sourceBuffer.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + sourceBuffer.length);
        }
    }
}