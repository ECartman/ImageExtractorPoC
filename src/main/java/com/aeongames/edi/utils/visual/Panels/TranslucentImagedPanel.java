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
 */
package com.aeongames.edi.utils.visual.Panels;

import com.aeongames.edi.utils.visual.ImageScaleComponents;
import com.aeongames.edi.utils.visual.ImageUtils;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * a Panel that Renders a Image with a Background that support levels of
 * Transparency thus this panel can render a Image and being totally opaque or
 * Totally transparent or a level in between. this panel also support rendering
 * the panel as a "Round Rectangle"
 *
 * @author Eduardo Vindas
 */
public class TranslucentImagedPanel extends TranslucentPanel {

    /**
     * the policy to use to resize and or print the image the default is Scale
     * Small Only
     */
    private ImageScaleComponents ScalePolicy = ImageScaleComponents.SCALE_SMALL_ONLY;
    /**
     * the default image location we use on our Image panel when none is
     * provided.
     */
    static final String DEF_LOGO = ImagePanel.DEF_LOGO;
    /**
     * the image to be show or process.
     */
    private Image OriginalImage = null;
    /**
     * the image to be show or process.
     */
    private Image RenderImage;
    /**
     * the image transparency level.
     */
    private float translucent = 1.0f;
    /**
     * if the Texture paint enabled. this 2 settings allow us to know if the
     * texture should be repeated on X and or on Y axis
     */
    private boolean repeat_X = true, repeat_Y = true;
    /**
     * minimal transparency
     */
    private static final float MINTRASPT = 0f;

    /**
     * the TranslucentImagedPanel is a normal Swing panel that can be at
     * different levels of Transparency. AND can render a image on the
     * background instead of the silly and boring color will draw a image
     * whenever is required or wanted to be implemented.
     */
    public TranslucentImagedPanel() {
        readDefault();
        set();
    }

    /**
     * the TranslucentImagedPanel is a normal Swing panel that can be at
     * different levels of Transparency. AND can render a image on the
     * background instead of the silly and boring color will draw a image
     * whenever is required or wanted to be implemented.
     *
     * @param todisplay the image that is desired to render on top of the color
     * of the panel
     */
    public TranslucentImagedPanel(Image todisplay) {
        if (todisplay != null) {
            RenderImage = todisplay;
        } else {
            readDefault();
        }
        set();
    }

    /**
     * the TranslucentImagedPanel is a normal Swing panel that can be at
     * different levels of Transparency. AND can render a image on the
     * background instead of the silly and boring color will draw a image
     * whenever is required or wanted to be implemented.
     *
     * @param todisplay the image that is desired to render on top of the color
     * of the panel
     * @param alpha a value between {@link #MINTRASPT} and {@code 1.0f}
     */
    public TranslucentImagedPanel(Image todisplay, float alpha) {
        if (todisplay != null) {
            RenderImage = todisplay;
        } else {
            readDefault();
        }
        if (alpha >= MINTRASPT && alpha < 1.0f) {
            translucent = alpha;
            config();
        }
        set();
    }

    /**
     * sets the dimensions for this panel according to the image.
     */
    private void set() {
        java.awt.Dimension size = new java.awt.Dimension(RenderImage.getWidth(this), RenderImage.getHeight(this));
        setSize(size);
    }

    /**
     * configures the image to be printed as transparency on the panel if
     * required, otherwise just set the image.
     */
    private void config() {
        if (Objects.isNull(RenderImage)) {
            return;//nothing to config
        }
        if (translucent < MINTRASPT || translucent > 1.0f) {
            return;
        }
        if(translucent == 1.0f && OriginalImage !=null){
            RenderImage = OriginalImage;
            return;
        }
        BufferedImage BuffOrigin;
        if (RenderImage instanceof BufferedImage providedBuffIma) {
            BuffOrigin = providedBuffIma;
        } else {
            BuffOrigin = ImageUtils.toBufferedImage(RenderImage);
        }
        /*done with checks BuffOrigin COULD be null here but if so the code SHOULD puke that is OK here. */
        if (BuffOrigin.getType() != BufferedImage.TRANSLUCENT) {
            var TransparentAppliedBuffer = new BufferedImage(BuffOrigin.getWidth(), BuffOrigin.getHeight(), BufferedImage.TRANSLUCENT);
            Graphics2D g2 = TransparentAppliedBuffer.createGraphics();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, translucent));
            g2.drawImage(BuffOrigin, null, 0, 0);
            // let go of all system resources in this Graphics  
            g2.dispose();
            OriginalImage = RenderImage;
            RenderImage = TransparentAppliedBuffer;
        }
    }

    /**
     * read and sets the default image for the panel.
     */
    private void readDefault() {
        try {
            this.RenderImage = ImageIO.read(this.getClass().getResource(DEF_LOGO));
        } catch (IOException ex) {
            try {
                this.RenderImage = java.awt.Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(DEF_LOGO));
            } catch (Exception sub) {
                Logger.getLogger(TranslucentImagedPanel.class.getName()).log(Level.SEVERE, null, sub);
            }
        }
    }

    /**
     * sets the policy for resize the background image acceptable parameters
     * <br> {@link ImageScaleComponents#SCALE_ALWAYS}
     * <br> {@link ImageScaleComponents#SCALE_USE_ALL_SPACE}
     * <br> {@link ImageScaleComponents#SCALE_SMALL_ONLY}
     * <br> {@link ImageScaleComponents#NO_SCALABLE_TEXTURE}
     *
     * @param policy the policy to use.
     * @throws IllegalArgumentException if a invalid parameter is sent
     */
    public void setBackgroundPolicy(ImageScaleComponents policy) {
        Objects.requireNonNull(policy, "Invalid Policy");
        ScalePolicy = policy;
    }

    /**
     * this method changes the image of the panel.
     *
     * @param todisplay the image to display
     * @return either true or false determine whenever or not the change were
     * successful or not if the image is the same as before this will return
     * false.
     */
    public final boolean changeImage(Image todisplay) {
        boolean result = false;
        if (todisplay == null) {
            OriginalImage = null;
            readDefault();
        } else {
            if ((OriginalImage != null && OriginalImage != todisplay)
                    || RenderImage != todisplay) {
                RenderImage = todisplay;
                OriginalImage = null;
                config();
            }
            result = true;
        }
        repaint();
        return result;
    }

    public final void setImageTrasparency(float trasparency) {
        if (trasparency >= MINTRASPT && trasparency <= 1.0f) {
            translucent = trasparency;
            config();
            repaint();
        }
    }

    /**
     * returns the alpha Level of the Image that is printed on this panel.
     *
     * @return Alpha Level of the image Begin printed on this panel.
     */
    public final float getImageAlphaLevel() {
        return translucent;
    }

    /**
     * returns the default background for a image panel that is the Logo image.
     */
    protected final void returnToDefault() {
        readDefault();
        OriginalImage = null;
        translucent = 1.0f;
        repaint();
    }

    /**
     * returns the current policy
     *
     * @return current policy
     */
    public final ImageScaleComponents getBackgroundScalePolicy() {
        return ScalePolicy;
    }

    public final void setRepeatX(boolean repeat) {
        repeat_X = repeat;
    }

    public final void setRepeatY(boolean repeat) {
        repeat_Y = repeat;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //we should add code for set this as an option instad of always apply
        if (g instanceof Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
//                RenderingHints.VALUE_RENDER_QUALITY);
        }
        switch (ScalePolicy) {
            case SCALE_ALWAYS:
                paintRespectRatio(g);
                break;
            case SCALE_USE_ALL_SPACE:
                paintDefault(g);
                break;
            case NO_SCALABLE_TEXTURE:
                paintsTexture(g);
                break;
            default:/*do the same as SCALE_SMALL_ONLY by default*/
            case SCALE_SMALL_ONLY:
                //meh lets set paint to size as default if somthing is wrong...
                paintToSize(g);
                break;
        }
    }

    /**
     * paints the image from size 1x1 to the image actual size that of course we
     * will respect the image ratio so the image will be show as it should with
     * not forced size also will center the image
     */
    private void paintToSize(Graphics g) {
        if (RenderImage.getWidth(null) > -1 && RenderImage.getWidth(null) < getWidth() && RenderImage.getHeight(null) < getHeight()) {
            int Width = (getWidth() / 2) - RenderImage.getWidth(null) / 2;
            int Height = (getHeight() / 2) - RenderImage.getHeight(null) / 2;
            g.drawImage(RenderImage, Width, Height, RenderImage.getWidth(null), RenderImage.getHeight(null), this);
        } else {
            paintRespectRatio(g);
        }
    }

    /**
     * paints the image from size 1x1 to whatever is possible without error (yet
     * unknown to me) use with caution also if the image is expanded to much
     * might eventually look... stretch... it respect the image ratio BTW
     */
    private void paintRespectRatio(Graphics g) {
        //ok now we want to keep the image ratio so lets try the new aproach
        int[] size = ImageUtils.keep_ratio_for_size(getWidth(), getHeight(), RenderImage);
        g.drawImage(RenderImage, size[2], size[3], size[0], size[1], this);
    }

    /**
     * sets the image on the panel but stretch to the PANEL size so this will
     * not respect the ratio will fill the hold panel.
     */
    private void paintDefault(Graphics g) {
        g.drawImage(RenderImage, 0, 0, getWidth(), getHeight(), this);
    }

    /**
     * draws the image all over the Panel as we want a textured Panel.
     *
     * @param g
     */
    private void paintsTexture(Graphics g) {
        if (!(RenderImage instanceof BufferedImage)) {
            OriginalImage = ImageUtils.toBufferedImage(RenderImage);
            RenderImage = OriginalImage;
        }
        Paint tempaint = ((Graphics2D) g).getPaint();
        TexturePaint textpaint = new TexturePaint((BufferedImage) RenderImage, new Rectangle(0, 0, ((BufferedImage) RenderImage).getWidth(), ((BufferedImage) RenderImage).getHeight()));
        if (g instanceof Graphics2D g2d) {
            g2d.setPaint(textpaint);
        }
        int Xupto = getWidth();
        if (!repeat_X) {
            Xupto = ((BufferedImage) RenderImage).getWidth();
        }
        int yupto = getHeight();
        if (!repeat_Y) {
            yupto = ((BufferedImage) RenderImage).getHeight();
        }
        if (g instanceof Graphics2D g2d) {
            g2d.fillRect(0, 0, Xupto, yupto);
            g2d.setPaint(tempaint);
        }
    }

    /**
     * provides the image Dimension. the Dimensions are generated each time this
     * method is called.
     * @return returns the dimentions of the underline image.
     */
    public Dimension getImageSize() {
        if (RenderImage != null) {
            return new Dimension(RenderImage.getWidth(null) == -1 ? 0 : RenderImage.getWidth(null),
                    RenderImage.getHeight(null) == -1 ? 0 : RenderImage.getHeight(null));
        } else {
            return new Dimension();
        }
    }
}
