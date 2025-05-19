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
 * This Interface defines a set of functions that a Property that can be listen
 * for changes on the property. 
 * @author Eduardo Vindas
 * @param <T> the Property Type Class that represent the value this property holds
 */
public interface ListenableProperty<T> {
    /**
     * updates the underline Object Property {@code Property} with the new value
     * {@code newProperty}
     *
     * @param newValue the new Value to set to the specified property.
     * @return true if able to change, false otherwise.
     */
    boolean updateProperty(T newValue);

    /**
     * updates the underline Object Property {@code Property} with the new value
     * {@code newProperty}
     * this function might or might not throw a run-time exception if it fails. 
     * @see #updateProperty(java.lang.Object, java.lang.Object)    
     * @param newValue the new Value to set to the specified property.
     */
    void setValue(T newValue);

    /**
     * gather the Object Property and returns its value. if not set might return 
     * null
     * @return the current value for the {@code Property}
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
