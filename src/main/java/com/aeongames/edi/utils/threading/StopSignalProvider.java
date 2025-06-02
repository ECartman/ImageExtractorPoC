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
 */
package com.aeongames.edi.utils.threading;

/**
 * a functional interface to check of the current execution should halt and the 
 * function that is checking should return as soon as possible. 
 * @author Eduardo Vindas
 */
@FunctionalInterface
public interface StopSignalProvider {
    /**
     * This method should be called to check if a stop signal has been received.
     * a function that uses this interface should check for the stop signal 
     * at any point in the code where it is safe to stop the process.
     * specially for any point before and after a blocking call.
     * @return true if a stop signal has been received, false otherwise.
     */
    public boolean isStopSignalReceived();

}
