/* 
 *  Copyright © 2025 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.Clipboard;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class CharsetCompatibilityChecker {

    // UTF-16 and 32 BOM (Big-Endian: 0xFE 0xFF, Little-Endian: 0xFF 0xFE)
    /*
    private static final char BYTE_ORDER_MARK = '\uFEFF';
    private static final char REVERSED_BYTE_ORDER_MARK = '\uFFFE';
     */
    /**
     * Byte Order Mark (BOM) for UTF-16 and UTF-32
     */
    private static final byte[] BYTE_ORDER_MARK_BYTES = {(byte) 0xFE, (byte) 0xFF};
    /**
     * ASCII_PLUS is a character set that is compatible with ASCII and can be
     * used to encode Base64 characters. It is a super set of ASCII.
     */
    public static final Charset ASCII_PLUS = StandardCharsets.ISO_8859_1;
    /**
     * RFC 4648 Table 1 and Table 2 Base64 Alphabet. this is a mapping of the
     * base64 characters that are valid for encoding and decoding Base64
     * strings. we don't have to care for its numeric value just that it is a
     * valid character
     */
    private static final char[] toBase64Map = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '+', '/', '-', '_', '='
    };

    /**
     * Checks if the given Character set is compatible with ASCII.
     *
     * @param charset The Character set to check.
     * @return True if the Character set is compatible with ASCII, false
     * otherwise.
     */
    public static boolean isAsciiByteCompatible(Charset charset) {
        CharsetEncoder encoder = charset.newEncoder();
        for (int i = 0; i < 0x7F; i++) { // ASCII range
            char c = (char) i;
            if (!encoder.canEncode(c)) {
                // If the character cannot be encoded, it's not ASCII-compatible
                return false;
            }
            byte[] encodedBytes = String.valueOf(c).getBytes(charset);
            if (encodedBytes.length != 1 || (encodedBytes[0] & 0xFF) != i) {
                // If the encoding doesn't match the ASCII byte value, it's not compatible
                return false;
            }
        }
        return true;
    }

    /**
     * this method checks if the given Character set and the specified
     * character map can be represented with the same numeric value as the ASCII
     * character set.
     *
     * for example. on UTF-32 the character 'A' is represented as 0x00000041, on
     * ascii it is represented as 0x41, thus the numeric value is the same.
     * the character 'A' is represented as 0x0041 on UTF-16, which is also the same
     * thus the difference is the byte size and the byte order(endianness)
     *
     * so for we check that all the characters in the given Map can be encoded
     * and that the encoded bytes match the expected values.
     *
     * @param checkMap The character map to check against the Character set.
     * @param charset The Character set to check.
     * @return True if the Character set is compatible with ASCII, false
     * otherwise.
     */
    public static boolean isAsciiNumericCompatible(Charset charset, final char checkMap[]) {
        CharsetEncoder encoder = charset.newEncoder();
        for (char c : checkMap) {
            if (!encoder.canEncode(c)) {
                return false;
            }
            byte[] encodedBytes = String.valueOf(c).getBytes(charset);
            var bigEdian = RemoveBOM(charset, encodedBytes);
            for (int index = bigEdian ? 0 : 1; index < encodedBytes.length - (bigEdian ? 1 : 0); index++) {
                //other byte besides the first one on this character has a value that is not 0
                //thus does not match the criteria.
                if (encodedBytes[index] != 0x0) {
                    return false;
                }
            }
            if ((encodedBytes[bigEdian ? encodedBytes.length - 1 : 0] & 0xFF) != (int) c) {
                // If the encoding doesn't match the ASCII byte value, it's not compatible
                return false;
            }
        }
        // All characters in the map are compatible with the Character set
        return true;
    }

    /**
     * removes the BOM(Byte order Mark) from the encoded bytes (if present) and
     * returns if the character set enconding is big Endian or not.
     *
     * @param charset the Character set to test.
     * @param encoded the encoded bytes to review
     * @return true if is big Endian false otherwise
     */
    public static boolean RemoveBOM(Charset charset, byte[] encoded) {
        Objects.requireNonNull(encoded);
        if (encoded.length <= 1) {
            return false;
        }
        // Check for BOM patterns
        if (encoded.length >= 2) {
            if ((encoded[0] == BYTE_ORDER_MARK_BYTES[0] && encoded[1] == BYTE_ORDER_MARK_BYTES[1])) {
                encoded[0] = 0x0;
                encoded[1] = 0x0;
                return true;
            } else if ((encoded[0] == BYTE_ORDER_MARK_BYTES[1] && encoded[1] == BYTE_ORDER_MARK_BYTES[0])) {
                encoded[0] = 0x0;
                encoded[1] = 0x0;
                return false;
            }
        }
        if (encoded.length >= 4) {
            if ((encoded[0] == (byte) 0x00 && encoded[1] == (byte) 0x00
                    && encoded[2] == BYTE_ORDER_MARK_BYTES[0] && encoded[3] == BYTE_ORDER_MARK_BYTES[1])) {
                encoded[2] = 0x0;
                encoded[3] = 0x0;
                return true;
            //i think this should be tackled on the previous if [the encoded.length >= 2 else if] as the order is the same and the bytes are
            //in the same position...
            } else if ((encoded[0] == BYTE_ORDER_MARK_BYTES[1] && encoded[1] == BYTE_ORDER_MARK_BYTES[0]
                    && encoded[2] == (byte) 0x00 && encoded[3] == (byte) 0x00)) {
                encoded[0] = 0x0;
                encoded[1] = 0x0;
                return false;
            }
        }
        //dont know yet the endianess...
        String name = charset.name();
        if (name.contains("BE")) {
            return true;
        } else if (name.contains("LE")) {
            return false;
        }
        //uhhg... lets try Assuming the bytes provided only contains ONE significat byte.
        //we should not be looking for the first non "0" character after 128 bits
        //~(32 single byte characters and up to 8 32 utf-32 ). is a waste
        //and the caller is a troll
        var limitloop=encoded.length >16 ? 16 : encoded.length;
        for (int i = 0; i < limitloop ; i++) {
            if (encoded[i] != (byte) 0x00) {
                if (i == 0) {
                    return false;
                } else if (i == encoded.length - 1) {
                    return true;
                }
            }
        }
        return false; //hmmmm
    }

    /**
     * Checks if the given Character set is compatible with ASCII and specifically
     * with the Base64 character.
     * Base64 is a binary-to-text encoding scheme that represents binary data
     * that is encoded in an ASCII character strings
     *
     * This method checks if the given Character set is
     * <Strong>Congruent</Strong>
     * and or <Strong>Compatible</Strong> with Base64 by verifying if it can
     * encode the ASCII character that represent a Base64 String have the same 
     * numeric values (A-Z, a-z, 0-9, -, _, +, /, =).
     *
     * for example the character set "UTF-8" is compatible with Base64, as it
     * can encode the Base64 characters and the encoded bytes on a 1:1 mapping
     *
     * now on Character sets such as "UTF-16" and "UTF-32". the characters are
     * encoded in a different way, and the bytes are not in the same order
     * as the ASCII character set.
     * for example. on UTF-32 the character 'A' is represented as 0x00000041 vs
     * on ascii it is represented as 0x41, thus the numeric value is the same.
     * however it contains 4 bytes, and the byte order is(or might be) different.
     * so usually process that look for "equality" will fail. as it is congruent but
     * not a 1:1 mapping.
     *
     * thus thims method checks that all the characters in the given Map its integer
     * value is the same as the ASCII character set. using the same previous example
     * the character 'A' is represented as 0x0041 on UTF-16, which is also the same
     *  UTF-32 the character 'A' is represented as 0x00000041 vs
     *
     * @param charset the character set to check for congruency with Base64 And
     * ASCII
     * @return true if the characters that are used in Base64 can be encoded
     * congruently
     */
    public static boolean charsetCompatibleWithBase64(Charset charset) {
        return isAsciiNumericCompatible(charset, toBase64Map);
    }

}
