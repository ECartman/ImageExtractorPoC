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
package com.aeongames.imgext.components;

import com.aeongames.edi.utils.common.CharsetCompatibilityChecker;
import com.aeongames.edi.utils.clipboard.FlavorProcessor;
import com.aeongames.edi.utils.threading.StopSignalProvider;
import com.aeongames.edi.utils.common.ByteUtils;
import com.aeongames.edi.utils.common.SkipInputStream;
import com.aeongames.edi.utils.error.LoggingHelper;
import com.aeongames.imgext.components.ProgressObject;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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
 * @author Eduardo Vindas
 */
public class ImageProcessor implements FlavorProcessor {

    private static final int PUSHBACK_BUFFER = 4096;
    private static final int METADATA_CHUNK = 32;
    private transient Path SafeLocation;
    private HashMap<String, String> SignaturesFile;
    private static final DataFlavor[] PROCESSORFLAVOR = new DataFlavor[]{DataFlavor.getTextPlainUnicodeFlavor()};
    private final MessageDigest Hasher;
    private ProgressObject InfoLink;

    /**
     * default class constructor.
     *
     * @param safePath
     * @exception NoSuchAlgorithmException if we cannot initialize the Message
     * Digester.
     */
    public ImageProcessor(Path safePath) throws NoSuchAlgorithmException {
        InfoLink = new ProgressObject();
        RegisterForPathChanges();
        SafeLocation = safePath;
        SignaturesFile = new HashMap<>();
        MessageDigest resultHasher = null;
        try {
            resultHasher = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            LoggingHelper.getClassLoggerForMe().log(Level.SEVERE, null, ex);
            // rethrow the error we cannot work without a hasher.
            throw ex;
        }
        Hasher = resultHasher;
    }

    public synchronized boolean updateSafePath(Path safePath) {
        if (!Files.exists(safePath) || !Files.isDirectory(safePath) || !Files.isWritable(safePath)) {
            return false;
        }
        SafeLocation = safePath;
        return true;
    }

    /**
     * returns the supported DataFlavor for this class.
     *
     * @return
     */
    public final DataFlavor[] mySupportedFlavor() {
        return PROCESSORFLAVOR;
    }

    private void reportNewRequest() {
        Report("///-------------------------------------------------------///");
        Report("A new Request From Clipboard");
        UIStatus(false);
    }

    private boolean isThisForUs(DataFlavor flavor, StopSignalProvider stopProvider) {
        if (!checkinputs(flavor, stopProvider)) {
            Report("Not for us");
            UIStatus(true);
            return false;
        }
        return true;
    }

    private boolean checkinputs(DataFlavor flavor, StopSignalProvider stopProvider) {
        // asure that we are not to stop processing
        if (stopProvider.isStopSignalReceived()) {
            return false;
        }
        // there should be no way this was modified. at the current implm.
        // however for sake of safety.
        if (!mySupportedFlavorSupport(flavor)) {
            return false;
        }
        // assure that the Flavor can be handled Correctly here.
        // and if not return false as we cannot handle.
        return flavor.isRepresentationClassInputStream();
    }

    private boolean shouldStop(StopSignalProvider stopProvider) {
        if (stopProvider.isStopSignalReceived()) {
            UIStatus(true);
            return true;
        }
        return false;
    }

    private void reportFailure(String message) {
        Report(message);
        UIStatus(true);
    }

    private InputStream OpenClipboard(Transferable transferData, DataFlavor flavor) {
        InputStream TrasferableDataStream = null;
        try {
            Report("Reading The Clipboard Into JVM");
            TrasferableDataStream = (InputStream) transferData.getTransferData(flavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            LoggingHelper.getClassLoggerForMe().log(Level.SEVERE, "Unable to pull the TransferData with the registered Flavor.", ex);
            reportError(ex);
        }
        return TrasferableDataStream;
    }

    private PushbackInputStream processMetadata(Charset charset, InputStream TrasferableDataStream, StringBuilder TypeBuilder) {
        // Now we need to manually process the data. this is because we want to do
        // several things with the data.
        // first
        Report("checking Metadata");
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
        var encoder = charset.newEncoder();
        var decoder = charset.newDecoder();
        var minBytesPerChar = (int) Math.floor(encoder.averageBytesPerChar());
        PushbackInputStream pushbackStream = new PushbackInputStream(TrasferableDataStream, PUSHBACK_BUFFER);
        try {
            byte[] buffer = new byte[minBytesPerChar * METADATA_CHUNK]; // Read a small chunk for analysis
            int bytesRead = pushbackStream.read(buffer);
            if (bytesRead == -1) {
                Report("Clipboard Data is shorter than expected or not avail");
                UIStatus(true);
                pushbackStream.close();
                return null;
            }
            // Step 2: Decode the bytes using the specified charset
            Report("Decoding Metadata from the Clipboard initial chunk");
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            CharBuffer charBuffer = CharBuffer.allocate(METADATA_CHUNK); // Allocate enough space for characters
            CoderResult result = decoder.decode(byteBuffer, charBuffer, true);
            charBuffer.flip(); // Prepare the CharBuffer for reading
            String header = charBuffer.toString().strip();
            charBuffer.clear();
            Report("Data Header: " + header);
            // data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAA...
            if (header.startsWith("data:image/")) {
                int lastCharofHeader = header.indexOf(",");
                var imageType = header.substring(11, lastCharofHeader);
                InfoLink.setImageTypeString(imageType);
                imageType = SimpleImageType(imageType);
                if (Objects.isNull(imageType)) {
                    Report("Image String Data does not Report its type. we will asume is base64");
                } else {
                    TypeBuilder.append(imageType);
                }
                header = header.substring(lastCharofHeader + 1); // Extract the Base64 part
                // Step 4: Push back the remaining bytes
                int remainingBytes = byteBuffer.remaining(); // Bytes not yet decoded
                if (remainingBytes > 0) {
                    pushbackStream.unread(buffer, bytesRead - remainingBytes, remainingBytes);
                }
                // now return the non header part back to the stream.
                if (header.length() > 0) {
                    pushbackStream.unread(header.getBytes(charset));
                }
            } else {
                pushbackStream.unread(buffer);
                // the data did not match return all the data to the Stream
            }
            return pushbackStream;
        } catch (IOException ex) {
            try {
                pushbackStream.close();
            } catch (IOException err) {
            }
            LoggingHelper.getClassLoggerForMe().log(Level.SEVERE, null, ex);
            reportError(ex);
            UIStatus(true);
            return null;
        }
    }

    private DigestInputStream getWrappedStream(PushbackInputStream pushbackStream, Charset charEncoding) {
        Hasher.reset();// ensure we are starting fresh.
        var digestStream = SkipInputStream.getWrappedStream(pushbackStream, charEncoding, Hasher);
        digestStream.on(true);
        return digestStream;
    }

    @Override
    public boolean handleFlavor(DataFlavor flavor, StopSignalProvider stopProvider,
            Transferable transferData, Clipboard clipboard) {
        reportNewRequest();
        if (!isThisForUs(flavor, stopProvider)) {
            return false;
        }
        InputStream TrasferableDataStream = OpenClipboard(transferData, flavor);
        if (Objects.isNull(TrasferableDataStream)) {
            reportFailure("Could Not Read The Clipboard");
            return false;
        }
        if (shouldStop(stopProvider)) {
            if (TrasferableDataStream != null) {
                try {
                    TrasferableDataStream.close();
                } catch (IOException err) {
                }
            }
            return false;
        }
        var charEncoding = Charset.forName(flavor.getParameter("charset")); // try to get what charset we are using.
        StringBuilder TypeBuilder = new StringBuilder();
        PushbackInputStream pushbackStream = processMetadata(charEncoding, TrasferableDataStream, TypeBuilder);
        if (Objects.isNull(pushbackStream)) {
            reportFailure("Could Not Read the metadata");
            return false;
        }
        try (pushbackStream) {
            Report("Finish With Metadata Check");
            if (shouldStop(stopProvider)) {
                return false;
            }
            Report("Testing Base64 Decoding");
            if (!CharsetCompatibilityChecker.charsetCompatibleWithBase64(charEncoding)) {
                Report("Charset is NOT compatible with Base64");
                UIStatus(true);
                return false;
            }
            Report("Charset is compatible with Base64, setting up to Read Image");
            DigestInputStream digestStream = getWrappedStream(pushbackStream, charEncoding);
            Report("Reading the Image...");
            StringBuilder type = new StringBuilder();
            var image = readImageFromStream(digestStream, type);
            getImageTypeFinal(TypeBuilder, type);//this might return a empty string? but is so image would be null most likely.
            if (Objects.nonNull(image)) {
                Report(image);
            }
            if (shouldStop(stopProvider)) {
                return false;
            }
            if (image != null) {
                Report("Calculating Checksum");
                var signature = ByteUtils.ByteArrayToString(digestStream.getMessageDigest().digest());
                if (SignaturesFile.containsKey(signature)) {
                    reportCheckSum(signature, SignaturesFile.get(signature));
                    Report("File Alredy Recorded.");
                    UIStatus(true);
                    return true;// we dont need to safe it. again.
                }
                final Path FilePath = GetNextFile(TypeBuilder.toString());
                reportCheckSum(signature, FilePath.toString());
                var imgResult = ImageIO.write(image, TypeBuilder.toString(),
                        Files.newOutputStream(FilePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
                if (imgResult) {
                    Report("File saved.");
                    //we only report the signature if we sucesfully recorded the file. 
                    SignaturesFile.put(signature, FilePath.toString());
                }
                tickFileSpinner();
                UIStatus(true);
                return imgResult;
            } else {
                Report("No image data. flushing the Checksum");
                Hasher.reset();// flush the data we dont need it. 
            }
        } catch (IOException ex) {
            LoggingHelper.getClassLoggerForMe().log(Level.SEVERE, null, ex);
            reportError(ex);
            UIStatus(true);
            return false;
        } finally {
            try {
                if (Objects.nonNull(TrasferableDataStream)) {
                    TrasferableDataStream.close();
                }
            } catch (IOException ex) {
                LoggingHelper.getClassLoggerForMe().log(Level.SEVERE, null, ex);
                reportError(ex);
            }
        }
        UIStatus(true);
        return false;
    }

    private synchronized Path GetNextFile(String imageType) {
        var nextfile = InfoLink.getFileNumber().toString();
        var FilePath = SafeLocation.resolve(String.format("%s.%s", nextfile, imageType));
        return FilePath;
    }

    private synchronized void tickFileSpinner() {
        InfoLink.fileNumberpplus();
    }

    private void RegisterForPathChanges() {
        InfoLink.registerSavingFilePath((Source, newValue) -> {
            var success = updateSafePath(Paths.get(newValue));
        });
    }

    private boolean mySupportedFlavorSupport(DataFlavor flavor) {
        for (DataFlavor dataFlavor : mySupportedFlavor()) {
            if (dataFlavor != null && dataFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public ProgressObject getInfoLink() {
        return InfoLink;
    }

    private void Report(String message) {
        InfoLink.updateStatus(message.concat("\n"));
    }

    private void reportCheckSum(String checksum, String file) {
        var str = String.format("File: %s ; Checksum %s", file, checksum);
        Report(str);
        InfoLink.setStatus(str);
    }

    private void UIStatus(boolean state) {
        InfoLink.setUIEnablement(state);
    }

    private void reportError(Throwable err) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        err.printStackTrace(writer);
        Report(out.toString());
    }

    private String SimpleImageType(String imageType) {
        if (imageType != null) {
            imageType = imageType.toLowerCase().contains("png") ? "png" : "jpg";
        }
        return imageType;
    }

    private void Report(BufferedImage image) {
        InfoLink.setImageData(image);
    }

    private BufferedImage readImageFromStream(DigestInputStream digestStream, StringBuilder type) throws IOException {
        BufferedImage img = null;
        var iis = ImageIO.createImageInputStream(digestStream);
        var readers = ImageIO.getImageReaders(iis);
        if (readers.hasNext()) {
            var reader = readers.next();
            type.append(reader.getFormatName());
            reader.setInput(iis, true);
            img = reader.read(0);
        }
        return img;
    }

    private void getImageTypeFinal(StringBuilder TypeBuilder, StringBuilder type) {
        if (TypeBuilder.isEmpty() || TypeBuilder.toString().strip().isBlank()) {
            Report("Image String Metadata does not report the format or type. we assume Base64 full file");
            TypeBuilder.append(type);
            if (TypeBuilder.isEmpty() || TypeBuilder.toString().strip().isBlank()) {
                TypeBuilder.append("No Image data");
                Report("The data does not seem to represent a Image.");
            } else {
                Report("Image Type from Metadata: " + type.toString());
                Report("Completed Reading the Image...");
            }
            InfoLink.setImageTypeString(TypeBuilder.toString());
        }
    }

}
