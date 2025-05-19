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
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JComponent;

/**
 *
 * @author Eduardo
 */
public class MCBoolCompEnableBind extends MultiCastingSwingBind<Boolean, JComponent> {

    private static final String ENABLEDPROPERTY = "enabled";
    private final ReentrantLock LastestValueLock = new ReentrantLock(true);

    public MCBoolCompEnableBind(JComponent component_to_Bind, ListenableProperty<Boolean> BindablePojo) {
        super(component_to_Bind, BindablePojo);
    }

    @Override
    protected void setTheUIValue(JComponent Component, Boolean newValue) {
        Component.setEnabled(newValue);
    }

    @Override
    protected void UnboundUIListener(JComponent Component) {
       // Component.removePropertyChangeListener(MyPropertyListener);
    }

    /**
     * returns the Last Value set.
     *
     * @return
     */
    @Override
    public Boolean getUIValue() {
        boolean result;
        LastestValueLock.lock();
        try {
            result = getLinkedComponent().isEnabled();
        } finally {
            LastestValueLock.unlock();
        }
        return result;
    }

    @Override
    protected void BindUIListener(JComponent Component) {
       // Component.addPropertyChangeListener(EDITABLEPROPERTY, MyPropertyListener);
    }

}
