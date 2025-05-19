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
package com.aeongames.edi.utils.visual.UIlink;

import com.aeongames.edi.utils.Pojo.ListenableProperty;
import com.aeongames.edi.utils.Pojo.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * a Caching Binding a Visual Component to the ListenableProperty POJO.this
 * differ from {@link BaseSwingBind} as this version Catches the changes from
 * the POJO and when able unloads all updates into the UI
 * component.functionality required this to work this Caching implementation
 * supports 1 -> 1 Unidirectional and 1 -> N unidirectional for that please
 * refer to
 *
 * @author Eduardo Vindas
 * @param <T> Type of Data to Handle
 * @param <C> The Type of UI that subclass from JComponent.
 */
abstract class CachedSwingBind<T, C extends JComponent> implements SwingComponentBind<T, C>, PropertyChangeListener<T, ListenableProperty<T>> {

    private final ReentrantReadWriteLock ListLock = new ReentrantReadWriteLock(true);
    private final AtomicBoolean MutatingProperty = new AtomicBoolean(false);
    private final AtomicBoolean PendingUpdate = new AtomicBoolean(false);
    private final ArrayList<C> WrappedComponents;
    private final LinkedList<T> CachedPendingTransferData;
    protected final ListenableProperty<T> BoundPojo;

    private CachedSwingBind() {
        throw new IllegalCallerException("illegal construction");
    }

    /**
     * Creates a new instance of this SuperClass
     *
     * @param component_to_Bind the Component to Bind
     * @param BindablePojo the POJO To bind
     */
    protected CachedSwingBind(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        Objects.requireNonNull(component_to_Bind, "the component to link cannot be null");
        Objects.requireNonNull(BindablePojo, "the component to link cannot be null");
        WrappedComponents = new ArrayList<>();
        CachedPendingTransferData = new LinkedList<>();
        WrappedComponents.add(component_to_Bind);
        BoundPojo = BindablePojo;
        bound();
    }

    /**
     * binds what it can at this point and call the function so Implements can
     * bind their specific UI properties.
     */
    private void bound() {
        BoundPojo.addPropertyListener(this);
        for (C WrappedComponent : WrappedComponents) {
            BindUIListener(WrappedComponent);
        }
    }

    public List<C> getLinkedComponents() {
        return Collections.unmodifiableList(WrappedComponents);
    }

    @Override
    public C getLinkedComponent() {
        return WrappedComponents.get(0);
    }

    @Override
    public void Unbound() {
        BoundPojo.RemovePropertyListener(this);
        for (C WrappedComponent : WrappedComponents) {
            UnboundUIListener(WrappedComponent);
        }
    }

    @Override
    public T getUIValue() {
        return getUIValueFor(getLinkedComponent());
    }

    /**
     * Handle change From the POJO
     *
     * @param source
     * @param newValue
     */
    @Override
    public final void propertyChanged(ListenableProperty<T> source, T newValue) {
        if (source != BoundPojo) {
            throw new IllegalStateException("the Source does not match the Glued Object");
        }
        if (!Objects.equals(source, BoundPojo)) {
            return;//it is fine if we get a notification for something we dont care. we just dont process it as we dont care.
        }
        if (Objects.isNull(newValue)) {
            return;
        }
        ListLock.writeLock().lock();
        try {
            CachedPendingTransferData.add(newValue);
        } finally {
            ListLock.writeLock().unlock();
        }
        //if we are running on the EDT. call the process method.
        //otherwise if not "signaling" signal to process the pending queue. 
        if (SwingUtilities.isEventDispatchThread()) {
            SetUIProperty();
        } else {
            //if we have a update booked alredy dont book a new one. 
            if (PendingUpdate.get()) {
                return;
            }
            PendingUpdate.set(true);
            SwingUtilities.invokeLater(() -> {
                SetUIPropertyDelayed(source);
            });
        }
    }

    private void SetUIPropertyDelayed(ListenableProperty<T> source) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalThreadStateException("this Function can only be called from EDT");
        }
        if (!Objects.equals(BoundPojo, source)) {
            return;
        }
        processChange();
    }

    /**
     * sets the value to the underline component this should only be trigger by
     * a Linker
     *
     * @param newValue the value to set into the UI
     * @return if succeeds or not.
     */
    protected final boolean SetUIProperty() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalThreadStateException("this Function can only be called from EDT");
        }
        if (MutatingProperty.get()) {
            return false;
        }
        processChange();
        return true;
    }

    public T getUIValueFor(int index) {
        return getUIValueFor(WrappedComponents.get(index));
    }

    /**
     * Runs the code that is necessary to bound the Specific UI class to this
     * class to listen or trigger when a change is made on the UI. this is
     * highly relative on the component or desired Property to Listen
     */
    protected abstract void BindUIListener(C Component);

    /**
     * this function is called to Unbound the Listener of the UI to this class.
     * this is done when the class is not longer required to listen or trigger
     * data.
     */
    protected abstract void UnboundUIListener(C Component);

    protected abstract T getUIValueFor(C component);

    protected abstract void setTheUIValue(C Component, T newValue);

    private void processChange() {
        MutatingProperty.set(true);
        ListLock.readLock().lock();
        try {
            T val;
            while ((val = CachedPendingTransferData.poll()) != null) {
                for (C WrappedComponent : WrappedComponents) {
                    setTheUIValue(WrappedComponent, val);
                }
            }
        } finally {
            ListLock.readLock().unlock();
        }
        PendingUpdate.set(false);
        MutatingProperty.set(false);
    }

}
