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
package com.aeongames.imageextractor;

import com.aeongames.edi.utils.Clipboard.CharsetCompatibilityChecker;
import com.aeongames.edi.utils.Clipboard.FlavorProcessor;
import com.aeongames.edi.utils.ThreadUtils.StopSignalProvider;
import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Level;
import javax.imageio.ImageIO;

/**
 * this class will work as an FlavorProcessor Orchestrator that will hold and
 * arrange the work to be parsed by the tool from checking its Integrity,
 * looking if Already parsed saving into files and so on. this class is NOT a
 * thread. and will not work by its own. it need a worker that "drives" the
 * Orchestrator.
 *
 * however threads are intended to call this class and queue new work. and
 * register as listeners to get updates on their work status. and when finish to
 * get notified.
 *
 * finally. a worker should also pull from this class and process the data.
 *
 *
 * @author cartman
 */
public class ImageProcessor extends UIChangeNotifier implements FlavorProcessor {

    private LinkedList<byte[]> Signatures;
    private static final DataFlavor PROCESSORFLAVOR = DataFlavor.getTextPlainUnicodeFlavor();
    private MessageDigest Hasher;

    /**
     * default class constructor.
     *
     * @exception NoSuchAlgorithmException if we cannot initialize the Message
     * Digester.
     */
    public ImageProcessor() throws NoSuchAlgorithmException {
        Signatures = new LinkedList<>();
        try {
            Hasher = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Hasher = null;// asure we keep a null reference if we are unable to initialize.
            LoggingHelper.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
            // rethrow the error we cannot work without a hasher.
            throw ex;
        }
    }

    /**
     * returns the supported DataFlavor for this class.
     *
     * @return
     */
    public final DataFlavor mySupportedFlavor() {
        return PROCESSORFLAVOR;
    }

    @Override
    public boolean handleFlavor(DataFlavor flavor, StopSignalProvider stopProvider, Transferable transferData, Clipboard clipboard) {
        if (!checkinputs(flavor, stopProvider)) {
            return false;
        }
        InputStream TrasferableDataStream = null;
        try {
            TrasferableDataStream = (InputStream) transferData.getTransferData(flavor);
            if (stopProvider.isStopSignalReceived()) {
                return false;
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            LoggingHelper.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        // did we managed to get the Stream? otherwise this will fail
        if (Objects.isNull(TrasferableDataStream)) {
            return false;
        }
        // Now we need to manually process the data. this is because we want to do
        // several things with the data.
        // first
        var charEncoding = Charset.forName(flavor.getParameter("charset")); // try to get what charset we are using.
        CharsetDecoder decoder = charEncoding.newDecoder();
        CharsetEncoder encoder = charEncoding.newEncoder();
        // if 1 we can bypass the encoding process. and pass on the BASE64 decoder to
        // the stream.
        // otherwise we need to read the bytes and then decode them. and then encode
        // them again to a base64 compatible byte array.
        // Reason for this is that the base64 decoder will not work with a byte array
        // that is not encoded in the same way as the base64 decoder expects.
        // for example:
        // Character Encoding: If the Base64 string is stored in a multibyte encoding
        // (e.g., UTF-16 or UTF-32), each Base64 character (which should be 1 byte in
        // ASCII) could span multiple bytes. For example:
        // In UTF-16, the character A is represented as 0x0041 (2 bytes).
        // In UTF-32, the character A is represented as 0x00000041 (4 bytes).
        // In these cases, the Base64 decoder would not be able to correctly decode the
        // data as
        // it expect single byte characters. (e.g., UTF-8) or more specifically, ASCII.
        // (0x00-0x7F) on our case 0x41 is the letter A.
        // now you might think, but all of them are the same value
        var minBytesPerChar = (int) Math.floor(encoder.averageBytesPerChar());
        try (PushbackInputStream pushbackStream = new PushbackInputStream(TrasferableDataStream, 8_192)) {
            // Step 1: Read a portion of the stream
            byte[] buffer = new byte[minBytesPerChar * 64]; // Read a small chunk for analysis
            int bytesRead = pushbackStream.read(buffer);
            if (bytesRead == -1) {
                return false;
            }
            String imageType = null;
            // Step 2: Decode the bytes using the specified charset
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            CharBuffer charBuffer = CharBuffer.allocate(64); // Allocate enough space for characters
            CoderResult result = decoder.decode(byteBuffer, charBuffer, true);
            charBuffer.flip(); // Prepare the CharBuffer for reading
            // data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAA...
            String header = charBuffer.toString().strip();
            // Step 3: Check if the header contains the expected pattern
            if (header.startsWith("data:image/")) {
                int lastCharofHeader = header.indexOf(",");
                imageType = header.substring(10, lastCharofHeader);
                header = header.substring(lastCharofHeader + 1); // Extract the Base64 part                
                // Step 4: Push back the remaining bytes
                int remainingBytes = byteBuffer.remaining(); // Bytes not yet decoded
                if (remainingBytes > 0) {
                    pushbackStream.unread(buffer, bytesRead - remainingBytes, remainingBytes);
                }
                //now return the non header part back to the stream.
                if (header.length() > 0) {
                    pushbackStream.unread(header.getBytes(charEncoding));
                }
            }
            if (stopProvider.isStopSignalReceived()) {
                return false;
            }
            // Step 5 : check if the CharSet is compatible with the one for Base64
            var CanUseSkipApproach = CharsetCompatibilityChecker.charsetCompatibleWithBase64(charEncoding);
            if (CanUseSkipApproach) {
                var base64Decoded = Base64.getDecoder().wrap(new SkippingIS(pushbackStream, charEncoding, minBytesPerChar));
                Hasher.reset();//ensure we are starting fresh.
                DigestInputStream digestStream = new DigestInputStream(base64Decoded, Hasher);
                digestStream.on(true);
                var image = ImageIO.read(digestStream);
                if (stopProvider.isStopSignalReceived()) {
                    return false;
                }
                //test
                if (image != null) {
                    boolean write = ImageIO.write(image, imageType.toLowerCase().contains("png") ? "png" : "jpg", Files.newOutputStream(Path.of("C:\\", "Users", "cartman", "Desktop", "imager." + (imageType.toLowerCase().contains("png") ? "png" : "jpg")), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
                    return write;
                }
            }

        } catch (IOException ex) {
            LoggingHelper.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return false;
    }

    private boolean checkinputs(DataFlavor flavor, StopSignalProvider stopProvider) {
        // asure that we are not to stop processing
        if (stopProvider.isStopSignalReceived()) {
            return false;
        }
        // there should be no way this was modified. at the current implm.
        // however for sake of safety.
        if (!mySupportedFlavor().equals(flavor)) {
            return false;
        }
        // assure that the Flavor can be handled Correctly here.
        // and if not return false as we cannot handle.
        if (!flavor.isRepresentationClassInputStream()) {
            return false;
        }
        return true;
    }

    /**
     * we need a input stream that constantly skips a certain amount of bytes or
     * ignores them
     *
     */
    private class SkippingIS extends InputStream {

        private final transient Charset DataCharset;
        private final transient boolean isbigEdian;
        private final transient int BytesperData;
        private final InputStream wrappedstream;

        private SkippingIS(InputStream IS, Charset charset, int bytes) {
            wrappedstream = IS;
            BytesperData = bytes;
            DataCharset = charset;
            isbigEdian = CharsetCompatibilityChecker.RemoveBOM(DataCharset, String.valueOf('A').getBytes(charset));
        }

        @Override
        public int read() throws IOException {
            //the minimal read we do is 1 byte. 
            //but to do so we need to read BytesperData 
            //to gather it. 
            //to gather the byte we desire we need to either read the first one 
            //or read the last one (due Edianess) 
            //to do so efficiently we read a single byte or skipping and then reading
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

    private InputStreamReader getDatInputStreamReader(DataFlavor flavor, InputStream stream)
            throws UnsupportedEncodingException {
        var charEncoding = Charset.forName(flavor.getParameter("charset"));
        return (charEncoding == null)
                ? new InputStreamReader(stream)
                : new InputStreamReader(stream, charEncoding);
    }

    /**
     * TODO: THIS CODE NEEDS MIGRATION TO THE NEW system.
     *
     * new SwingWorker<BufferedImage, String>() {
     *
     * @Override protected BufferedImage doInBackground() throws Exception {
     * publish("Decripting Base64 for data."); if (UnderlineData.length() >
     * 1000) { pos = UnderlineData.indexOf(';', 0, 1000);//the delimiter HAS to
     * be on the first part of the string. thus not waste time looking at the
     * full string (assuing the string is long) } else { pos =
     * UnderlineData.indexOf(';'); } if (pos < 0) {
     *           publish("Data does not match the Required pattern, Skipping");
     *           return null;
     *           }
     *           //txtImageType.setText(UnderlineData.substring(0, pos));
     *           var imageType = UnderlineData.substring(0, pos);
     *           if (UnderlineData.length() > 1000) { pos = UnderlineData.indexOf(',',
     * pos, 1000);//the delimiter HAS to be on the first part of the string.
     * thus not waste time looking at the full string (assuing the string is
     * long) } else { pos = UnderlineData.indexOf(',', pos); } String base64 =
     * UnderlineData.substring(pos + 1); byte bytes[]; try { bytes =
     * Base64.getDecoder().decode(base64); } catch (IllegalArgumentException
     * err) { publish("Data cannot be parsed as Base64 string, Skipping");
     * return null; } try { var img = ImageIO.read(new
     * ByteArrayInputStream(bytes)); publish("Image Read."); if
     * (btauto.isSelected()) { var destinationfolder =
     * Paths.get(txtfolder.getText()); var filename =
     * FileSpiner.getModel().getValue().toString() + "." +
     * (imageType.toLowerCase().contains("png") ? "png" : "jpg");
     * destinationfolder = destinationfolder.resolve(filename);
     * ImageIO.write(img, imageType.toLowerCase().contains("png") ? "png" :
     * "jpg", Files.newOutputStream(destinationfolder,
     * StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)); publish("the
     * File " + destinationfolder.toString() + " saved. ");
     * FileSpiner.getModel().setValue(FileSpiner.getModel().getNextValue()); }
     * return img; } catch (java.nio.file.FileAlreadyExistsException exists) {
     * publish("the File alredy exists::" + exists.getMessage()); } catch
     * (IOException ex) {
     * Logger.getLogger(ImageExtractor.class.getName()).log(Level.SEVERE, null,
     * ex);
     *
     * }
     * return null; }
     *
     * @Override
     *
     * protected void process(List<String> chunks) {
     * txtstatusbar.setText(chunks.getLast()); }
     *
     * @Override protected void done() { try { var image = get(); if (image !=
     * null) { PImage.setImage(image); } } catch (InterruptedException |
     * ExecutionException ex) {
     * Logger.getLogger(ImageExtractor.class.getName()).log(Level.SEVERE, null,
     * ex); } SetControlsEnablement(true); } }.execute();
     *
     *
     */
}
