/*
 *
 * Copyright ©  Copyright © 2016,2019,2024-2025 Eduardo Vindas Cordoba. All rights reserved.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.error;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
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

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * private constructor, this class should never have instances
     *
     * @throws IllegalAccessException as this class cannot be instanced.
     */
    private LoggingHelper() throws IllegalAccessException {
        throw new IllegalAccessException("this Class should not be Instanciated");
    }
    //</editor-fold >

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private static final String DEFAULT_LOGGER_NAME = "CenterLogger";
    /**
     * the Default Logger for this Application TODO: need a way to detect
     * application name or project name or change this at compile time. or
     * something along those lines
     */
    private static final Logger DEFAULTLOGGER = Logger.getLogger(DEFAULT_LOGGER_NAME);
    /**
     * the File pattern to use when creating the log files.
     */
    public static final String LOG_FILE_PATTERN = ".%g.log";
    /**
     * default amount if bytes to write per file 
     */
    public static final int FILE_SIZE_LIMIT = 1_073_741_824/2; // lets write up to 500MB per file
    public static final int FILE_COUNT = 5;
    /**
     * the folder where we prefer the logs to be stored at. relative to the Run
     * time Folder
     */
    private static final Path LOG_FOLDER = Path.of("Logs");

    /**
     * a map that hold references to the registered loggers that were requested
     * to this class we use WeakHashMap, as we intent to hold a cache of loggers
     * that are frequenly used. and if they are not used we can save some memory
     * and if we need we can recall the logger to check if there is a logger
     * again
     */
    private static final Map<String, Logger> RegisteredLoggers = new WeakHashMap<>();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="variables">
    /**
     * a boolean that indicates if the folder for the logs was review and
     * created or not.
     */
    private static boolean FolderChecked = false;

    /**
     * a boolean that indicates if the SimpleFormatter should be used or not.
     * this is set to true by default. if false the XMLFormatter will be used.
     */
    public static boolean SimpleFormatter = true;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Static initialization">
    static {
        ensureErrorFolderExists();
        try {
            //we might need to either add or removed STD out or STD err outputs. but no for the time begin
            DEFAULTLOGGER.addHandler(getDefaultFileHandler(DEFAULT_LOGGER_NAME));
            DEFAULTLOGGER.setLevel(Level.ALL);
            RegisteredLoggers.put(DEFAULT_LOGGER_NAME, DEFAULTLOGGER);
        } catch (IOException | SecurityException ex1) {
            Logger.getLogger(LoggingHelper.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }
    //</editor-fold>

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
    public static final Logger getLogger(String LogID) {
        var LoggerforID = RegisteredLoggers.get(LogID);
        if (LoggerforID != null) {
            return LoggerforID;
        }
        LoggerforID = Logger.getLogger(LogID);
        LoggerforID.setLevel(Level.ALL);
        ensureErrorFolderExists();
        if (!isLoggingIntoFile(LoggerforID)) {
            try {
                LoggerforID.addHandler(getDefaultFileHandler(LogID));
            } catch (IOException | SecurityException ex) {
                Logger.getLogger(LoggingHelper.class.getName()).log(Level.SEVERE,"cannot setup the File Logging", ex);
            }
        }
        RegisteredLoggers.put(LogID, LoggerforID);
        return LoggerforID;
    }

    /**
     * gets the Default Logger for this application. you can use this logger to
     * log errors. but might provide a large file and difficult to follow if
     * many classes log into it. use this logger as last resort or if your
     * application logs are manageable
     *
     * @return the default logger for this application.
     */
    public static final Logger getDefaultLogger() {
        return DEFAULTLOGGER;
    }

    /**
     * gets or creates the logger for the caller class to this function. it
     * simplifies the process of creating the logger based on just class name
     * however it might fail to create it. if fails it returns the Default
     * logger.
     *
     * @return a new or the registered Logger for the calling class if it fails
     * to read the caller class it returns the {@link getDefaultLogger()}
     */
    public static final Logger getClassLoggerForMe() {
        String caller = getCallerCallerClassName();
        if (caller != null) {
            return getLogger(caller);
        } else {
            return getDefaultLogger();
        }
    }

    /**
     * get the name of the Class that called the LoggingHelper Class this is
     * used to at run time determine the name of the class and use this value to
     * create a logger. this might fail however due security constrains
     *
     * @return the string name of the caller class or null if we cannot
     * determine+
     */
    private static String getCallerCallerClassName() {
        StackTraceElement[] stElements = null;
        try {
            stElements = Thread.currentThread().getStackTrace();
        } catch (SecurityException err) {
            //likely security related. we are on a limited enviroment?
        }
        if (stElements != null) {
            //so the stack trace SHOULD be parked at THIS class. or on the Thead stack call
            //so we need to dive until we find the last iteration of THIS or Thread Stack Trace. 
            for (StackTraceElement ste : stElements) {
                //ensure we get the caller class and not THIS class
                if (!ste.getClassName().equals(LoggingHelper.class.getName()) && ste.getClassName().indexOf(Thread.class.getName()) != 0) {
                    return ste.getClassName();
                }
            }
        }
        //not detected? WHY!?
        return null;
    }

    private static void EnsureLogFolderReady() throws IOException {
        if (!Files.exists(LOG_FOLDER, LinkOption.NOFOLLOW_LINKS)
                || !Files.isDirectory(LOG_FOLDER, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(LOG_FOLDER);
        }
    }

    /**
     * checks if the folder for the logs exists and if not it creates it. the
     * whole process is skipped if {@code FolderChecked} is true.
     */
    private static void ensureErrorFolderExists() {
        if (FolderChecked) {
            return;
        }
        try {
            EnsureLogFolderReady();
            FolderChecked = true;
        } catch (IOException ex) {
            Logger.getLogger(LoggingHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * checks if the provided logger is logging into a file or not.
     *
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
     *
     * @return whenever the application is running in debug mode or not.
     */
    public static boolean RunningInDebugMode() {
        var propertyDebug = Boolean.getBoolean("debug.mode") || Boolean.getBoolean("debug");
        var isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
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
        String pattern =String.format("%s/%s", LOG_FOLDER.toString(), Name) + LOG_FILE_PATTERN;
        FileHandler Fhandle = new FileHandler(pattern,FILE_SIZE_LIMIT,FILE_COUNT,true);
        Fhandle.setFormatter(SimpleFormatter ? new SimpleFormatter() : new XMLFormatter());
        return Fhandle;
    }
}
