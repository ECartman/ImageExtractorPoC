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

public class CharsetCompatibilityChecker {
    // UTF-16 and 32 BOM (Big-Endian: 0xFE 0xFF, Little-Endian: 0xFF 0xFE)
    /*
    private static final char BYTE_ORDER_MARK = '\uFEFF';
    private static final char REVERSED_BYTE_ORDER_MARK = '\uFFFE';
    */
    //space is plentiful we require less computation here. thus 
    private static final byte[] BYTE_ORDER_MARK_BYTES = {(byte) 0xFE, (byte) 0xFF};
    public static final Charset ASCII_PLUS = StandardCharsets.ISO_8859_1;
    /**
     * RFC 4648 Table 1: The Base 64 Alphabet Value Encoding Value Encoding
     * Value Encoding Value Encoding Table 2: The "URL and Filename safe" Base
     * 64 Alphabet Value Encoding Value Encoding Value Encoding Value Encoding
     *
     * This array is a lookup table that translates 6-bit positive integer index
     * values into their "Base64 Alphabet" and BASE64_URL equivalents as
     * specified in "Table 1 and 2: The Base64 Alphabet" of RFC 4648
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
     * @return True if the Character set is compatible with ASCII, false otherwise.
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
        return true; // All ASCII characters are compatible
    }

    /**
     * Checks if the given Character set is compatible with ASCII.
     *
     * @param charset The Character set to check.
     * @return True if the Character set is compatible with ASCII, false otherwise.
     */
    public static boolean isAsciiNumericCompatible(Charset charset, final char checkMap[]) {
        CharsetEncoder encoder = charset.newEncoder();
        for (char c : checkMap) {
            if (!encoder.canEncode(c)) {
                return false;
            }
            byte[] encodedBytes = String.valueOf(c).getBytes(charset);
            var bigEdian = RemoveBOM(charset, encodedBytes);
            for(int index=bigEdian?0:1;index<encodedBytes.length-(bigEdian?1:0);index++){
                //other byte besides the first one on this character has a value that is not 0
                //thus does not match the criteria.
                if(encodedBytes[index] != 0x0){
                    return false;
                }
            }
            if ((encodedBytes[bigEdian?encodedBytes.length-1:0] & 0xFF) != (int) c) {
                // If the encoding doesn't match the ASCII byte value, it's not compatible
                return false;
            }
        }
        return true; // All ASCII characters are compatible
    }

    /**
     * check if the Character set name defines if it is big or little Endian
     * @param charset the Character set to test.
     * @return true if big Endian, false otherwise 
     */
    public static boolean isBigEndian(Charset charset) {
        String name = charset.name();
        if (name.contains("BE")) {
            return true;
        } else if (name.contains("LE")) {
           return false;
        } 
       //dunno. might want to throw a error instead? 
        return false;
    }

    /**
     * removes the BYTE ORDER MARK if it has one. at the first character of the 
     * sequence. 
     * @param charset the Character set to test.
     * @param encoded the encoded bytes to review
     * @return true if is big Endian false otherwise
     */
    public static boolean RemoveBOM(Charset charset, byte[] encoded) {
        Objects.requireNonNull(encoded);
        if (encoded.length <= 0) {
            throw new IllegalArgumentException("the array is invalid");
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
            } else if ((encoded[0] == BYTE_ORDER_MARK_BYTES[1] && encoded[1] == BYTE_ORDER_MARK_BYTES[0]
                    && encoded[2] == (byte) 0x00 && encoded[3] == (byte) 0x00)) {
                encoded[0] = 0x0;
                encoded[1] = 0x0;
                return false;
            }
        }
        return isBigEndian(charset); // No BOM detected
    }

    public static boolean charsetCompatibleWithBase64(Charset charset) {
        //base64 is only compatible with single byte character sets that is 
        //congruent with ASCII and ISO-8859-1 for example UTF-8 is Congruent
        //meaning that A-Za-z0-9 (and a few other characters match the same
        //NUMERIC mapping) nhowever we will not check for character size but
        //rather the underline value compatibility meaning 
        return isAsciiNumericCompatible(charset, toBase64Map);
    }

    /*
    public static void main(String[] args) {
        // Test with some common charsets
        Charset utf8 = Charset.forName("UTF-8");
        Charset utf16 = Charset.forName("UTF-16");
        Charset utf16be = Charset.forName("UTF-16BE");
        Charset utf16le = Charset.forName("UTF-16LE");
        Charset iso88591 = Charset.forName("ISO-8859-1");
        Charset windows1252 = Charset.forName("Windows-1252");
        Charset UTF32 = Charset.forName("UTF-32");

        System.out.println("UTF-8 is ASCII-compatible: " + charsetCompatibleWithBase64(utf8));
        System.out.println("UTF-16 is ASCII-compatible: " + charsetCompatibleWithBase64(utf16));
        System.out.println("UTF-16BE is ASCII-compatible: " + charsetCompatibleWithBase64(utf16be));
        System.out.println("UTF-16LE is ASCII-compatible: " + charsetCompatibleWithBase64(utf16le));
        System.out.println("ISO-8859-1 is ASCII-compatible: " + charsetCompatibleWithBase64(iso88591));
        System.out.println("Windows-1252 is ASCII-compatible: " + charsetCompatibleWithBase64(windows1252));
        System.out.println("UTF32 is ASCII-compatible: " + charsetCompatibleWithBase64(UTF32));
    }*/
}
