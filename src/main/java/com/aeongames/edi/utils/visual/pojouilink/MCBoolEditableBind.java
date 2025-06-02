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
import javax.swing.text.JTextComponent;

/**
 *
 * @author Eduardo
 */
public class MCBoolEditableBind extends UniDirBroadcastBind<Boolean, JTextComponent> {
    private final boolean negated;
    private static final String EDITABLEPROPERTY = "editable";

    public MCBoolEditableBind(JTextComponent component_to_Bind, ListenableProperty<Boolean> BindablePojo) {
        super(component_to_Bind, BindablePojo);
        negated=false;
        /**
         *
         * for (var WrappedComponent : WrappedComponents) {
         * BindUIListener(WrappedComponent); }
         */
    }
    
    public MCBoolEditableBind(JTextComponent component_to_Bind, ListenableProperty<Boolean> BindablePojo, boolean negated) {
        super(component_to_Bind, BindablePojo);
        this.negated=negated;
        /**
         *
         * for (var WrappedComponent : WrappedComponents) {
         * BindUIListener(WrappedComponent); }
         */
    }

    @Override
    protected void setTheUIValue(JTextComponent Component, Boolean newValue) {
        Component.setEditable(negated ? !newValue : newValue);
        Component.setEnabled(negated ? !newValue : newValue);
    }

    @Override
    protected void UnboundUIListener(JTextComponent Component) {
        // Component.removePropertyChangeListener(MyPropertyListener);
    }

    @Override
    protected void BindUIListener(JTextComponent Component) {
        // Component.addPropertyChangeListener(EDITABLEPROPERTY, MyPropertyListener);
    }

    @Override
    protected Boolean getUIValueFor(JTextComponent component) {
        return component.isEditable();
    }

}
