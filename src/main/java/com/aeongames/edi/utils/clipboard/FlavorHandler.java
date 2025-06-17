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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * a {@link FlavorProcessor} wrapper Class that holds the Flavor processor and
 * does a pre check if the Transferable is not null and that it supports the
 * Flavor we are ask to process. also simplifies calling the processor by
 * setting the {@code StopSignalProvider} ahead of time.
 *
 * @author Eduardo Vindas
 * @version 1.5
 */
public class FlavorHandler {

    //<editor-fold defaultstate="collapsed" desc="Properties">
    private static final String LOGGERNAME = "DataTransferLogger";
    /**
     * a set of {@link DataFlavor} (at the least one) of flavors that this
     * handler can unload into a {@link FlavorProcessor}
     */
    private final Set<DataFlavor> flavors;

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
    //</editor-fold>

    /**
     * Constructor for FlavorHandler.
     *
     * @param stopper an <strong>Optional</strong> instance of
     * {@code StopSignalProvider} used to determine if the execution or
     * processing of this method (or underline calls) should be halted and the
     * method should return as soon as possible. this value can be null if so,
     * we assume that the task will never be ask to halt.
     * @param processor the desire action to call when Handling the Desired
     * Flavor(s)
     * @param flavors the DataFlavor(s) to be handled by this instance.
     */
    public FlavorHandler(StopSignalProvider stopper, FlavorProcessor processor, DataFlavor... flavors) {
        flavors = Objects.requireNonNull(flavors, "Flavor cannot be null");
        if (flavors.length < 1 || flavors[0] == null) {
            throw new IllegalStateException("the first element cannot be null");
        }
        this.flavors = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(flavors)));
        stopProvider = Objects.requireNonNullElse(stopper, () -> false);//if is null assume there will be no stops
        this.processor = Objects.requireNonNull(processor, "processor cannot be null");
    }

    /**
     * check if the flavor is supported by this handler and if so, unload the
     * processor to handle the flavor.
     *
     * @param transferData the Transferable object to handle
     * @return true if the flavor was handled successfully, false otherwise
     * @throws DataTransferException if a error happens while Reading the data
     * from {@code transferData} due the Clipboard Was busy. or in used by other
     * process
     */
    public final boolean handleFlavor(Transferable transferData) throws DataTransferException {
        if (Objects.isNull(transferData)) {
            return false;
        }
        //check if the transferible. supports the flavor that our handle can process
        var FlavorTohandle = consumesAny(transferData.getTransferDataFlavors());
        if (Objects.isNull(FlavorTohandle)) {
            return false;
        }
        LoggingHelper.getLogger(LOGGERNAME).log(Level.INFO, "Compatible Handler Found, Calling: {0}", processor.getClass().getName());
        return processor.handleFlavor(FlavorTohandle, stopProvider, transferData);
    }

    /**
     * Checks if this Handler Can consume Any of the Provided
     *
     * @param otherFlavor the flavors to check against our internal registered
     * flavors.
     * @return the first instance of a flavor that this class can handle
     */
    public final DataFlavor consumesAny(DataFlavor... otherFlavor) {
        var list= List.of(otherFlavor);
        var encounter = flavors.stream().filter((t) -> list.contains(t)).findFirst();
        if(encounter.isPresent()){
            return encounter.get();
        }
        return null;
    }

    public final boolean canConsume(DataFlavor... otherFlavor) {
        return Objects.nonNull(consumesAny(otherFlavor));
    }

    /**
     * gathers and returns a reference to the DataFlavor associated with this
     * FlavorHandler.
     *
     * @return the DataFlavor associated with this FlavorHandler
     */
    public final Set<DataFlavor> getFlavor() {
        return flavors;
    }
}
