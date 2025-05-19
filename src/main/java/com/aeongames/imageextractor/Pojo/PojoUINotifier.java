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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Java does not support JavaBeans binding native to Swing components. and JSR
 * 295 was withdraw in 2011 (14 years ago) java beans are widely used and
 * supported on JAVA for Enterprise projects (case in point Spring family of
 * frameworks) and for Hibernate. however on desktop applications that we desire
 * for it to be LIGHTWIGHT including this huge overhead like spring. is...
 * undesirable. also after research I found that there is no "free" or open
 * source Binder. that is well maintained most is abandon-ware or is pay walled.
 */
/**
 * A lightweight class to synchronize POJO properties with Swing components.
 * Provides two-way binding between POJO properties and UI components.
 *
 * @author Eduardo Vindas Cordoba
 */
public class PojoUINotifier {

    private final PropertyChangeSupport propertyChangeSupport;
    

    public PojoUINotifier() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Adds a PropertyChangeListener to listen for property changes.
     *
     * @param propertyName the name of the property to listen to
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a PropertyChangeListener.
     *
     * @param propertyName the name of the property
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Fires a property change event.
     *
     * @param propertyName the name of the property
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Binds a POJO property to a Swing component (e.g., JTextField).
     *
     * @param propertyName the name of the POJO property
     * @param pojo the POJO instance
     * @param component the Swing component
     */
    public void bind(String propertyName, Object pojo, JComponent component) {
        if (component instanceof JTextComponent textComponent) {
            // Listen for changes in the Swing component and update the POJO
            textComponent.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updatePojo();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updatePojo();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updatePojo();
                }

                private void updatePojo() {
                    try {
                        String newValue = textComponent.getText();
                        Object oldValue = pojo.getClass().getMethod("get" + trimAndCapitalize(propertyName)).invoke(pojo);
                        if (!newValue.equals(oldValue)) {
                            pojo.getClass().getMethod("set" + trimAndCapitalize(propertyName), String.class).invoke(pojo, newValue);
                            firePropertyChange(propertyName, oldValue, newValue);
                        }
                    } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Listen for changes in the POJO and update the Swing component
            addPropertyChangeListener(propertyName, evt -> {
                String newValue = (String) evt.getNewValue();
                if (!textComponent.getText().equals(newValue)) {
                    textComponent.setText(newValue);
                }
            });
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    protected void fastBind(String propertyName, Object pojo, JTextComponent textComponent) {
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePojo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePojo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePojo();
            }

            private void updatePojo() {
                try {
                    String newValue = textComponent.getText();
                    Object oldValue = pojo.getClass().getMethod("get" + trimAndCapitalize(propertyName)).invoke(pojo);
                    if (!newValue.equals(oldValue)) {
                        pojo.getClass().getMethod("set" + trimAndCapitalize(propertyName), String.class).invoke(pojo, newValue);
                        firePropertyChange(propertyName, oldValue, newValue);
                    }
                } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String trimAndCapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        str = str.strip();
        return str.substring(0, 1).toUpperCase().concat(str.substring(1));
    }
}
