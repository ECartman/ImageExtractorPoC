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
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeongames.edi.utils.visual.UIlink;

import com.aeongames.edi.utils.Pojo.ListenableProperty;
import com.aeongames.edi.utils.Pojo.PropertyChangeListener;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * the Base Class to Bind a Visual Component to the ListenableProperty Pojo.
 * this define the basic functionality required this to work
 * this Base implementation only allows a 1:0, 0:1 or 1:1 binding. 
 * meaning that it can bind from one side to the other. or Two way update. 
 * but does NOT support 1:N 
 * for that please refer to  
 *
 * @author cartman
 */
abstract class BaseSwingBind<T, C extends JComponent> implements SwingComponentBind<T, C>, PropertyChangeListener<T, ListenableProperty<T>> {

    private final AtomicBoolean MutatingProperty = new AtomicBoolean(false);
    private final AtomicBoolean PendingUpdate = new AtomicBoolean(false);
    protected final C WrappedComponent;
    private final ListenableProperty<T> BoundPojo;

    private BaseSwingBind() {
        throw new IllegalCallerException("illegal construction");
    }

    /**
     * Creates a new instance of this SuperClass
     *
     * @param component_to_Bind the Component to Bind
     * @param BindablePojo the Pojo To bind
     */
    protected BaseSwingBind(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        Objects.requireNonNull(component_to_Bind, "the component to link cannot be null");
        Objects.requireNonNull(BindablePojo, "the component to link cannot be null");
        WrappedComponent = component_to_Bind;
        BoundPojo = BindablePojo;
        bound();
    }

    /**
     * binds what it can at this point and call the function so Implements can
     * bind their specific UI properties.
     */
    private void bound() {
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
        setTheUIValue(newValue);
        PendingUpdate.set(false);
        MutatingProperty.set(false);
        return true;
    }

    /**
     * call this function to update the POJO with the current value from the 
     * UI. 
     * thus function does some checkup for Concurrency and recurrence(as can lead to issues) 
     * then check if the value has not changed via {@link #getUIValue()} 
     * and if different calls the {@link ListenableProperty#updateProperty(java.lang.Object) }
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
        BoundPojo.updateProperty(currentUIValue);
        MutatingProperty.set(false);
    }

    private void SetUIPropertyDelayed(ListenableProperty<T> source) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalThreadStateException("this Function can only be called from EDT");
        }
        if (!Objects.equals(BoundPojo, source)) {
            return;
        }
        MutatingProperty.set(true);
        setTheUIValue(BoundPojo.getValue());
        PendingUpdate.set(false);
        MutatingProperty.set(false);
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
