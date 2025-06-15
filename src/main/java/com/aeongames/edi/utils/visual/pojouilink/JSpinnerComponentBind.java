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
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Eduardo Vindas
 */
public class JSpinnerComponentBind extends BaseBiDirectionalBind<Integer, JSpinner> {

    private final ChangeListener SpinnerchangeListener = (var e) -> {
        updatePojo();
    };

    public JSpinnerComponentBind(JSpinner component, ListenableProperty<Integer> pojo) {
        super(component, pojo);              
        BindUIListener();
    }

    @Override
    public Integer getUIValue() {
        var currentUIValue = WrappedComponents.get(0).getModel().getValue();
        if (currentUIValue instanceof Integer UnderlineValue) {
            return UnderlineValue;
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void setTheUIValue(Integer newValue) {
        getUIComponent().getModel().setValue(newValue);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected final void BindUIListener() {
        getUIComponent().getModel().addChangeListener(SpinnerchangeListener);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void UnboundUIListener() {
       getUIComponent().getModel().removeChangeListener(SpinnerchangeListener);
    }

}
