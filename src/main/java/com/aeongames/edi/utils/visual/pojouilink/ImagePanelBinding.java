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
import com.aeongames.edi.utils.visual.Panels.ImagePanel;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

/**
 * 
 * @author Eduardo
 */
public class ImagePanelBinding extends BaseBiDirectionalBind<BufferedImage, ImagePanel>{
    
    /**
     * Creates a Instance of {@link BaseBiDirectionalBind} that check for the
     * objects on the parameters are not null and then Bounds them.
     *
     * @see BaseBiDirectionalBind#BaseBiDirectionalBind(javax.swing.JComponent,
     * com.aeongames.edi.utils.Pojo.ListenableProperty)
     * @param component the {@link JLabel} to Bind
     * @param pojo the {@link ListenableProperty} to bind
     */
    public ImagePanelBinding(ImagePanel component, ListenableProperty<BufferedImage> pojo) {
        super(component, pojo);        
        BindUIListener();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected final void BindUIListener() {
       //we cannot use the property change listener as it will not work with this panel.
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final void UnboundUIListener() {
        //not needed 
    }

    /**
     * not used
     */
    @Override
    public synchronized BufferedImage getUIValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void setTheUIValue(BufferedImage newValue) {
        getUIComponent().setImage(newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean allowNullUpdate() {
        return true;//null are AOK here. 
    }
}
