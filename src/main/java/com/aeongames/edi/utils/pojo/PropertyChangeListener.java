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
 * 
 */
package com.aeongames.edi.utils.pojo;

/**
 * a functional Interface that will be called back when a object property changes  
 * this class differs from {@link java.beans.PropertyChangeListener} as we don't
 * want or care to serialize the events, nor we want to track or keep track of
 * "previous value". we might even remove the value as parameter as the property
 * reference holds it. but it might have been changed further. 
 * @author Eduardo Vindas
 * @param <S> the type of the source of the change
 * @param <T> the property type that this UpdatableProperty will handle
 */
@FunctionalInterface
public interface PropertyChangeListener<T,S extends ListenableProperty<T>> {
    /**
     * called by {@link ListenableProperty} when its property has changed. 
     * the {@link ListenableProperty} does NOT require to notify if the value
     * has been changed further, meaning that the provided {@code newValue} is 
     * immediately deprecated and the code on this function might prefer to 
     * call {@link ListenableProperty#getValue()} to get an updated value.
     * @param source the {@link ListenableProperty} that trigger this change
     * @param newValue the value to which was changed when this call is made.(immediately deprecated)
     */
    void propertyChanged(S source,T newValue);
}
