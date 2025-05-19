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
import javax.swing.text.JTextComponent;

/**
 *
 * @author Eduardo
 */
public class MCBoolEditableBind extends MultiCastingSwingBind<Boolean, JTextComponent> {

    private static final String EDITABLEPROPERTY = "editable";
    private final ReentrantLock LastestValueLock = new ReentrantLock(true);

    public MCBoolEditableBind(JTextComponent component_to_Bind, ListenableProperty<Boolean> BindablePojo) {
        super(component_to_Bind, BindablePojo);
    }

    @Override
    protected void setTheUIValue(JTextComponent Component, Boolean newValue) {
        Component.setEditable(newValue);
        Component.setEnabled(newValue);
    }

    @Override
    protected void UnboundUIListener(JTextComponent Component) {
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
            result = getLinkedComponent().isEditable();
        } finally {
            LastestValueLock.unlock();
        }
        return result;
    }

    @Override
    protected void BindUIListener(JTextComponent Component) {
       // Component.addPropertyChangeListener(EDITABLEPROPERTY, MyPropertyListener);
    }

}
