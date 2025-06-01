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
import javax.swing.JProgressBar;

/**
 *
 * @author Eduardo
 */
public class MCBoolProbarIndeterminate extends UniDirBroadcastBind<Boolean, JProgressBar> {

    private static final String ENABLEDPROPERTY = "indeterminate";
    private final boolean negated;

    public MCBoolProbarIndeterminate(JProgressBar component_to_Bind, ListenableProperty<Boolean> BindablePojo, boolean negated) {
        super(component_to_Bind, BindablePojo);
        this.negated = negated;
        /*
        for (var WrappedComponent : WrappedComponents) {
            BindUIListener(WrappedComponent);
        }
         */
    }

    public MCBoolProbarIndeterminate(JProgressBar component_to_Bind, ListenableProperty<Boolean> BindablePojo) {
        super(component_to_Bind, BindablePojo);
        this.negated = false;
        /*
        for (var WrappedComponent : WrappedComponents) {
            BindUIListener(WrappedComponent);
        }
         */
    }

    @Override
    protected void setTheUIValue(JProgressBar Component, Boolean newValue) {
        if (newValue != null) {
            Component.setIndeterminate(negated ? !newValue : newValue);
        }
    }

    @Override
    protected void UnboundUIListener(JProgressBar Component) {
        // Component.removePropertyChangeListener(MyPropertyListener);
    }

    @Override
    protected void BindUIListener(JProgressBar Component) {
        // Component.addPropertyChangeListener(EDITABLEPROPERTY, MyPropertyListener);
    }

    @Override
    protected Boolean getUIValueFor(JProgressBar component) {
        return component.isIndeterminate();
    }

}
