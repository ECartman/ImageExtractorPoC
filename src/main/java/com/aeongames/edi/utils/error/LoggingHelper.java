/* 
 *  Copyright © 2024 Eduardo Vindas Cordoba. All rights reserved.
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
package com.aeongames.edi.utils.error;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Eduardo V
 */
public class LoggingHelper {

    /**
     * the pattern to use for logs cycles if required.
     */
    public static final String LOG_FILE_PATTERN = "%g.log";
    private static final Map<String, Logger> StrongReferences = new HashMap<>();
    private static boolean FolderChecked = false;
    public static final String LOG_FOLDER = "errors";

    /**
     * gathers the Logger for the specified ID. if the logger does not exist it 
     * creates a new one and setup the Logger Handler to output the logs into files
     * thus creating a way to track and gather information when the log creates a 
     * entry. 
     * do note the logger level is not change and is leave as the default value.
     * the caller might desire to check or change this to fits its needs.
     * @param LogID the ID or name of the Log
     * @return a instance of Logger class. 
     */
    public static Logger getLogger(String LogID) {
        var LoggerforID = StrongReferences.get(LogID);
        if (LoggerforID != null) {
            return LoggerforID;
        }
        LoggerforID = Logger.getLogger(LogID);
        ensureErrorFolderExists();
        if (isLoggingIntoFile(LoggerforID)) {
            StrongReferences.put(LogID, LoggerforID);
            return LoggerforID;
        }
        try {
            LoggerforID.addHandler(getDefaultFileHandler(LogID));
            StrongReferences.put(LogID, LoggerforID);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(LoggingHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return LoggerforID;
    }

    private static void ensureErrorFolderExists() {
        if (FolderChecked) {
            return;
        }
        var errorFolder = Paths.get(LOG_FOLDER);
        if (!Files.exists(errorFolder, LinkOption.NOFOLLOW_LINKS) || !Files.isDirectory(errorFolder, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectory(errorFolder);
                FolderChecked = true;
            } catch (IOException ex) {
                Logger.getLogger(LoggingHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static boolean isLoggingIntoFile(Logger test) {
        for (var handler : test.getHandlers()) {
            if (handler instanceof FileHandler) {
                return true;
            }
        }
        return false;
    }

    /**
     * builds a "default" File handler for the provided class.
     *
     * @param Name the name to use on this file handler( used as part of the
     * filename as well.)
     * @return a newly created file handler.
     * @throws IOException
     */
    private static FileHandler getDefaultFileHandler(String Name) throws IOException {
        FileHandler Fhandle = new FileHandler(String.format("%s/%s", LOG_FOLDER, Name) + LOG_FILE_PATTERN);
        Fhandle.setFormatter(new SimpleFormatter());
        return Fhandle;
    }
}
