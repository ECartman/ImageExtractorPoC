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
package com.aeongames.edi.utils.visual.UIlink;

import java.util.Objects;
import javax.swing.JComponent;

/**
 *
 * @author Eduardo Vindas
 * @param <T> the property type that this interface will handle
 * @param <C> the Component to handle 
 */
interface SwingComponentBind<T,C extends JComponent> {


    /**
     * returns the Component Bound for this Object.
     * @return the Component Bounded;
     */
    C getLinkedComponent();

    /**
     * removes the binding between the Swing Component and the POjo
     */
    void Unbound();
    
    /**
     * gather the Object Property and returns its value. if not set might return 
     * null
     * @return the current value for the {@code Property}
     */
    T getUIValue(); 
    /**
     * this function is intended for testing. and the idea is to check 
     * if the UI has the same value for the property as the POJO
     * @param theValueThatShould the value that should match
     * @return 
     */
     default boolean isPropertyOutOfSync(T theValueThatShould){
        return Objects.equals(getUIValue(),theValueThatShould);
     }

}
