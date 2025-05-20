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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * this is A Bidirectional POJO({@link ListenableProperty}) to VisualComponent
 * bind Class. is important to note this Class defines a Binder that
 * <strong>can</strong>
 * bind Bidirectionally. however is up to the specific implementation if the
 * class notifies back and forth the changes on the UI to POJO and the other way
 * it is also important to note that this class defines a Bidirectional Bind to
 * 1:1
 * <br>
 * meaning it can bind up to 1 POJO and up to 1 UI component. if You need a 1:N
 * binding. please refer to {@link MultiCastingSwingBind} or
 * {@link CachedSwingBind}
 * <br>
 * please note that this class does not cache the changes. and does not
 * guarantee that the value would stay unchanged until updated on the UI. (this
 * means that the property <strong>could</strong> change between the time the
 * property request a change. and the time the UI updates, the UI will setup the
 * value that is currently at {@link ListenableProperty#getValue}
 * <br>
 * thus. this Binding is not recommended to be used when you require to update
 * multiple Components with the same Property.
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
abstract class BaseBiDirectionalBind<T, C extends JComponent> implements SwingComponentBind<T, C>, PropertyChangeListener<T, ListenableProperty<T>> {

    /**
     * A flag that notifies if there Bind is attempting to Mutate (change) the
     * value on either side. (The UI or the POJO)
     * <br>
     * and if set and we reenter from the Notification functions that we should
     * avoid change the value a new in order to avoid infinite Recursion and
     * Deadlocks.
     */
    private final AtomicBoolean MutatingProperty = new AtomicBoolean(false);
    /**
     * this flag allow us to determine if the value has changed. and we
     * requested to be updated Asynchronous (from one thread to the Event
     * Dispatch) but the Event Dispatch has yet to run the update.
     */
    private final AtomicBoolean PendingUpdate = new AtomicBoolean(false);
    /**
     * the UnderLine UI Component of type {@code C} that inherits
     * {@code JComponent} and is bound on this Binding.
     */
    protected final C WrappedComponent;
    /**
     * the Underline Java Property to bound to the Component. this is a Object
     * of type {@link ListenableProperty} that has code get, set and listen to
     * changes on its property.
     */
    private final ListenableProperty<T> BoundPojo;

    /**
     * @throws IllegalCallerException this Constructor Should NEVER be called.
     * @hidden
     */
    private BaseBiDirectionalBind() {
        throw new IllegalCallerException("illegal construction");
    }

    /**
     * Creates a Instance of {@link BaseBiDirectionalBind} that check for the
     * objects on the parameters are not null and then Bounds them.
     *
     * @param component_to_Bind the UI Component to Bind
     * @param BindablePojo the POJO To bind
     */
    protected BaseBiDirectionalBind(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        Objects.requireNonNull(component_to_Bind, "the component to link cannot be null");
        Objects.requireNonNull(BindablePojo, "the component to link cannot be null");
        WrappedComponent = component_to_Bind;
        BoundPojo = BindablePojo;
        bound();
    }

    /**
     * Binds the POJO, Listening for changes and sending request to Update the
     * UI with the new data. and <strong>requests</strong> to bind the UI
     * component to listen for changes. it might be possible for the UI
     * component to no send or register a binding and1 thus it became
     * Unidirectional thus please review {@link BindUIListener} on the specific
     * Implementation to check if the Listeners are bounded.
     * <br>
     * also check the POJO Value and UI are equals if they are not. and the POJO
     * value is null. it sends a quest to the POJO to adopt the Value from the
     * UI.
     *
     */
    private void bound() {
        //before binding. we should check if both have the same value. 
        T POJOvalue = BoundPojo.getValue();
        T UIvalue = getUIValue();
        if (!Objects.equals(POJOvalue, UIvalue)) {
            if (Objects.isNull(POJOvalue)) {
                //if the Pojo is null we Sync it to the UI value. 
                BoundPojo.setValue(UIvalue);
            }
            //Should we update if the values are not null but do not match??? 
        }
        BoundPojo.addPropertyListener(this);
        BindUIListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void Unbound() {
        BoundPojo.RemovePropertyListener(this);
        UnboundUIListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final C getLinkedComponent() {
        return WrappedComponent;
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
        if (source != BoundPojo) {// it must be the same reference.
            throw new IllegalStateException("the Source does not match the Bound Object");
        }
        // in theory BoundPojo.getValue() == newValue but we dont need to check that. 
        newValue = swapNull(newValue);
        if (Objects.isNull(newValue) && !allowNullUpdate()) {
            return;
        }
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
     * sets the value to the underline component this should only be trigger by
     * a Linker
     *
     * @param newValue the value to set into the UI
     * @return if succeeds or not.
     */
    protected final boolean SetUIProperty(T newValue) {
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
     * call this function to update the POJO with the current value from the UI.
     * thus function does some checkup for Concurrency and recurrence(as can
     * lead to issues) then check if the value has not changed via
     * {@link #getUIValue()} and if different calls the {@link ListenableProperty#updateProperty(java.lang.Object)
     * }
     */
    protected final void updatePojo() {
        //if WE are setting the value we should not notify back otherwise 
        //we end on a deadlock and likely unnecesary update. 
        if (MutatingProperty.get()) {
            return;
        }
        var currentPojoValue = BoundPojo.getValue();
        var currentUIValue = getUIValue();
        //no update bail
        if (Objects.equals(currentPojoValue, currentUIValue)) {
            return;
        }
        MutatingProperty.set(true);
        try {
            BoundPojo.updateProperty(currentUIValue);
        } finally {
            MutatingProperty.set(false);
        }
    }

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
                MutatingProperty.set(false);
            }
            MutatingProperty.set(false);
        }
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
    private T swapNull(final T newValue) {
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

    protected abstract void setTheUIValue(T newValue);

    /**
     * Runs the code that is necessary to bound the Specific UI class to this
     * class to listen or trigger when a change is made on the UI. this is
     * highly relative on the component or desired Property to Listen
     */
    protected abstract void BindUIListener();

    /**
     * this function is called to Unbound the Listener of the UI to this class.
     * this is done when the class is not longer required to listen or trigger
     * data.
     */
    protected abstract void UnboundUIListener();

}
