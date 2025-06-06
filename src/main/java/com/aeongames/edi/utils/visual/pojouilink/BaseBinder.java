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
package com.aeongames.edi.utils.visual.pojouilink;

import com.aeongames.edi.utils.pojo.ListenableProperty;
import com.aeongames.edi.utils.pojo.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * this is the BaseClass for Binders that bind POJO({@link ListenableProperty})
 * to VisualComponent is important to note this Class only defines a Binder they
 * <strong>can</strong> bind Bidirectionally, Unidirectional, 1:1 or 1:N and is
 * up to implementation if desire to further attempt to support more.
 * <br>
 *
 *
 * TODO: support Binding A error Listener. as some bindings might cause errors
 * when posting values to the UI, for example {@link JSpinner} can fail when
 * updating the value IllegalArgumentException - if value isn't allowed and we
 * have no way to tell the POJO Back that the Property Could not be updated
 *
 * @author Eduardo Vindas
 * @param <T> Type of Data to Handle
 * @param <C> The Type of UI that subclass from JComponent.
 */
public abstract sealed class BaseBinder<T, C extends JComponent> implements PropertyChangeListener<T, ListenableProperty<T>>
        permits BaseBiDirectionalBind, CachedUniDirectionalBind,UniDirBroadcastBind {

    /**
     * the Underline Java Property to bound to the Component. this is a Object
     * of type {@link ListenableProperty} that has code get, set and listen to
     * changes on its property.
     */
    protected final ListenableProperty<T> BoundPojo;
    /**
     * A flag that notifies if there Bind is attempting to Mutate (change) the
     * value on either side. (The UI or the POJO)
     * <br>
     * and if set and we reenter from the Notification functions that we should
     * avoid change the value a new in order to avoid infinite Recursion and
     * Deadlocks.
     */
    protected final AtomicBoolean MutatingProperty = new AtomicBoolean(false);
    /**
     * this flag allow us to determine if the value has changed. and we
     * requested to be updated Asynchronous (from one thread to the Event
     * Dispatch) but the Event Dispatch has yet to run the update.
     */
    protected final AtomicBoolean PendingUpdate = new AtomicBoolean(false);
    /**
     * a list of UI Components of type {@code C} that inherits
     * {@code JComponent} and is bound on this Binding. (it could 1 or many.)
     * the implementer might or might not support binding one or more.
     */
    protected final ArrayList<C> WrappedComponents;

    /**
     * Creates a Instance of {@link BaseBiDirectionalBind} that check for the
     * objects on the parameters are not null and then Bounds them.
     *
     * @param component_to_Bind the UI Component to Bind
     * @param BindablePojo the POJO To bind
     */
    protected BaseBinder(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        Objects.requireNonNull(component_to_Bind, "the component to link cannot be null");
        Objects.requireNonNull(BindablePojo, "the component to link cannot be null");
        WrappedComponents = new ArrayList<>();
        WrappedComponents.add(component_to_Bind);
        BoundPojo = BindablePojo;
    }

    /**
     * Handles The Changes From the POJO({@link ListenableProperty}) object
     * checks if the source is the correct instance.
     *
     * @param source the source of the event, on this implementation it
     * <strong>must</strong> be equals than the BoundPOJO
     * @param newValue the new Value to set. if this Thread is EDT it will set
     * this value to the UI directly. otherwise will request a Invocation to
     * update the UI later on. on which case the value used will be pulled from
     * the POJO. <br>
     * <strong>NOTE:</strong> the update can happens later on and the bean might
     * change its property in between and the current value is what will be set.
     */
    @Override
    public final void propertyChanged(ListenableProperty<T> source, T newValue) {
        if (source != BoundPojo) {
            throw new IllegalStateException("the Source does not match the Glued Object");
        }
        newValue = swapNull(newValue);
        if (Objects.isNull(newValue) && !allowNullUpdate()) {
            return;
        }
        PropertyUpdated(newValue);
        //if we are running on the EDT. call the process method.
        //otherwise if not "signaling" signal to process the pending queue. 
        if (SwingUtilities.isEventDispatchThread()) {
            SetUIProperty(newValue);
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

    /**
     * checks if the value is already mutating if so we return as that means we
     * are falling into a recursion. otherwise we set the flags for recursion
     * and request the change to be done on the UI
     *
     * @param newValue the value to set into the UI
     * @return if succeeds or not.
     */
    private boolean SetUIProperty(T newValue) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalThreadStateException("this Function can only be called from EDT");
        }
        if (MutatingProperty.get()) {
            return false;
        }
        MutatingProperty.set(true);
        try {
            setTheUIValue(newValue);
        } finally {
            PendingUpdate.set(false);
            MutatingProperty.set(false);
        }
        return true;
    }

    /**
     * this function runs a delayed task to update the UI component(s)
     *
     * @param source the source of the change.
     */
    private void SetUIPropertyDelayed(ListenableProperty<T> source) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalThreadStateException("this Function can only be called from EDT");
        }
        if (!Objects.equals(BoundPojo, source)) {
            return;
        }
        synchronized (BoundPojo) {
            MutatingProperty.set(true);
            try {
                setTheUIValue(BoundPojo.getValue());
            } finally {
                PendingUpdate.set(false);
                MutatingProperty.set(false);
            }
        }
    }

    /**
     * Removes the Binding(s) From the UI Component and the POJO. the specifics
     * on how this is done please review the specific Implementation.
     */
    public final void Unbound() {
        BoundPojo.RemovePropertyListener(this);
        UnboundUIListener();
    }

    /**
     * returns a UNMODIFIABLE list of Components to which updates will be
     * broadcast
     *
     * @return a unmodifiable list of components
     */
    public final List<C> getLinkedComponents() {
        return Collections.unmodifiableList(WrappedComponents);
    }

    /**
     * this Method Defines if we should send Null updates to the UI. in general
     * terms RARELY you would desire to do that. and thus by default this
     * function will return false. if your binding. and your UI can Handle
     * processing Null references as Updates please Override this Function
     *
     * @return false by default we ignore changes where the New value is null.
     */
    protected boolean allowNullUpdate() {
        return false;
    }

    /**
     * this function check if the value is null and if so and calls
     * {@link #nullRemplacement()} that provide a default value to use instead
     * of null by default it still returns null and might later on check on
     * {@link #allowNullUpdate()}
     *
     * @param newValue the value to check
     * @return the value to use if the NewValue is null (the result can still be
     * null)
     */
    protected final T swapNull(final T newValue) {
        return Objects.isNull(newValue) ? nullRemplacement() : newValue;
    }

    /**
     * returns a instance of {@code T} that is to be used when the newValue sent
     * from the POJO into the UI is null. null as a result is acceptable
     *
     * @return Null by default
     */
    protected T nullRemplacement() {
        return null;
    }

    /**
     * this method gets notified when a new value from the POJO arrived.
     * and intended to use for sub task before updating the ui
     * this is in order for Sub classes to use.
     * @param newValue 
     */
    protected void PropertyUpdated(T newValue) {
    }

    /**
     * this function is called to Unbound the Listener(s) of the UI to this
     * class. this is done when the class is not longer required to listen or
     * trigger data.
     */
    protected abstract void UnboundUIListener();

    /**
     * request the UI element(s) to be updated with the provided value
     *
     * @param newValue the new value to set
     */
    protected abstract void setTheUIValue(T newValue);

    /**
     * call this function to update the POJO with the current value from the UI.
     * thus function does some checkup for Concurrency and recurrence(as can
     * lead to issues) then check if the value has not changed via
     * {@link #getUIValue()} and if different calls the {@link ListenableProperty#updateProperty(java.lang.Object)
     * }
     */
    protected abstract void updatePojo();
}
