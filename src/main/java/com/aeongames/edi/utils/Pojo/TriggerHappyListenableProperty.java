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
 * this is a Abstract class that Pass on ListenableProperty and defines
 * Functions to trigger events to the Listeners for changes in properties
 *
 * @author Eduardo Vindas
 * @param <T> the Property Type Class that represent the value this property
 * holds
 */
public abstract class TriggerHappyListenableProperty<T> implements ListenableProperty<T> {

    private T Value;
    private final List<PropertyChangeListener<T, ListenableProperty<T>>> listeners;

    /**
     * gets a READ only list of the list that is hosted at this object.
     *
     * @return READ ONLY list with the listeners for this object
     */
    public List<PropertyChangeListener<T, ListenableProperty<T>>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    protected TriggerHappyListenableProperty() {
        listeners = new ArrayList<>();
    }

    protected final void firePropertyChanged() {
        for (PropertyChangeListener<T, ListenableProperty<T>> listener : listeners) {
            listener.propertyChanged(this, getValue());
        }
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
