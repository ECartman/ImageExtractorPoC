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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import com.aeongames.edi.utils.pojo.ListenableProperty;

/**
 * this class Binds a
 * {@code PropertyChangeListener<String, ListenableProperty<String>} to an
 * {@code JTextComponent} in a 2 way bind. and can be used to update
 *
 * @author Eduardo
 */
public class JtextComponentBind extends BaseBiDirectionalBind<String, JTextComponent>{

    private final DocumentListener DocListener= new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePojo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePojo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePojo();
            }
        };

    public JtextComponentBind(JTextComponent component, ListenableProperty<String> pojo) {
        super(component,pojo);                
        BindUIListener();
    }
    
    @Override
    protected final void BindUIListener(){        
        getUIComponent().getDocument().addDocumentListener(DocListener);
    }
    
    @Override
    public final void UnboundUIListener(){
        getUIComponent().getDocument().removeDocumentListener(DocListener);
    }

    /**
     * return the Text of this property.or null if not found.
     * @return the value currently holding on the UI. null if none.
     */
    @Override
    public synchronized String getUIValue() {
        return getUIComponent().getText();
    }

    @Override
    protected synchronized void setTheUIValue(String newValue) {       
        getUIComponent().setText(newValue);
    }

}
