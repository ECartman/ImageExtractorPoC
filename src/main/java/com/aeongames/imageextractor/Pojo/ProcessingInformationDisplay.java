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
import com.aeongames.edi.utils.Pojo.StringPropertyPojo;

/**
 * TODO: Encapsulate.
 * @author Eduardo Vindas
 */
public class ProcessingInformationDisplay {    
   public StringPropertyPojo CurrentStatus = new StringPropertyPojo();
   public StringPropertyPojo SavingFilePath = new StringPropertyPojo();
   public IntegerPropertyPojo CurrentFileNumber = new IntegerPropertyPojo();
   public BooleanPropertyPojo CurrentUIEnablement = new BooleanPropertyPojo();
        
    
    public ProcessingInformationDisplay(){
    }
    
}
