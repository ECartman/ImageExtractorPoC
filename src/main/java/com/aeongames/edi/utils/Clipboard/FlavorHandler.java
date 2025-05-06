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
package com.aeongames.edi.utils.Clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Objects;

import com.aeongames.edi.utils.ThreadUtils.StopSignalProvider;

public class FlavorHandler {

    private final DataFlavor flavor;
    private final FlavorProcessor processor;
    private final StopSignalProvider stopProvider;

    /**
     * Constructor for FlavorHandler.
     *
     * @param flavor the DataFlavor to be handled
     */
    public FlavorHandler(DataFlavor flavor, StopSignalProvider stopper, FlavorProcessor processor) {
        Objects.requireNonNull(flavor, "Flavor cannot be null");
        stopProvider = Objects.requireNonNullElse(stopper, () -> false);
        Objects.requireNonNull(processor, "processor cannot be null");
        this.flavor = flavor;
        this.processor = processor;
    }

    /**
     * check if the flavor is supported by this handler and if so, unload the
     * processor to handle the flavor.
     *
     * @param transferData the Transferable object to handle
     * @param clipboard the Clipboard associated with the Transferable
     * @return true if the flavor was handled successfully, false otherwise
     */
    public final boolean handleFlavor(Transferable transferData, Clipboard clipboard) {
        //we cannot process the data if the clipboard is null or the data is null
        if (Objects.isNull(transferData) || Objects.isNull(clipboard)) {
            return false;
        }
        //check if the transferible. supports the flavor that our handle can process
        if (!transferData.isDataFlavorSupported(flavor)) {
            return false;
        }
        return processor.handleFlavor(flavor, stopProvider, transferData, clipboard);
    }

    /**
     * gathers and returns a reference to the DataFlavor associated with this
     * FlavorHandler.
     *
     * @return the DataFlavor associated with this FlavorHandler
     */
    public final DataFlavor getFlavor() {
        return flavor;
    }
}
