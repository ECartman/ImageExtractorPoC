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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * also this class is Sync using ReentrantReadWriteLock Sync.
 * and thus.reading and setting the value will cause the object to lock for read
 * or write depending on the action, also the locking mechanism is finer meaning 
 * that the updates are done with the lowest holdup as possible, meaning that threads
 * will be waited as little as possible, but also changes to the value can happen
 * mid notification task and thus meaning that the Listeners will NOT get notified
 * each time the value changes but notified if it changes and might be notified with
 * the latest version.
 * for this reason we encourage to check using {@link FastPropertyPojo#getValue()}
 * to get the current value from the property instead of the cached value.
 *
 * @author Eduardo Vindas
 * @param <T> the Property Type Class that represent the value this property
 * holds
 */
public final class FastPropertyPojo<T> implements ListenableProperty<T> {

    /**
     * property lock that guard for syncro in a more finer control
     */
    private final ReentrantReadWriteLock PropertyLock;
    /**
     * lock to guard the changes on the UpdateID.
     */
    private final ReentrantReadWriteLock UpdateIDLock;

    /**
     * a integer that holds a value that tell us which iteration or id of a
     * change
     */
    private volatile int updateID = Integer.MIN_VALUE;

    /**
     * the underline Value to hold for this Property
     */
    private T Value;
    /**
     * a Map that contains a set of listeners that contains the Integer value
     * with the ID of the last UpdateID notified. this is useful to track of the
     * Listener needs to be notified of other changes
     *
     */
    private final Map<PropertyChangeListener<T, ListenableProperty<T>>, Integer> listenersTable;

    /**
     * creates a new Instance of a FastProperty POJO. the underline Property is
     * unset (null);
     */
    public FastPropertyPojo() {
        PropertyLock = new ReentrantReadWriteLock(true);
        UpdateIDLock = new ReentrantReadWriteLock(true);
        listenersTable = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * creates a new Instance of a FastPropertyPojo. and sets the Value property
     * to the Provided value.
     *
     * @param initialValue the Initial Value to set.
     */
    public FastPropertyPojo(T initialValue) {
        this();
        Value = initialValue;
    }

    /**
     * try to get the Write lock to change the property. if unable to
     * immediately get the lock. it fails immediately to change the value and
     * thus the value is unchanged and returns false. otherwise if able to held
     * the lock for write and then change the value and trigger the change
     * notify the listeners.
     *
     * @param newValue the new value to set.
     * @return true if able to lock and make the change immediately. if unable
     * returns false a no change takes place.
     */
    @Override
    public boolean tryUpdateProperty(T newValue) {
        boolean holdsValueLock = false;
        boolean holdsIDLock = false;
        try {
            holdsValueLock = PropertyLock.writeLock().tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(FastPropertyPojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            holdsIDLock = UpdateIDLock.writeLock().tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(FastPropertyPojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        //***************************************************************//
        if (holdsValueLock && holdsIDLock) {
            try {
                Value = newValue;
                UpdateIDLock.writeLock().lock();//we have the value lock. we also need to lock the updateid
                updateID = updateID == Integer.MAX_VALUE ? Integer.MIN_VALUE : updateID + 1;
            } finally {
                UpdateIDLock.writeLock().unlock();
                PropertyLock.writeLock().unlock();
            }
            firePropertyChanged();
            return true;
        } else {
            if (holdsValueLock) {
                PropertyLock.writeLock().unlock();
            }
            if (holdsIDLock) {
                UpdateIDLock.writeLock().unlock();
            }
            return false;
        }
    }

    /**
     * locks or waits (*until able to held the lock) to change the value and
     * changes the value. afterwards unlocking and immediately firing change to
     * the listeners.
     *
     * @param newValue the new value to set.
     */
    @Override
    public void setValue(T newValue) {
        PropertyLock.writeLock().lock();
        UpdateIDLock.writeLock().lock();
        try {
            Value = newValue;
            updateID = updateID == Integer.MAX_VALUE ? Integer.MIN_VALUE : updateID + 1;
        } finally {
            PropertyLock.writeLock().unlock();
            UpdateIDLock.writeLock().unlock();
        }
        firePropertyChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue() {
        T Heldvalue;
        PropertyLock.readLock().lock();
        try {
            Heldvalue = Value;
        } finally {
            PropertyLock.readLock().unlock();
        }
        return Heldvalue;
    }

    /**
     *
     * @return the value that we held on this property. or null if we could not
     * immediately. get the lock. WARNING: the underline value could be null.
     * and thus thus function should be called only if you are willing to accept
     * that the return value could be inaccurate but returns as fast as possible
     */
    public T tryGetValue() {
        T Heldvalue;
        //to read the value we care not for fairness
        var locked = PropertyLock.readLock().tryLock();
        if (!locked) {
            return null;
        }
        try {
            Heldvalue = Value;
        } finally {
            PropertyLock.readLock().unlock();
        }
        return Heldvalue;
    }

    /**
     * iterates over the listeners and check if this class has already notified 
     * the latest change. if so continue without notify this listener as it already
     * receive the last update.
     * if not then notifies the listener with the Current value of this property
     * WARNING this value is immediately deprecated. meaning that during the call to
     * the listener the value can change. it is suggested to either call {@link getValue()}
     * instead to get the current value.
     */
    protected final void firePropertyChanged() {
        for (PropertyChangeListener<T, ListenableProperty<T>> listener : listenersTable.keySet()) {
            UpdateIDLock.readLock().lock();
            if (listenersTable.get(listener) == updateID) {
                //if the updateID is equal we alredy notified the lastest value
                //this could happend as the value is tighly sync. and thus 
                // a call to setValue happend while firePropertyChanged was running
                //on another thread. and it notified midway the lastest value. 
                // this is acceptable by this class. as it is intended to notify
                // as fast as posible the lastest change. 
                //thus. if the lastest update was alredy notified. nothing to do 
                //here and therefore release lock and check the rest of the items.
                UpdateIDLock.readLock().unlock();
                continue;
            }
            final T value;
            try {
                listenersTable.put(listener, updateID);
                value = getValue();
            } finally {
                UpdateIDLock.readLock().unlock();
            }
            listener.propertyChanged(this, value);
        }
    }

    /**
     * gets a READ only list of the list that is hosted at this object. Warning
     * this set is sync. and thus might lock the listener table.
     *
     * @return READ ONLY list with the listeners for this object
     */
    public Set<PropertyChangeListener<T, ListenableProperty<T>>> getListeners() {
        return Collections.unmodifiableSet(listenersTable.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addPropertyListener(PropertyChangeListener<T, ListenableProperty<T>> Listener) {
        // the map itself handles syncronization so we dont need to lock or handle
        //sync here.
        listenersTable.put(Listener, updateID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void RemovePropertyListener(PropertyChangeListener<T, ListenableProperty<T>> Listener) {
        // the map itself handles syncronization so we dont need to lock or handle
        //sync here.
        listenersTable.remove(Listener);
    }

}
