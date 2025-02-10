/* 
 *  Copyright © 2024 Eduardo Vindas. All rights reserved.
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
package com.aeongames.edi.utils.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.JButton;

/**
 * a Simple "Close Button" that render a normal empty Jbutton
 * and adds a "X" that becomes the color setup (default RED) 
 * when the mouse rolls over it. 
 * @author Eduardo V
 */
public class SimpleCloseButton extends JButton {

    /**
     * the Colors to render the "X" when idle and when Rolled over.
     */
    private Color XColor = null, XRolloverColor = null;
    protected static final float GAP = 3.3f;
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (e.getComponent() instanceof AbstractButton button) {
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (e.getComponent() instanceof AbstractButton button) {
                button.setBorderPainted(false);
            }
        }
    };

    /**
     * creates a new instance of this SimpleCloseButton
     */
    public SimpleCloseButton() {
        XColor = javax.swing.UIManager.getColor("Button.foreground");
        XRolloverColor = Color.RED;
        setContentAreaFilled(false);
        setBorderPainted(false);
        addMouseListener(buttonMouseListener);
        setRolloverEnabled(true);
    }

    /**
     * Changes the color of the "X" when renders. 
     * if the provided value is null the color is set to the default UIManager color for 
     * "Button.foreground"
     * @param NewColor the new color to set. or Null to set the UI default. 
     */
    public void SetXColor(Color NewColor) {
        XColor = Objects.requireNonNullElse(NewColor, javax.swing.UIManager.getColor("Button.foreground"));
    }

    /**
     * Changes the color of the "X" when the mouse rollover it. 
     * if the provided value is null the color is set to the default UIManager color for 
     * "Button.foreground"
     * @param NewRollColor the new color to set. or Null to set the UI default. 
     */
    public void SetXRolloverColor(Color NewRollColor) {
        XRolloverColor = Objects.requireNonNullElse(NewRollColor, Color.RED);
    }

    //paint the "X" after the rest of the button is rendered.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            shift the image for pressed buttons (animate it slightly)
        if (getModel().isPressed()) {
            g2.setStroke(new BasicStroke(2));
            //g2.translate(1, 1);
        } else {
            g2.setStroke(new BasicStroke(3));
        }
        g2.setColor(XColor);
        if (getModel().isRollover()) {
            g2.setColor(XRolloverColor);
        }
        var init = 0f + GAP;
        var presize = getWidth() - GAP;
        g2.draw(new Line2D.Float(init, init, presize, presize));
        g2.draw(new Line2D.Float(presize, init, init, presize));
        g2.dispose();

    }
}
