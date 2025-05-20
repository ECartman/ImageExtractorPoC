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
package com.aeongames.edi.utils.Pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * this is a class that Pass on ListenableProperty and defines Functions to
 * trigger events to the Listeners for the specific property hosted. this class
 * can be overridden to extend functionality
 *
 * @author Eduardo Vindas
 * @param <T> the Property Type Class that represent the value this property
 * holds
 */
public class PropertyPojo<T> implements ListenableProperty<T> {

    /**
     * creates a new {@link PropertyPojo} of {@link String} Type. 
     * with the Default value of ""
     * @return a new {@link PropertyPojo} of {@link String} Type that hold a empty String
     */
    public final static PropertyPojo<String> newStringPojo() {
        return new PropertyPojo<>("");
    }
    
    /**
     * creates a new {@link PropertyPojo} of {@link Object} Type. 
     * with the Default value of ""
     * @return a new {@link PropertyPojo} of {@link Object} Type 
     */
    public final static PropertyPojo<Object> newObjectPojo() {
        return new PropertyPojo<>();
    }

    private T Value;
    private final List<PropertyChangeListener<T, ListenableProperty<T>>> listeners;

    /**
     * creates a new Instance of a Property POJO. 
     * the underline Property is unset (null);
     */
    public PropertyPojo() {
        listeners = new ArrayList<>();
    }

    /**
     * creates a new Instance of a Property POJO.and sets the 
     * Value property to the Provided value.
     * @param defaultValue the Initial Value to set.
     */
    public PropertyPojo(T defaultValue) {
        this();
        Value = defaultValue;
    }

    protected final void firePropertyChanged() {
        for (PropertyChangeListener<T, ListenableProperty<T>> listener : listeners) {
            listener.propertyChanged(this, getValue());
        }
    }
    
    
    /**
     * gets a READ only list of the list that is hosted at this object.
     *
     * @return READ ONLY list with the listeners for this object
     */
    public List<PropertyChangeListener<T, ListenableProperty<T>>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }


    @Override
    public synchronized boolean updateProperty(T newValue) {
        Value = newValue;
        firePropertyChanged();
        return true;
    }

    @Override
    public synchronized void setValue(T newValue) {
        Value = newValue;
        firePropertyChanged();
    }

    @Override
    public synchronized T getValue() {
        return Value;
    }

    @Override
    public synchronized final void addPropertyListener(PropertyChangeListener<T, ListenableProperty<T>> Listener) {
        listeners.add(Listener);
    }

    @Override
    public synchronized final void RemovePropertyListener(PropertyChangeListener<T, ListenableProperty<T>> Listener) {
        listeners.remove(Listener);
    }
}
