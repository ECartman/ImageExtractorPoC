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
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JComponent;

/**
 *
 * this is A CachedUnidirectionalBind that binds
 * POJO({@link ListenableProperty}) to VisualComponent, is important to note
 * this Class defines a Binder that
 * <strong>can</strong> bind Bidirectionally, However is not ready to do So By
 * default and it is up to the specific implementation to glue the listeners to
 * do so, for Bidirectional Implementation please refer to
 * {@link BaseBiDirectionalBind} <br>
 *
 * the default Binding For this Class is Unidirectional Caching Values from the
 * POJO to the UI. in a way that matches<br>
 * 1(POJO):N(UI Components) (1:N)<br>
 * <br>
 * <strong>NOTE:</strong> this implementation caches changes from the property
 * and then they are unloaded one by one at the same time into the Component.
 * they are not concatenated. rather they are barraged into each element one at
 * the time. (similar to a card dealer in poker, with the caveat that the dealer
 * on this case provides a copy of the same card to all players)
 * <br>
 * this method is suggested for those components that append changed into them
 * for example a Text Area that keep logs of activities. and can be used for a
 * single component or multiple.
 * <br>
 * TODO: support Binding A error Listener.
 *
 * @author Eduardo Vindas
 * @param <T> Type of Data to Handle
 * @param <C> The Type of UI that subclass from JComponent.
 */
non-sealed abstract class CachedUniDirectionalBind<T, C extends JComponent> extends BaseBinder<T, C> {

    /**
     * a Read write lock to protect the LinkedList Insertions and reads.
     */
    private final ReentrantReadWriteLock ListLock = new ReentrantReadWriteLock(true);
    /**
     * a cache of changes made on the property to throw to the UI.
     */
    private final LinkedList<T> CachedPendingTransferData;

    /**
     * Creates a new instance of this CachedUniDirectionalBind class
     *
     * @param component_to_Bind the Component to Bind
     * @param BindablePojo the POJO To bind
     */
    protected CachedUniDirectionalBind(C component_to_Bind, ListenableProperty<T> BindablePojo) {
        super(component_to_Bind, BindablePojo);
        CachedPendingTransferData = new LinkedList<>();
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

    public T getUIValue() {
        return getUIValueFor(WrappedComponents.get(0));
    }

    /**
     * record the Change into the Cache. 
     * @param newValue
     */
    @Override
    protected final void PropertyUpdated(T newValue) {
        ListLock.writeLock().lock();
        try {
            CachedPendingTransferData.add(newValue);
        } finally {
            ListLock.writeLock().unlock();
        }
    }

    public T getUIValueFor(int index) {
        return getUIValueFor(WrappedComponents.get(index));
    }
    
   @Override
    protected void setTheUIValue(T newValue) {
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
    }

    /**
     * you COULD override this to enable Bidirectional changes.
     * but personally do not recommend. 
     */
    @Override
    protected void updatePojo() {
        //do do Unicast. thus we dont update the POJO
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
