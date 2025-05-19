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
package com.aeongames.edi.utils.Pojo;

/**
 * this is a interface that define a set of Functions to expose a properties of
 * the {@code P indexed or sorted} of type {@code String}.
 *
 * @author Eduardo Vindas
 */
public final class StringPropertyPojo extends TriggerHappyListenableProperty<String>{

    private String Value;

    public StringPropertyPojo() {
        super();
    }

    @Override
    public synchronized boolean updateProperty(String newValue) {
        setValue(newValue);
        return true;
    }

    @Override
    public synchronized void setValue(String newValue) {
        Value = newValue;
        firePropertyChanged();
    }

    @Override
    public synchronized String getValue() {
        return Value;
    }
}
