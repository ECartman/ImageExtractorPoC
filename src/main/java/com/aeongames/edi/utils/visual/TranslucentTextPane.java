/*
 *  Copyright © 2008-2013 Eduardo Vindas. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.visual;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JTextPane;

/**
 *
 * @author  Eduardo Vindas
 */
public class TranslucentTextPane extends JTextPane{
  
    public static final int DEFAULTARC = 0;
    public static final int DEFTRANSPARENCY = 200;
    private int arcWidth = DEFAULTARC, arcHeight = DEFAULTARC;
    private int trasparency = DEFTRANSPARENCY;

    public TranslucentTextPane() {
        super();
        super.setOpaque(false);
        setcolor(getBackground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color temp = g.getColor();
        var ppColor = new Color(temp.getRGB()|DEFTRANSPARENCY<<24, true);
        g.setColor(ppColor);
        g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arcWidth, arcHeight);
        g.setColor(temp);
        temp = null;
        super.paintComponent(g);
//        g.dispose();
    }

    public void setuniformarc(int arc) {
        if (arc > 0) {
            arcWidth = arcHeight = arc;
        }
    }

    public void setPanelTrasparency(int Trasparency) {
        if (Trasparency >= 0 && Trasparency <= 255) {
            trasparency = Trasparency;
        }
        repaint();
    }

    public int getpanelAlpha() {
        return trasparency;
    }

    /**
     * @param arcWidth the arcWidth to set
     */
    public void setArcWidth(int arcWidth) {
        if (arcWidth > 0) {
            this.arcWidth = arcWidth;
        }
    }

    /**
     * @param arcHeight the arcHeight to set
     */
    public void setArcHeight(int arcHeight) {
        if (arcHeight > 0) {
            this.arcHeight = arcHeight;
        }
    }

    /**
     * unlike the original implementation on this case if you set opaque or not
     * will result in either begin completely opaque or complete transparent on
     * the internal alpha level. so it will not call the parent implementation.
     * however the result will appear to be the same
     *
     * @param isOpaque
     */
    @Override
    public void setOpaque(boolean isOpaque) {
        if (isOpaque) {
            setPanelTrasparency(255);
        } else {
            setPanelTrasparency(0);
        }
    }

    public final void setcolor(Color col) {
        super.setBackground(col);
        this.repaint();
    }  
}
