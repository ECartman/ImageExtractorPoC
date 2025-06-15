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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * this class defines a "simple Java Object" that holds a single property of
 * type {@code T} that defines getters and setters. this class implements
 * {@link ListenableProperty} which means this class also is intended to notify
 * a listener when the value is changed. NOTE: by design this will only notify
 * for changes on the Value change. not the reference changes (if using non
 * primitives) for complex objects and notification on changes for objects. you
 * need to override functionality to support this. this class can be overridden
 * to extend functionality
 *
 * also this class is Syncronized using basic object Syncronization. and thus.
 * reading and setting the value will cause this object to lock. this is usually
 * desired. for Thread to UI. but some threads might need to update As fast as
 * possible for those a different implementation is required.
 *
 * @author Eduardo Vindas
 * @param <T> the Property Type Class that represent the value this property
 * holds
 */
public sealed class PropertyPojo<T> implements ListenableProperty<T>
        permits BooleanPropertyPojo,
        IntegerPropertyPojo,
        PathPropertyPojo {

    /**
     * creates a new {@link PropertyPojo} of {@link String} Type. with the
     * Default value of ""
     *
     * @return a new {@link PropertyPojo} of {@link String} Type that hold a
     * empty String
     */
    public final static PropertyPojo<String> newStringPojo() {
        return new PropertyPojo<>("");
    }

    /**
     * creates a new {@link PropertyPojo} of {@link Object} Type. with the
     * Default value of ""
     *
     * @return a new {@link PropertyPojo} of {@link Object} Type
     */
    public final static PropertyPojo<Object> newObjectPojo() {
        return new PropertyPojo<>();
    }

    /**
     * the underline Value to hold for this Property
     */
    private T Value;
    /**
     * a List of Listeners that have been registered to be notified when the
     * property changes.
     */
    private final List<PropertyChangeListener<T, ListenableProperty<T>>> listeners;

    /**
     * creates a new Instance of a Property POJO. the underline Property is
     * unset (null);
     */
    public PropertyPojo() {
        listeners = new ArrayList<>();
    }

    /**
     * creates a new Instance of a Property POJO.and sets the Value property to
     * the Provided value.
     *
     * @param initialValue the Initial Value to set.
     */
    public PropertyPojo(T initialValue) {
        this();
        Value = initialValue;
    }

    /**
     * iterates over the listeners and fires the event telling the listeners
     * that the value has changed.
     */
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

    /**
     * has the same effect as calling {@link PropertyPojo#setValue} this means
     * that this function is synchronized and does not support a more complex
     * locking (atm)
     *
     * @param newValue the new value to set.
     * @return true as the value is changed.
     */
    @Override
    public synchronized boolean tryUpdateProperty(T newValue) {
        if (!Objects.equals(Value, newValue)) {
            Value = newValue;
            firePropertyChanged();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(T newValue) {
        if (!Objects.equals(Value, newValue)) {
            Value = newValue;
            firePropertyChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized T getValue() {
        return Value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized final void addPropertyListener(PropertyChangeListener<T, ListenableProperty<T>> Listener) {
        listeners.add(Listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized final void RemovePropertyListener(PropertyChangeListener<T, ListenableProperty<T>> Listener) {
        listeners.remove(Listener);
    }
}
