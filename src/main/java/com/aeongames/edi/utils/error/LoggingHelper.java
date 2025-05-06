/* 
 *  Copyright © 2024-2025 Eduardo Vindas Cordoba. All rights reserved.
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
import java.util.logging.XMLFormatter;

/**
 *
 * @author Eduardo Vindas
 */
public class LoggingHelper {

    /**
     * the File pattern to use when creating the log files.
     */
    public static final String LOG_FILE_PATTERN = "%g.log";
    /**
     * a map that hold references to the registered loggers that were requested
     * to this class
     */
    private static final Map<String, Logger> StrongReferences = new HashMap<>();
    /**
     * a boolean that indicates if the folder for the logs was review and created or not.
     */
    private static boolean FolderChecked = false;
    /**
     * the folder to use for the logs. this is a relative path to the current
     * working directory.
     */
    public static final String LOG_FOLDER = "errors";
    /**
     * a boolean that indicates if the SimpleFormatter should be used or not.
     * this is set to true by default. if false the XMLFormatter will be used.
     */
    public static boolean SimpleFormatter = true;

    /**
     * gathers the Logger for the specified ID. if the logger is not registered
     * to this class it calls the underline Facility to pull or create it. and
     * setup the Logger Handler to output the logs into files thus creating a
     * way to track and gather information when the log creates a entry. do note
     * the logger level is not change and is leave as the default value. the
     * caller might desire to check or change this to fits its needs.
     *
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

    /**
     * checks if the folder for the logs exists and if not it creates it.
     * the whole process is skipped if {@code FolderChecked} is true.
     */
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

    /**
     * checks if the provided logger is logging into a file or not.
     * @param test the logger to check
     * @return true if the logger is logging into a file, false otherwise.
     */
    private static boolean isLoggingIntoFile(Logger test) {
        for (var handler : test.getHandlers()) {
            if (handler instanceof FileHandler) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if the application is running in debug mode or not.
     * @return whenever the application is running in debug mode or not.
     */
    public static boolean RunningInDebugMode() {
        var propertyDebug = Boolean.getBoolean("debug.mode") || Boolean.getBoolean("debug");
        var isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("jdwp") >= 0;
        return propertyDebug || isDebug;
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
        Fhandle.setFormatter(SimpleFormatter ? new SimpleFormatter() : new XMLFormatter());
        return Fhandle;
    }
}
