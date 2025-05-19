/* 
 *  Copyright © 2012,2025 Eduardo Vindas Cordoba. All rights reserved.
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
package com.aeongames.edi.utils.ThreadUtils;

/**
 * simple await class example.
 * @author cartman
 */
public class SignalWaiter {

    private volatile boolean await = false;

    public SignalWaiter() {
    }

    public synchronized void waitForSignal() {
        while (await) {
            try {
                wait();
            } catch (InterruptedException e) {
//                        System.err.println("Exepcion");
            }
        }

    }

    public synchronized boolean StartWaitSignal() {
        if (!await) {
            await = true;
        }
        return await;
    }

    public synchronized void Signal() {
        await = false;
        notify();
    }
}
