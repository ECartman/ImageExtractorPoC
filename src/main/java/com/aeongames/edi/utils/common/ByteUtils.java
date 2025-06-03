/*
 *   Copyright © 2018,2025 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 * 
 */
package com.aeongames.edi.utils.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Eduardo Vindas <cartman>
 */
public class ByteUtils {

    /**
     * Reverses the order of the bytes in the given byte array.
     * This method modifies the original array in place.
     * 
     * @param data the byte array to reverse
     * @return the reversed byte array
     */
    public static byte[] reverse(final byte[] data) {
        for (int i = 0, j = data.length - 1; i < data.length / 2; i++, j--) {
            data[i] ^= data[j];
            data[j] ^= data[i];
            data[i] ^= data[j];
        }
        return data;
    }

    /**
     * Converts a byte array to a hexadecimal string representation.
     * 
     * @param Array
     * @return the hexadecimal string representation of the byte array
     */
    public static String byteArrayToString(final byte Array[]){
        Objects.requireNonNull(Array,"Invalid Byte Array");
        StringBuilder hexString = new StringBuilder(); // This will contain hash as hexidecimal
        for (byte byte_data:Array) {
            String hex = Integer.toHexString(0xff & byte_data);//to unsigned int
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Converts a ByteBuffer array to a hexadecimal string representation.
     * @param buffer the buffer to convert
     * @return the hexadecimal string representation of the byte array
     */
    public static String byteArrayToString(final ByteBuffer buffer){
        Objects.requireNonNull(buffer,"Invalid Byte Array");
        buffer.rewind();
        int num=buffer.remaining();
        byte values[]= new byte[num];
        buffer.get(values);
        buffer.rewind();
        return byteArrayToString(values);
    }

    /**
     * converts the String that represent a Hex value into a byte array
     * TODO: this function does not convert the value in order (*meaning from right to left)
     * rather left to right. 
     * @param hex
     * @return 
     */
    public static byte[] hexToBytes(String hex) {
        Objects.requireNonNull(hex, "the hex value cannot be null");
       final byte values[] = new byte[hex.length()/2];
        for (int i = 0; i < values.length; i++) {
            int index = i * 2;
            int j = Integer.parseUnsignedInt(hex.substring(index, index + 2), 16);
            values[i] = (byte) j;
        }
        return values;
    }
    
    /**
     * wraps the provided byte to a byteBuffer.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(final byte data) {
        return ByteBuffer.allocate(1).put(data).asReadOnlyBuffer();
    }
    
    /**
     * wraps the data provided into a byteBuffer. 
     * please not that the ByteBuffer does NOT copy the data. make sure the 
     * provided byte array is immutable. otherwise use toSafeByteBuff
     * @param data
     * @return a ReadOnly ByteBuffer that wraps the provided data 
     */
    public static final ByteBuffer toByteBuff(final byte[] data) {
        return ByteBuffer.allocate(data.length).put(data).asReadOnlyBuffer();
    }
    /**
     * returns a ReadOnly ByteBuffer that stores a <Strong>immutable COPY</strong> of the
     * provided array. please note that this might impact performance as it needs 
     * to do a memory copy of the provided data. 
     * @param data the data to copy and wrap into a ByteBuffer.
     * @return (immutable) ReadOnly ByteBuffer that contains a copy of the provided data.
     */
    public static final ByteBuffer toSafeByteBuff(byte[] data){
        final byte[] immutablearray= Arrays.copyOf(data, data.length);
        return toByteBuff(immutablearray);
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(final short data) {
        return ByteBuffer.allocate(2).putShort(data).asReadOnlyBuffer();
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(char data) {
        return ByteBuffer.allocate(2).putChar(data).asReadOnlyBuffer();
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(int data) {
        return ByteBuffer.allocate(4).putInt(data).asReadOnlyBuffer();
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(long data) {
        return ByteBuffer.allocate(8).putLong(data).asReadOnlyBuffer();
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(float data) {
        return ByteBuffer.allocate(4).putFloat(data).asReadOnlyBuffer();
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(double data) {
        return ByteBuffer.allocate(8).putDouble(data).asReadOnlyBuffer();
    }

    /**
     * creates an array that contains the provided data.
     *
     * @param data the data that will be stored in the array
     * @return the array contained the value.
     */
    public static final ByteBuffer toByteBuff(boolean data) {
        return ByteBuffer.allocate(1).putDouble(data ? 0x01 : 0x00).asReadOnlyBuffer();
    }

}