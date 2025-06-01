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
 * 
 */
package com.aeongames.edi.utils.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

/**
 * a class that defines a Input stream that skip a certain amount of bytes when
 * reading from it. this is useful so for example we read data that is formatted
 * in UTF 16 or UTF 32 and we want to read them AS IF it were ANSI or UTF-8.
 * thus we "skip" part of the bytes from the stream
 *
 * @author Eduardo Vindas
 */
public class SkipInputStream extends InputStream {

    /**
     * the character set to assume we will be working with.
     */
    private final transient Charset DataCharset;
    /**
     * order of bytes
     */
    private final transient boolean isbigEdian;
    /**
     * bytes per character
     */
    private final transient int BytesperData;
    /**
     * the underline stream to handle.
     */
    private final InputStream wrappedstream;

    /**
     * a static function that can be called to Wrap on several other input streams
     * specifically.  
     * (MessageDigestStream(base64Decoder(SkipInputStream(toWrap))))
     * 
     * @param toWrap the stream to wrap
     * @param charEncoding the encoding to use for the SkipInputStream
     * @param diggester the digester algorith to use to wrap the SkipInputStream to calculate the digested signature
     * @return DigestInputStream that wraps a base64Stream that wraps the SkipInputStream that wraps the toWrap stream
     */
    public static final DigestInputStream getWrappedStream(InputStream toWrap, Charset charEncoding, MessageDigest diggester) {
        var base64Decoded = Base64.getDecoder()
                .wrap(new SkipInputStream(toWrap, charEncoding));
        //diggester.reset();//the caller need to ensure this 
        DigestInputStream digestStream = new DigestInputStream(base64Decoded, diggester);
        //digestStream.on(true);//the caller need to ensure this 
        return digestStream;
    }

    public SkipInputStream(InputStream IS, Charset charset) {
        wrappedstream = IS;
        BytesperData = (int) Math.floor(charset.newEncoder().averageBytesPerChar());
        DataCharset = charset;
        isbigEdian = CharsetCompatibilityChecker.RemoveBOM(DataCharset, String.valueOf('A').getBytes(charset));
    }

    @Override
    public int read() throws IOException {
        // the minimal read we do is 1 byte.
        // but to do so we need to read BytesperData
        // to gather it.
        // to gather the byte we desire we need to either read the first one
        // or read the last one (due Edianess)
        // to do so efficiently we read a single byte or skipping and then reading
        if (isbigEdian) {
            wrappedstream.skip(BytesperData - 1);
        }
        var data = wrappedstream.read();
        if (!isbigEdian) {
            wrappedstream.skip(BytesperData - 1);
        }
        return data;
    }

    @Override
    public int available() throws IOException {
        return wrappedstream.available();
    }

    @Override
    public void close() throws IOException {
        wrappedstream.close();
    }

    @Override
    public boolean markSupported() {
        return wrappedstream.markSupported();
    }

    /**
     * fails due this class is not to be cloned.
     *
     * @return nothing
     * @throws CloneNotSupportedException always as this class is not allowed to
     * be cloned.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("not allowed");
    }

    @Override
    public void mark(int readlimit) {
        wrappedstream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        wrappedstream.reset();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        byte Buffer[] = new byte[len * BytesperData];
        var results = wrappedstream.read(Buffer);
        if (results == -1) {
            return results;
        }
        for (int index = 0; index < results; index += BytesperData) {
            var offset = isbigEdian ? BytesperData - 1 : 0;
            b[off++] = Buffer[index + offset];
        }
        return results;
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedstream.skip(BytesperData * n);
    }
}
