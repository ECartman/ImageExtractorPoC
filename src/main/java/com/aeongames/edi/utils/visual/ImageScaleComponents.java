/* 
 *  Copyright © 2024 Eduardo Vindas Cordoba. All rights reserved.
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

/**
 *
 * @author cartman
 */
public enum ImageScaleComponents {
        /**
         * variable to determine a policy where allow the image from a
         * ImagePanel to scale the image to a smaller size only used for when
         * you want to show a image up to its original size
         */
        SCALE_SMALL_ONLY,
        /**
         * variable to determine a policy where allow the image from a
         * ImagePanel to scale the image to the size required to show on the
         * panel but keep the aspect ratio of the image, also will be center
         */
        SCALE_ALWAYS,
        /**
         * will scale the image to use ALL the space of the panel will not try
         * to keep the ratio will not keep the aspect will fill the hold panel
         * space. this thought is not a good idea.
         */
        SCALE_USE_ALL_SPACE,
        /**
         * will Not Scale the image, but will instead use the image as A texture
         * to be used to Paint the background of the Panel. (repeated as much as
         * needed)
         */
        NO_SCALABLE_TEXTURE
    };
