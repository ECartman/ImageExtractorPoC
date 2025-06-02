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
package com.aeongames.edi.utils.pojo;

/**
 * a extension of PropertyPojo that defines 2 functions for primitive value.
 * {@link #super}
 *
 * @author Eduardo
 */
public final class IntegerPropertyPojo extends PropertyPojo<Integer> {

    /**
     * creates a new Instance of a Property POJO.and sets the Value property to
     * 0
     */
    public IntegerPropertyPojo() {
        super(0);
    }

    /**
     * gather the integer primitive Property and returns its value. if not set
     * this will throw null pointer Exception.
     *
     * @return the current value for the {@link #getValue()}
     * @throws NullPointerException
     */
    public synchronized int getValuePrimive() {
        var result = getValue();
        if (result == null) {
            throw new NullPointerException("the Property returned null value");
        }
        return result;
    }

    /**
     * gather the integer primitive Property and returns its value.if not set
     * this will throw null pointer Exception.
     *
     * @param nullRemplacementValue the value to return if the property is null
     * @return the current value for the {@link #getValue()}
     * @throws NullPointerException
     */
    public synchronized int getValuePrimive(int nullRemplacementValue) {
        var result = getValue();
        return result == null ? nullRemplacementValue : result;
    }

    public synchronized final void plusplus() {
        var next = getValuePrimive();
        setValue(++next);
    }

}
