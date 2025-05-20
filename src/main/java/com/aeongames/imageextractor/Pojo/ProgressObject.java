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
package com.aeongames.imageextractor.Pojo;

import com.aeongames.edi.utils.Pojo.BooleanPropertyPojo;
import com.aeongames.edi.utils.Pojo.IntegerPropertyPojo;
import com.aeongames.edi.utils.Pojo.PropertyPojo;
import java.awt.image.BufferedImage;

/**
 * TODO: Encapsulate.
 *
 * @author Eduardo Vindas
 */
public class ProgressObject {

    public final PropertyPojo<String> CurrentStatus = PropertyPojo.newStringPojo();
    public final PropertyPojo<String> ImageTypeString = PropertyPojo.newStringPojo();
    public final PropertyPojo<BufferedImage> ImageProperty = new PropertyPojo<>();
    public final PropertyPojo<String> SavingFilePath = PropertyPojo.newStringPojo();
    public final PropertyPojo<String> LastFileCheckSum = PropertyPojo.newStringPojo();
    public final IntegerPropertyPojo CurrentFileNumber = new IntegerPropertyPojo();
    public final BooleanPropertyPojo CurrentUIEnablement = new BooleanPropertyPojo();

    public ProgressObject() {
    }

}
