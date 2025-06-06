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
 * This Interface defines a set of functions that a Property that can be listen
 * for changes on the property. 
 * <Strong>the implementer MUST ensure this class is Syncronized</Strong>
 * and must ensure that the read writes are atomic, however the implementer is 
 * not bound to ensure that when sending notification the value is updated.
 * @author Eduardo Vindas
 * @param <T> the Property Type Class that represent the value this property holds
 */
public sealed interface ListenableProperty<T> 
        permits PropertyPojo,FastPropertyPojo{
    /**
     * attempts to update the underline {@code T} Property with the new value
     * and returns whenever or not Success to update the value.
     * the implementation might wait to update and update the value 
     * or just timeout and return false.
     *
     * @param newValue the new Value to set to the specified property.
     * @return true if able to change, false otherwise.
     */
    boolean tryUpdateProperty(T newValue);
    /**
     * updates the underline Object Property {@code T} with the new value
     * {@code T}
     * this function might or might not throw a run-time exception if it fails. 
     * @see #updateProperty(java.lang.Object, java.lang.Object)    
     * @param newValue the new Value to set to the specified property.
     */
    void setValue(T newValue);

    /**
     * gather the Object Property and returns its value. if not set might return 
     * null
     * @return the current value for the {@code T}
     */
    T getValue();
    
    /**
     * adds a listener to this object to process changes made on its property.
     * @param Listener the listener to register
     */
    void addPropertyListener(PropertyChangeListener<T,ListenableProperty<T>> Listener);
    
    /**
     * removes the Lister for this property
     * @param Listener 
     */
    void RemovePropertyListener(PropertyChangeListener<T,ListenableProperty<T>> Listener);
}
