/*
 *  Copyright © 2008-2013,2025 Eduardo Vindas Cordoba. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.File;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * this class is intended to save and load data from a property file and create
 * a instance that will keep properties from a single or multiple files.
 * the property file is a text file that use a pattern:
 * <br>
 * variable=value
 * <br>
 * ...
 *  <br>
 * #comment ...
 *  <br>
 * and so on... or a XML Format. it is useful to read user editable settings.
 *
 * @version 2.0
 * @since 1.7
 * @see java.util.Properties
 * @author Eduardo Jose Vindas Cordoba <cartman>
 */
public final class Properties_File {

    //<editor-fold defaultstate="collapsed" desc="Static And Constants">
    public enum Response {
        SAVED,
        NOREQUIRED,
        ERROR,
        FILENOTSET;
    }
    /**
     * the Generic name for Names on this class.
     */
    private final static String GENERIC_PROPERTY_NAME = "Properties";
    /**
     * a simple date DATE_FORMAT.
     */
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    /**
     * Current Instance Counter
     */
    private static int Runtime_Counter = 0;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="constant Properties">
    /**
     * the Properties object that will contain and manage the properties.
     */
    private final java.util.Properties configFile = new java.util.Properties();
    /**
     * runtimeID
     */
    private final int RUNTIMEID = assingID();
    /**
     * the settings name
     */
    private final String Name;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Properties">
    /**
     * a variable to determine whenever or not changes been performed to the
     * settings and are not recorded into a file
     */
    private boolean Dirty = false;
    /**
     * the settings File Location for data persistence.
     */
    private Path SaveLocation = null;
    /**
     * whenever or not the data was read and therefore required to be recorded
     * as XML
     */
    private boolean ReadedFromXML = false;

    /**
     * a list of files loaded on this Property object
     */
    //</editor-fold>
    /**
     * generates and provides the current class instance.
     *
     * @return the next id to be used on this class.
     */
    private synchronized static int assingID() {
        return ++Runtime_Counter;
    }

    /**
     * creates a new instance of the Properties_FileV2 that reads and holds all
     * the properties from the provided file location
     *
     * @param FilePath the file path to read the initial properties from
     */
    public Properties_File(Path FilePath) {
        Objects.requireNonNull(FilePath, "the Path cannot be null");
        //lets check whenver or not the file defined by path is xml 
        Name = FilePath.getFileName().toString().strip();
        ReadedFromXML = Name.toLowerCase().endsWith(".xml");
        if (Files.exists(FilePath, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(FilePath, LinkOption.NOFOLLOW_LINKS)) {
            //directory or link. likely the caller just want to dump properties to this dir. we cant do that from here throw up
            throw new IllegalArgumentException("we require a File either a XML or property file (text)");
        } else if (Files.exists(FilePath, LinkOption.NOFOLLOW_LINKS) && Files.isReadable(FilePath)) {
            //good we have a file that we can TRY to load.
            try (var is = Files.newInputStream(FilePath, StandardOpenOption.READ)) {
                LoadSettings(is, false, ReadedFromXML);
            } catch (IOException ex) {
                Logger.getLogger(Properties_File.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //if the filepath is not a directory and regardless if we loaded the data. 
        //we will use it to record later on. 
        SaveLocation = FilePath;

    }

    /**
     * creates a new instance of the Properties_FileV2 that reads and holds all
     * the properties from the provided file location
     *
     * @param FilePath the file path to read the initial properties from
     * @param isXML whenever the file refers to a XML or not. (the default tries
     * to detected whenever is or not a XML
     */
    public Properties_File(Path FilePath, boolean isXML) {
        Objects.requireNonNull(FilePath, "the Path cannot be null");
        Name = FilePath.getFileName().toString().strip() + RUNTIMEID;
        ReadedFromXML = isXML;
        if (!Files.isRegularFile(FilePath, LinkOption.NOFOLLOW_LINKS)) {
            //directory or link. likely the caller just want to dump properties to this dir. we cant do that from here throw up
            throw new IllegalArgumentException("we require a File either a XML or property file (text)");
        } else if (Files.exists(FilePath, LinkOption.NOFOLLOW_LINKS) && Files.isReadable(FilePath)) {
            //good we have a file that we can TRY to load.
            try (var is = Files.newInputStream(FilePath, StandardOpenOption.READ)) {
                LoadSettings(is, false, ReadedFromXML);
            } catch (IOException ex) {
                Logger.getLogger(Properties_File.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //if the filepath is not a directory and regardless if we loaded the data. 
        //we will use it to record later on. 
        SaveLocation = FilePath;

    }

    public Properties_File(String internalFile, boolean isXML, Class requesterClass) {
        Objects.requireNonNull(internalFile, "the file within cannot be null");
        Name = GENERIC_PROPERTY_NAME + RUNTIMEID;
        ReadedFromXML = isXML;
        try {
            loadwithin(internalFile, requesterClass, false, isXML);
        } catch (IOException e) {
            Logger.getLogger(Properties_File.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * reads the provided stream and loads the properties into this instance we
     * assume the stream represent a stream that is supported by
     * {@link java.util.Properties} to read.
     *
     * @param stream the stream to read.
     * @param concatValues whenever we should add the values to this instance or
     * remove all and then load them into this instance
     * @param fromXML if the stream represent a xml file.
     * @throws IOException if fails to read the stream
     */
    protected synchronized void LoadSettings(InputStream stream, boolean concatValues, boolean fromXML) throws IOException {
        if (!concatValues) {
            Dirty = false;
            configFile.clear();
        }
        boolean willdirty = !configFile.isEmpty();
        if (!fromXML) {
            configFile.load(stream);
        } else {
            configFile.loadFromXML(stream);
        }
        Dirty = willdirty && !configFile.isEmpty();
    }

    /**
     * Loads And if specified Concatenates all the settings already loaded.
     * please be aware that this will rewrite settings that have not been saved
     * if settings with the same name exists. also if Concat is set to false all
     * new settings will be dropped.
     *
     * @param file the File location to read.
     * @param concat if concatenate the values.
     * @param XML if a XML file will be loaded or a prop file
     */
    public synchronized void LoadSettings(Path file, boolean concat, boolean XML) {
        Objects.requireNonNull(file, "the Path cannot be null");
        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
            //directory. did the caller wants to load all the files in this dir? 
            //we shall NOT recurse only read whichever files are there 
            try (var filesinfolder = Files.list(file).filter(Files::isRegularFile)) {
                filesinfolder.forEach((var p) -> {
                    LoadSettings(p, true, XML);
                });
            } catch (IOException ex) {
                Logger.getLogger(Properties_File.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && Files.isReadable(file)) {
            //lets check whenver or not the file defined by path is xml 
            var isxml = file.getFileName().toString().strip().toLowerCase().endsWith(".xml");
            isxml = isxml ? isxml : XML;
            try (var is = Files.newInputStream(file, StandardOpenOption.READ)) {
                LoadSettings(is, false, isxml);
            } catch (IOException ex) {
                Logger.getLogger(Properties_File.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * reloads the INITIAL settings from the first file registered, will clear
     * all current changes and will remove any cache. be aware if you need to do
     * a cached task you need to reset to do a cache.
     */
    public synchronized void reLoadSettings() {
        LoadSettings(SaveLocation, false, ReadedFromXML);
    }

    /**
     *
     * @param within the internal resource to read
     * @param classfrom the class calling to read
     * @param concat if concatenate the values or load from 0 and remove all new
     * variables and changes
     * @param XML if the read file is XML or props file
     * @throws java.io.IOException
     */
    public synchronized void loadwithin(String within, Class classfrom, boolean concat, boolean XML) throws java.io.IOException {
        if (!concat) {
            Dirty = false;
            configFile.clear();
        }
        try (InputStream resource = classfrom.getResourceAsStream(within)) {
            LoadSettings(resource, concat, XML);
        }
    }

    /**
     * gathers and returns all the keys for the loaded properties
     *
     * @return a set of Objects (most likely Strings) keys.
     */
    public synchronized Set<Object> getKeyset() {
        return configFile.keySet();
    }

    /**
     * saves the settings to the specified Settings file (that is manually
     * specified or the same file from the settings were loaded) if no file is
     * set will return false and no settings will be recorded. this method will
     * and cannot record to internal resources.
     *
     * @return whenever or not successful, also might return false if the save
     * is not needed.(values have not changed)
     */
    public synchronized Response SaveIfNeeded() {
        if (Dirty) {
            if (SaveLocation != null) {
                return save(SaveLocation, String.format("Changes Last Made: %s",
                        DATE_FORMAT.format(Calendar.getInstance().getTime())),
                        ReadedFromXML) ? Response.SAVED : Response.ERROR;
            } else {
                return Response.FILENOTSET;
            }
        } else {
            return Response.NOREQUIRED;
        }
    }

    /**
     *
     * @param file the file to record into
     * @param HeaderInfo a header comment.
     * @param XML whenever to save with XML pattern or not.
     * @return whenever or not successful, also might return false if the save
     * is not needed.(values have not changed)
     */
    public synchronized Response SaveIfNeeded(Path file, String HeaderInfo, boolean XML) {
        if (Dirty) {
            return save(file, HeaderInfo, XML) ? Response.SAVED : Response.ERROR;
        } else {
            return Response.NOREQUIRED;
        }
    }

    public synchronized boolean save(Path file, String HeaderInfo, boolean XML) {
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("the path defines a folder or a non Regular file");
        }
        
        try (var out = Files.newOutputStream(file)) {
            if (!XML) {
                configFile.store(out, HeaderInfo);
            } else {
                configFile.storeToXML(out, HeaderInfo);
            }
            Dirty = false;
            return true;
        } catch (IOException | NullPointerException ex) {
            return false;
        }

    }

    /**
     * reads and gather a property, if it is not found or the property is null
     * returns null
     *
     * @param KeyName they property name
     * @return
     */
    public synchronized String getProperty(String KeyName) {
        return configFile.getProperty(KeyName);
    }

    /**
     * sets the specific value to the provided property, if the property had a
     * previous value this method will return that value if none or an error is
     * found will return null;
     *
     * @param KeyName the property to set
     * @param Value the value of this property
     * @return a previous stored value under this property or null if none or
     * error Error can be caused due the OBject is corrupted or key is null
     */
    public synchronized Object setProperty(String KeyName, String Value) {
        if (Objects.isNull(KeyName)) {
            return null;
        }
        var oldv = configFile.setProperty(KeyName, Value);
        Dirty = true;
        return oldv;
    }

    public synchronized Object removeProperty(String KeyName) {
        return configFile.remove(KeyName);
    }

    /**
     * @return the Name
     */
    public synchronized String getName() {
        return Name;
    }

    /**
     * returns this instance unique ID;
     *
     * @return
     */
    public int getID() {
        return RUNTIMEID;
    }

    /**
     * returns true if changes were done but are not stored on file.
     *
     * @return
     */
    public synchronized boolean hasUnsavedChanges() {
        return Dirty;
    }

    public synchronized Path getInPlaseSaveLocation() {
        return SaveLocation;
    }

    /**
     * sets the save location for this properties.
     *
     * @param Save the file location to save
     * @param asXML if recorded as XML or properties file.
     * @return true if and only if the values were set.
     */
    public synchronized boolean setSaveLocation(Path Save, boolean asXML) {
        Objects.requireNonNull(Save, "the save path cannot be null");
        if (Files.exists(Save, LinkOption.NOFOLLOW_LINKS)
                && (!Files.isRegularFile(Save, LinkOption.NOFOLLOW_LINKS)
                || !Files.isWritable(Save))) {
            //throw new IllegalArgumentException("the path defines a folder or a non Regular file or cannot be written");
            return false;
        }
        SaveLocation = Save;
        ReadedFromXML = asXML;
        return true;
    }

    public void clearMemory() {
        SaveLocation = null;
        configFile.clear();
    }

}
