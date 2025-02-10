/*
 * Copyright 2008-2011,2024 Eduardo Vindas
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 /*
 *Created on Oct 9, 2010
 */
package com.aeongames.edi.utils.visual;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.util.Optional;
import javax.swing.ImageIcon;

/**
 *
 * @author Eduardo Vindas / cartman
 */
public class ImageUtils {

    /**
     * we want to show a image but we want to keep the ratio, so lets made a
     * image using this approach what it is done is we take the smallest
     * parameter as we want a image that is able to be display within the
     * parameter size we assume the image will be contained within a "box" also
     * we will calculate the position there we will place the image
     *
     * @param width the width of the image container or the context where will
     * be paced
     * @param height the height of the image container or the context where will
     * be paced
     * @param to_resize image that we require to calculate the the ratio for
     * @return a array of integers with the following values 0= width to set
     * 1=height to set 2=the width where the image required to be place 3=the
     * height within the image will be set.
     */
    public static int[] keep_ratio_for_size(int width, int height, Image to_resize) {
        double scale = determineImageScale(to_resize.getWidth(null), to_resize.getHeight(null), width, height);
        var WidthToUse = (int) (to_resize.getWidth(null) * scale);
        var HeightToUse = (int) (to_resize.getHeight(null) * scale);
        WidthToUse = (WidthToUse < 1) ? 1 : WidthToUse;
        HeightToUse = (HeightToUse < 1) ? 1 : HeightToUse;
        var PositionForW = width / 2 - WidthToUse / 2;
        var PositionForH = height / 2 - HeightToUse / 2;
        return new int[]{WidthToUse, HeightToUse, PositionForW, PositionForH};
    }

    /**
     * determine scale of the image returns the smallest of the scale values
     */
    private static double determineImageScale(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        double scalex = (double) targetWidth / sourceWidth;
        double scaley = (double) targetHeight / sourceHeight;
        return Math.min(scalex, scaley);
    }

    /**
     * converts a Image into a Buffered image. if the image is already a buffer
     * image returns the parameters (does not create a copy!)
     *
     * @param image the image to transform to buffered image
     * @return the same image if is instance of BufferedImage or a new
     * BufferedImage with the same content as the parameter
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage buffIma) {
            return buffIma;
        } else {
            // This code ensures that all the pixels in the image are loaded
            image = new ImageIcon(image).getImage();
            // Determine if has Alpha on image or has Alpha on pixels
            boolean hasAlpha = hasAlpha(image);
            BufferedImage bimage = null;
            int transparency
                    = (hasAlpha) ? Transparency.BITMASK : Transparency.OPAQUE;
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                // Create the buffered image
                GraphicsDevice gs = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gs.getDefaultConfiguration();
                bimage = gc.createCompatibleImage(
                        image.getWidth(null),
                        image.getHeight(null),
                        transparency);
            } catch (HeadlessException e) {
                // The system does not have a screen
            }

            if (bimage == null) {
                // Create a buffered image using the default color model
                var type = (hasAlpha) ? BufferedImage.TYPE_INT_ARGB
                        : BufferedImage.TYPE_INT_RGB;
                bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
            }
            // Copy image to buffered image
            Graphics g = bimage.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return bimage;
        }
    }

    /**
     * creates and returns a copy of the imaged resized to the desired
     * dimensions and using the provided Hint.(for resizing)
     *
     * @param source the source image to create a resized version.
     * @param width the desired width
     * @param height the desired height
     * @param OptionalHint A Optional value that refers to the hint for resize.
     * @return a resized Instance of the image.
     */
    public static ImageIcon ScaleImageIcon(ImageIcon source, int width, int height, Integer OptionalHint) {
        Image image = source.getImage();
        var opthint = Optional.ofNullable(OptionalHint);
        int hint;
        if (opthint.isPresent()) {
            hint = opthint.get();
        } else {
            hint = java.awt.Image.SCALE_SMOOTH;
        }
        Image newimg = image.getScaledInstance(width, height, hint); // scale it the smooth way  
        return new ImageIcon(newimg);
    }

    /**
     * creates and returns a copy of the imaged resized to the desired
     * dimensions and using the provided Hint.(for resizing)
     *
     * @param source the source image to create a resized version.
     * @param width the desired width
     * @param height the desired height
     * @return a resized Instance of the image.
     */
    public static ImageIcon ScaleImageIcon(ImageIcon source, int width, int height) {
        return ScaleImageIcon(source, width, height, null);
    }
    
    /**
     * creates and returns a copy of the imaged resized to the desired
     * dimensions and using the provided Hint.(for resizing)
     *
     * @param source the source image to create a resized version.
     * @param width the desired width
     * @param height the desired height
     * @param Hint A value that refers to the hint for resize.
     * @return a resized Instance of the image.
     */
    public static ImageIcon ScaleImageIcon(ImageIcon source, int width, int height, int Hint) {
        return ScaleImageIcon(source, width, height, Integer.valueOf(Hint));
    }

    /**
     * check if the provided image has or support Alpha Channel if the Image is
     * a Buffered Image it gathers the value from the Color Model
     *
     * otherwise we attempt to get the Color model from a Pixel via (pixel
     * grabber).
     *
     * @param image the image to check
     * @return
     */
    public static boolean hasAlpha(Image image) {
        if (image instanceof BufferedImage buff) {
            BufferedImage bimage = buff;
            return bimage.getColorModel().hasAlpha();
        }
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels(1000);
        } catch (InterruptedException e) {
        }
        ColorModel cm = pg.getColorModel();
        if (cm != null) {
            return cm.hasAlpha();
        }
        return false;
    }
}
