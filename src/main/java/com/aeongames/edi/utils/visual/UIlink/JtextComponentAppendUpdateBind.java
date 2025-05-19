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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * this class is a Binding that Works only on One Way (towards the UI. any
 * change made from the UI will be ignored. furthermore this Implementation is
 * intended for Appending new text to a text Component, whenever the update is
 * done on the UI side we don't care. also the Bounded property might or might
 * not be logging or keeping track of the changes. EACH UPDATE is intended to be
 * understood as a new chunk of data that needs to be added into the component.
 *
 * @author Eduardo Vindas
 */
public class JtextComponentAppendUpdateBind extends BaseSwingBind<String, JTextComponent> {

    public JtextComponentAppendUpdateBind(JTextComponent component, ListenableProperty<String> pojo) {
        super(component, pojo);
    }

    /**
     * return the Text of this property.or null if not found.
     *
     * @return the value currently holding on the UI. null if none.
     */
    @Override
    public String getUIValue() {
        return WrappedComponent.getText();
    }

    @Override
    protected void BindUIListener() {
        //we dont need to bind the UI. we wont Listen to changes from UI. 
    }

    @Override
    protected void UnboundUIListener() {
        //we dont need to Unbound. 
    }

    /**
     * we don't set the value. we APPEND the value.
     * @param newValue
     */
    @Override
    protected void setTheUIValue(String newValue) {
        var doc = WrappedComponent.getDocument();
        try {
            doc.insertString(doc.getLength(), newValue, null);
        } catch (BadLocationException ex) {
        }
    }

}
