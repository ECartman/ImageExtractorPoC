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
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeongames.imageextractor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author cartman
 */
public abstract class UIChangeNotifier {

    private final ArrayList<UIChangeListener> Listeners;

    protected UIChangeNotifier() {
        Listeners = new ArrayList<>();
    }

    public boolean RemoveListener(UIChangeListener toRemove) {
        return Listeners.remove(toRemove);
    }

    public boolean AddListener(UIChangeListener newListener) {
        if (Listeners.contains(newListener)) {
            return false;
        }
        return Listeners.add(newListener);
    }

    protected void ChangeUIEnabledStatus(boolean Enablement) {
        for (UIChangeListener Listener : Listeners) {
            Listener.RequestEnableUI(Enablement);
        }
    }

    protected void SendErrorNotification(Throwable Error) {
        for (UIChangeListener Listener : Listeners) {
            Listener.NotifyErrorEvent(Error);
        }
    }

    protected void SendItermediateChange(String Update) {
        for (UIChangeListener Listener : Listeners) {
            Listener.NotifyIntermediateChanges(Update);
        }
    }

    protected void SendItermediateChange(BufferedImage completedImage) {
        for (UIChangeListener Listener : Listeners) {
            Listener.NotifyResultImageProcess(completedImage);
        }
    }

}
