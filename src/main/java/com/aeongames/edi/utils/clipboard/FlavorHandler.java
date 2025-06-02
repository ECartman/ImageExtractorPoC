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

import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Objects;

import com.aeongames.edi.utils.threading.StopSignalProvider;
import java.util.logging.Level;

/**
 *
 * @author Eduardo Vindas
 */
public class FlavorHandler implements FlavorProcessor {

    private static final String LOGGERNAME = "ClipBoardListenerLogger";
    /**
     * the an array of {@link DataFlavor} (at the least one) to handle Clipboard
     * events.
     */
    private final DataFlavor[] flavors;

    /**
     * a instance of {@link FlavorProcessor} that defines the functionality to
     * handle the {@link #flavors}
     */
    private final FlavorProcessor processor;

    /**
     * a instance of {@link StopSignalProvider} used to check if the function
     * execution of {@link #handleFlavor(java.awt.datatransfer.Transferable, java.awt.datatransfer.Clipboard)
     * }
     */
    private final StopSignalProvider stopProvider;

    /**
     * Constructor for FlavorHandler.
     *
     * @param stopper an instance of {@code StopSignalProvider} used to detected
     * if the execution or processing of this method (or underline calls) should
     * be halted and the method should return as soon as possible this value can
     * be null if so. it is assume that the task will never be ask to halt.
     * @param processor the desire action to call when Handling the Desired
     * Flavor(s)
     * @param flavors the DataFlavor(s) to be handled by this instance.
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
     * @param flavor the expected flavor to handle,can be null, and if null this
     * implementation will check for which one at {@link #flavors} can handle
     * it.
     * @param transferData the Transferable object to handle
     * @param stopProvider {@link StopSignalProvider} used to check if this
     * function should stop processing data and return. if this value is not
     * equals to {@link #stopProvider} this method will throw a
     * {@code IllegalArgumentException}
     * @param clipboard the Clipboard associated with the Transferable
     * @return true if the flavor was handled successfully, false otherwise (or
     * if unable or not supported)
     */
    @Override
    public boolean handleFlavor(DataFlavor flavor, StopSignalProvider stopProvider, Transferable transferData) throws ClipboardException {
        if (Objects.isNull(transferData)) {
            return false;
        }
        if (stopProvider != null && this.stopProvider != stopProvider) {
            throw new IllegalArgumentException("the StopProvided are different instances. this is not allowed");
        }
        //check if the transferible. supports the flavor that our handle can process
        DataFlavor FlavorTohandle = flavor;
        if (!transferData.isDataFlavorSupported(FlavorTohandle)) {
            for (DataFlavor DataFlav : flavors) {
                if (transferData.isDataFlavorSupported(DataFlav)) {
                    FlavorTohandle = DataFlav;
                    break;
                }
            }
        }
        if (FlavorTohandle == null) {
            return false;
        }
        return processor.handleFlavor(FlavorTohandle, this.stopProvider, transferData);
    }

    /**
     * check if the flavor is supported by this handler and if so, unload the
     * processor to handle the flavor.
     *
     * @param transferData the Transferable object to handle
     * @param clipboard the Clipboard associated with the Transferable
     * @return true if the flavor was handled successfully, false otherwise
     */
    public final boolean handleFlavor(Transferable transferData) throws ClipboardException {
        //we cannot process the data if the clipboard is null or the data is null
        if (Objects.isNull(transferData)) {
            return false;
        }
        //check if the transferible. supports the flavor that our handle can process
        DataFlavor FlavorTohandle = null;
        for (DataFlavor flavor : flavors) {
            if (transferData.isDataFlavorSupported(flavor)) {
                FlavorTohandle = flavor;
                break;
            }
        }
        if (FlavorTohandle == null) {
            return false;
        }
        LoggingHelper.getLogger(LOGGERNAME).log(Level.INFO,"Compatible Handler Found, Calling: {0}",processor.getClass().getName());
        return processor.handleFlavor(FlavorTohandle, stopProvider, transferData);
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
