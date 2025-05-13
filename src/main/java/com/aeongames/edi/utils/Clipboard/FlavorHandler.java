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

    private final DataFlavor[] flavors;
    private final FlavorProcessor processor;
    private final StopSignalProvider stopProvider;

    /**
     * Constructor for FlavorHandler.
     *
     * @param flavor the DataFlavor to be handled
     */
    public FlavorHandler(StopSignalProvider stopper, FlavorProcessor processor, DataFlavor... flavors) {
        Objects.requireNonNull(flavors, "Flavor cannot be null");
        if (flavors.length < 1 || flavors[0] == null) {
            throw new IllegalStateException("the first element cannot be null");
        }
        stopProvider = Objects.requireNonNullElse(stopper, () -> false);//if is null assume there will be no stops
        Objects.requireNonNull(processor, "processor cannot be null");
        this.flavors = flavors;
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
        DataFlavor FlavorTohandle=null;
        for (DataFlavor flavor : flavors) {
            if (transferData.isDataFlavorSupported(flavor)) {
                FlavorTohandle = flavor;
                break;
            }
        }
        if (FlavorTohandle == null) {
            return false;
        }
        return processor.handleFlavor(FlavorTohandle, stopProvider, transferData, clipboard);
    }

    /**
     * gathers and returns a reference to the DataFlavor associated with this
     * FlavorHandler.
     *
     * @return the DataFlavor associated with this FlavorHandler
     */
    public final DataFlavor[] getFlavor() {
        return flavors;
    }
}
