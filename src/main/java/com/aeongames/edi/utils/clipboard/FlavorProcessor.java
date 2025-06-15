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
package com.aeongames.edi.utils.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import com.aeongames.edi.utils.threading.StopSignalProvider;
import java.util.EventListener;

/**
 * A functional Interface that extends EventListener. as the implementation is
 * intended to listen to be called to "Process" the information from a clipboard
 * for the flavor provided.
 *
 * @author Eduardo Vindas C
 */
@FunctionalInterface
public interface FlavorProcessor extends EventListener {

    /**
     * note if this function is called by {@link FlavorHandler} the flavor check
     * is already performed.also assume that
     * {@code transferData}.isDataFlavorSupported({@code flavor}) will be
     * true.this function should make the assumption that non of the parameters
     * are null the implementation should focus on the following task:
     * Processing the flavor and the Transfer data.periodically (before and
 after) check if stopProvider has requested the function to stop
 processing data. and return a boolean indicating whenever or not it
 successfully handled the clipboard content.
     *
     * @param flavor the expected flavor to handle by this method
     * @param transferData the Transferable object to handle
     * @param stopProvider a functional interface that should be used to check
     * if this function should stop processing data and return.
     * @return true if the flavor was handled successfully, false otherwise (or
     * if unable or not supported)
     * @throws DataTransferException if a error happens while Reading the data from
     * {@code transferData} due the Clipboard Was busy. or in used by other
     * process
     */
    public boolean handleFlavor(DataFlavor flavor, StopSignalProvider stopProvider, Transferable transferData) throws DataTransferException;
}
