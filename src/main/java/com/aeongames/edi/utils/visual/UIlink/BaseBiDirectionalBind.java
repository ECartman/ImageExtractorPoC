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
import java.util.Objects;
import javax.swing.JComponent;

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
 * binding. please refer to {@link CachedUniDirectionalBind } or
 * {@link UniDirBroadcastBind}
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
non-sealed abstract class BaseBiDirectionalBind<T, C extends JComponent> extends BaseBinder<T, C> {

    /**
     * Creates a Instance of {@link BaseBiDirectionalBind} that check for the
     * objects on the parameters are not null and then Bounds them.
     *
     * @param component_to_Bind the UI Component to Bind
     * @param BindablePojo the POJO To bind
     */
    protected BaseBiDirectionalBind(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        super(component_to_Bind, BindablePojo);
        bind();
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
    private final void bind() {
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
    }
       
    /**
     * we don't consume this nor we want subclasses to consume it. 
     */
    protected final void PropertyUpdated(T newValue){}

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
    
    /**
     * gathers the UIComponent for this Binder.
     * @return the {code C} for this binder.
     */
    public C getUIComponent() {
        return WrappedComponents.get(0);
    }

    /**
     * Gets the Value of {@code T} type. from the UI Component.
     *
     * @return the current Property value from this UI Component that is
     * bounded.
     */
    protected abstract T getUIValue();

    /**
     * Runs the code that is necessary to bound the Specific UI class to this
     * class to listen or trigger when a change is made on the UI. this is
     * highly relative on the component or desired Property to Listen
     */
    protected abstract void BindUIListener();


}
