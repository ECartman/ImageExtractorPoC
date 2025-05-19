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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JLabel;

/**
 * this class Binds a
 * {@code PropertyChangeListener<String, ListenableProperty<String>} to an
 * {@code JTextComponent} in a 2 way bind. and can be used to update
 *
 * @author Eduardo
 */
public class JLabelComponentBind extends BaseSwingBind<String, JLabel> {
    private static final String LABELPROPERTY="text";
    private final PropertyChangeListener Listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(Objects.equals(evt.getPropertyName(),LABELPROPERTY)){
                updatePojo();
            }
        }
    };

    public JLabelComponentBind(JLabel component, ListenableProperty<String> pojo) {
        super(component, pojo);
    }

    @Override
    protected final void BindUIListener() {
        WrappedComponent.addPropertyChangeListener(LABELPROPERTY,Listener);
    }

    @Override
    public final void UnboundUIListener() {
        WrappedComponent.removePropertyChangeListener(Listener);
    }

    /**
     * return the Text of this property.or null if not found.
     *
     * @return the value currently holding on the UI. null if none.
     */
    @Override
    public synchronized String getUIValue() {
        return WrappedComponent.getText();
    }

    @Override
    protected synchronized void setTheUIValue(String newValue) {
        WrappedComponent.setText(newValue);
    }

}
