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
import java.util.Objects;
import javax.swing.JComponent;

/**
 *
 * this class allows and foster setting a POJO to notify in a
 * 1(ListenableProperty):N(UI elements) updates an Implementer Might provide a
 * 1:N support in two ways. but this class as it stands only support on way
 * Broadcast.
 *
 * @author Eduardo Vindas
 */
non-sealed abstract class UniDirBroadcastBind<T, C extends JComponent> extends BaseBinder<T, C> {

    /**
     * Creates a new instance of this CachedUniDirectionalBind class
     *
     * @param component_to_Bind the Component to Bind
     * @param BindablePojo the POJO To bind
     */
    protected UniDirBroadcastBind(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        super(component_to_Bind, BindablePojo);
        bound();
    }

    /**
     * binds what it can at this point and call the function so Implements can
     * bind their specific UI properties.
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
    }

    @Override
    public final void UnboundUIListener() {
        for (C WrappedComponent : WrappedComponents) {
            UnboundUIListener(WrappedComponent);
        }
    }

    public final T getUIValue() {
        return getUIValueFor(WrappedComponents.get(0));
    }

    /**
     * we don't consume this nor we want subclasses to consume it. 
     */
    @Override
    protected final void PropertyUpdated(T newValue){}

    public T getUIValueFor(int index) {
        return getUIValueFor(WrappedComponents.get(index));
    }

    @Override
    protected void setTheUIValue(T newValue) {
        for (C WrappedComponent : WrappedComponents) {
            setTheUIValue(WrappedComponent, newValue);
        }
    }

    /**
     * you COULD override this to enable Bidirectional changes. but personally
     * do not recommend.
     */
    @Override
    protected void updatePojo() {
        //do do Unicast. thus we dont update the POJO
    }

    /**
     * adds additional Components to listen for updates from the POJO
     *
     * @param Component
     * @return
     */
    public synchronized final boolean addComponent(C Component) {
        var result = WrappedComponents.add(Component);
        if (result) {
            BindUIListener(Component);
        }
        return result;
    }

    /**
     * Removes Components that will be notified for updates from the POJO
     *
     * @param Component
     */
    public synchronized final void removeComponent(C Component) {
        WrappedComponents.remove(Component);
        UnboundUIListener(Component);
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

}
