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
package com.aeongames.edi.utils.pojo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * this is a class that handles {@link Path}'s it differs from a plain
 * PropertyPojo as it has additional functionality to ask and handle if the path
 * represents a valid file, a folder and a few other check's and whenever it
 * should allow to update if the value is null or not.
 *
 * @author Eduardo
 */
public final class PathPropertyPojo extends PropertyPojo<Path> {

    //<editor-fold defaultstate="collapsed" desc="Enums">
    /**
     * Policy to restrict the path to a specific Type.
     */
    public enum Restrict {
        to_Folders,
        to_Files,
        to_none;
    }

    /**
     * policy to restrict whenever the file MUST exist or the other way it must
     * Not Exists.
     */
    public enum EnforcePolicy {
        Existance,
        NonExistance,
        Indiferent;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Properties">
    /**
     * The restriction that apply to this property for example the Path Must be
     * a directory.
     *
     * @see Restrict
     */
    private Restrict Restriction;
    /**
     * the Enforced Policies that apply to this property. for example if the
     * file Must Exists. or not.
     */
    private EnforcePolicy checkPathType;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Default Constructor.creates a new {@code PathPropertyPojo} with the
     * default Restriction of {@link Restrict#to_none} and
     * {@link EnforcePolicy#Indiferent}
     */
    public PathPropertyPojo() {
        super();
        Restriction = Restrict.to_none;
        checkPathType = EnforcePolicy.Indiferent;
    }

    /**
     * creates a new {@code PathPropertyPojo} with the default Restriction of
     * {@link Restrict#to_none} and {@link EnforcePolicy#Indiferent} using the
     * provided Path as the initial value to this property. do Note. we might
     * try to check the policies. and thus a IO error might trigger.
     *
     * @param initial the initial value of the Path.
     */
    public PathPropertyPojo(final Path initial) {
        super(initial);
        Restriction = Restrict.to_none;
        checkPathType = EnforcePolicy.Indiferent;
    }

    /**
     * creates a new {@code PathPropertyPojo} with the default Restriction of
     * {@link Restrict#to_none} and {@link EnforcePolicy#Indiferent} using the
     * provided Path as the initial value to this property. do Note. we might
     * try to check the policies. and thus a IO error might trigger.
     *
     * @param initial the initial value of the Path.
     */
    public PathPropertyPojo(final String initial) {
        this(Path.of(initial));
    }

    /**
     * creates a new {@code PathPropertyPojo} with the provided restrictions and
     * policy.
     *
     * @param initial the initial String that represent the Path to use
     * @param pathRestriction the restriction to apply to the files
     * @param policy the policy to apply to the path.
     * @throws IOException if it fails to process the File metadata to
     * enforcePolicy or Restrictions
     * @throws FileNotFoundException if EnforcePolicy requires the file to exist
     * and does not. Or it requires to be a File and does not exists.
     * @throws FileAlreadyExistsException if the EnforcePolicy requires the file
     * to NOT exists.
     */
    public PathPropertyPojo(final String initial, final Restrict pathRestriction, final EnforcePolicy policy) throws IOException, FileNotFoundException, FileAlreadyExistsException {
        this(Path.of(initial), pathRestriction, policy);
    }

    /**
     * creates a new {@code PathPropertyPojo} with the provided restrictions and
     * policy.
     *
     * @param initial the initial value for the Path.
     * @param pathRestriction the restriction to apply to the files
     * @param policy the policy to apply to the path.
     * @throws IOException if it fails to process the File metadata to
     * enforcePolicy or Restrictions
     * @throws FileNotFoundException if EnforcePolicy requires the file to exist
     * and does not. Or it requires to be a File and does not exists.
     * @throws FileAlreadyExistsException if the EnforcePolicy requires the file
     * to NOT exists.
     */
    public PathPropertyPojo(final Path initial, final Restrict pathRestriction, final EnforcePolicy policy) throws IOException {
        super(initial);
        Restriction = pathRestriction;
        checkPathType = policy;
        checkSettings(initial);
    }

    //</editor-fold>
    
    /**
     * review and determine if the current constrains allow the path to be set
     * as null
     *
     * @return whenever or not given the current constrains it should allow null
     * values
     */
    public boolean allowsNull() {
        var allowed = Objects.equals(Restriction, Restrict.to_none);
        allowed = allowed && !Objects.equals(checkPathType, EnforcePolicy.Existance);
        return allowed;
    }

    /**
     * checks of the value provided meets the required constrains.
     *
     * @param initial the initial value to check
     * @throws FileNotFoundException if the file does not exist but it should
     * @throws FileAlreadyExistsException if the file exist and it should not
     * @throws IOException if the path should be a directory and is regular or
     * the way around.
     */
    private void checkSettings(Path initial) throws FileAlreadyExistsException, FileNotFoundException, IOException {
        if (!allowsNull()) {
            Objects.requireNonNull(initial, "the Provided Path is null and it is not Allowed");
        } //if it allows nulls then check if the path is null. and skip check further
        else if (Objects.isNull(initial)) {
            return;
        }
        boolean exists = Files.exists(initial);
        boolean isDir = Files.isDirectory(initial);
        boolean isregular = Files.isRegularFile(initial);
        if (Objects.equals(EnforcePolicy.Existance, checkPathType) && !exists) {
            throw new FileNotFoundException("Path provided does not exist and it is required to do so.");
        } else if (Objects.equals(EnforcePolicy.NonExistance, checkPathType) && exists) {
            throw new FileAlreadyExistsException("Path provided does exist and it is required NOT to do so.");
        }
        if (exists && Objects.equals(Restriction, Restrict.to_Folders) && !isDir) {
            throw new IOException("The path does not related to a directory and is restricted to do so.");
        } else if (exists && Objects.equals(Restriction, Restrict.to_Files) && !isregular) {
            throw new IOException("The path is not a Regular File and is restricted to do so.");
        }
    }

    /**
     * check if the provided value follows the rules required.
     *
     * @param newValue the value to check the rules for.
     * @return if it matches or not the rules.
     */
    private boolean followsRules(Path newValue) {
        boolean proceed;
        switch (checkPathType) {
            case Existance:
                proceed = Files.exists(newValue);
                break;
            case NonExistance:
                proceed = !Files.exists(newValue);
                break;
            case Indiferent:
            default:
                proceed = true;
                break;
        }
        if (!proceed) {
            return false;
        }
        switch (Restriction) {
            case to_Files:
                proceed = Files.isRegularFile(newValue);
                break;
            case to_Folders:
                proceed = Files.isDirectory(newValue);
                break;
            case to_none:
            default:
                proceed = true;
                break;
        }
        return proceed;
    }

    //<editor-fold defaultstate="collapsed" desc="Setters">
    /**
     * we suggest to use either
     * {@link PathPropertyPojo#setValueEx(java.nio.file.Path)} instead to know
     * if the value change fails and why. if you desire a version that can fail
     * with no error use {@link PropertyPojo#tryUpdateProperty(java.lang.Object) }
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(Path newValue) {
        if (allowsNull() && Objects.isNull(newValue)) {
            super.setValue(newValue);
            return;
        }
        Objects.requireNonNull(newValue, "the Provided Path is null and it is not Allowed");
        if (followsRules(newValue)) {
            super.setValue(newValue);
        }
    }

    /**
     * Does the same as {@link PathPropertyPojo#setValue(java.nio.file.Path) }
     * but this will throw a error if fails the policies or enforcements.
     *
     * @param newValue the new value to set.
     * @throws FileNotFoundException if the file does not exist but it should
     * @throws FileAlreadyExistsException if the file exist and it should not
     * @throws IOException if the path should be a directory and is regular or
     * the way around.
     */
    public synchronized void setValueEx(Path newValue) throws FileNotFoundException, FileAlreadyExistsException, IOException {
        if (allowsNull() && Objects.isNull(newValue)) {
            super.setValue(newValue);
            return;
        }
        Objects.requireNonNull(newValue, "the Provided Path is null and it is not Allowed");
        boolean exists = Files.exists(newValue);
        boolean isDir = Files.isDirectory(newValue);
        boolean isregular = Files.isRegularFile(newValue);
        if (Objects.equals(EnforcePolicy.Existance, checkPathType) && !exists) {
            throw new FileNotFoundException("Path provided does not exist and it is required to do so.");
        } else if (Objects.equals(EnforcePolicy.NonExistance, checkPathType) && exists) {
            throw new FileAlreadyExistsException("Path provided does exist and it is required NOT to do so.");
        }
        if (exists && Objects.equals(Restriction, Restrict.to_Folders) && !isDir) {
            throw new IOException("The path does not related to a directory and is restricted to do so.");
        } else if (exists && Objects.equals(Restriction, Restrict.to_Files) && !isregular) {
            throw new IOException("The path is not a Regular File and is restricted to do so.");
        }
        super.setValue(newValue);
    }

    /**
     * Does the same as {@link PathPropertyPojo#setValue(java.nio.file.Path) }
     * but this will throw a error if fails the policies or enforcements. and
     * parses a string instead of the path. similar as {@code setValueEx(Path.of(String_Value))
     * }
     *
     * @param newValue the new value to set.
     * @throws FileNotFoundException if the file does not exist but it should
     * @throws FileAlreadyExistsException if the file exist and it should not
     * @throws IOException if the path should be a directory and is regular or
     * @throws NullPointerException if the string is null and is not allowed the
     * way around.
     */
    public synchronized void setValueEx(String newValue) throws FileNotFoundException, FileAlreadyExistsException, IOException {
        if (allowsNull() && Objects.isNull(newValue)) {
            super.setValue(null);
            return;
        }
        Objects.requireNonNull(newValue, "the Provided Path is null and it is not Allowed");
        setValueEx(Path.of(newValue));
    }

    /**
     * Does the same as {@link PathPropertyPojo#tryUpdateProperty(java.nio.file.Path)
     * }
     * and parses a string instead of the path. similar as {@code tryUpdateProperty(Path.of(String_Value))
     * }
     *
     * @param newValue the new value to set.
     */
    public synchronized boolean tryUpdateProperty(String newValue) throws FileNotFoundException, FileAlreadyExistsException, IOException {
        if (allowsNull() && Objects.isNull(newValue)) {
            return super.tryUpdateProperty(null);
        }
        return tryUpdateProperty(Path.of(newValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean tryUpdateProperty(Path newValue) {
        if (allowsNull() && Objects.isNull(newValue)) {
            return super.tryUpdateProperty(newValue);
        }
        if (Objects.isNull(newValue)) {
            return false;
        }

        if (!followsRules(newValue)) {
            return false;
        }
        return super.tryUpdateProperty(newValue);
    }

    /**
     * Does the same as {@link PathPropertyPojo#setValue(java.nio.file.Path) }
     * but this version will parse the string into a Path parses a string
     * instead of the path. similar as {@code setValueEx(Path.of(String_Value))
     * }
     *
     */
    public synchronized void setValue(String newValue) {
        setValue(Path.of(Objects.requireNonNull(newValue)));
    }
    //</editor-fold>

}
