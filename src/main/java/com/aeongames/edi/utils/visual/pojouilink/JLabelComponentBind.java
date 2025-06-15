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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JLabel;

/**
 * A {@link BaseBiDirectionalBind} that binds a {@link JLabel} and a
 * {@link ListenableProperty} in a 1:1 bidirectional Binding. It would be Rare
 * for a {@link JLabel} to send updates <strong>From</strong>
 * the UI into the Bean. but if required it should work just fine.
 *
 * @see BaseBiDirectionalBind
 * @see JLabel
 * @author Eduardo Vindas
 */
public class JLabelComponentBind extends BaseBiDirectionalBind<String, JLabel> {

    private static final String LABELPROPERTY = "text";
    private final PropertyChangeListener Listener = (PropertyChangeEvent evt) -> {
        if (Objects.equals(evt.getPropertyName(), LABELPROPERTY)) {
            updatePojo();
        }
    };

    /**
     * Creates a Instance of {@link BaseBiDirectionalBind} that check for the
     * objects on the parameters are not null and then Bounds them.
     *
     * @see BaseBiDirectionalBind#BaseBiDirectionalBind(javax.swing.JComponent,
     * com.aeongames.edi.utils.Pojo.ListenableProperty)
     * @param component the {@link JLabel} to Bind
     * @param pojo the {@link ListenableProperty} to bind
     */
    public JLabelComponentBind(JLabel component, ListenableProperty<String> pojo) {
        super(component, pojo);
        BindUIListener();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected final void BindUIListener() {
        getUIComponent().addPropertyChangeListener(LABELPROPERTY, Listener);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final void UnboundUIListener() {
        getUIComponent().removePropertyChangeListener(Listener);
    }

    /**
     * return the text for the Bounded {@link JLabel}
     * @return the value currently holding on the UI. null if none.
     */
    @Override
    public synchronized String getUIValue() {
        return getUIComponent().getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void setTheUIValue(String newValue) {
        getUIComponent().setText(newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String nullRemplacement() {
        return "";
    }
    
    

}
