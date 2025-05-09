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

import com.aeongames.edi.utils.error.LoggingHelper;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * ClipBoardListener defines both Clipboard Thread or runnable class that will
 * process changes from the clipboard the clipboard changes will be listened by
 * the FlavorListener interface. and when detected it will set a trigger for the
 * thread to check and process thus note there might be a significant delay
 * between the time the clipboard changes and the time the thread is able to
 * process the change. NOTE: this class does NOT process the clipboard changes
 * in the EDT.
 *
 * @see FlavorListener
 * @since 1.2
 * @author Eduardo Vindas
 */
public class ClipBoardListener implements Runnable, FlavorListener {

    /**
     * this is the underline Thread that will be running this service. this
     * Thread can change. as we can stop and restart the service. doing so
     * implies we stop (or request to stop) the service(and thus the thread) and
     * spawn a new one as well as resetting and re attaching the listener to the
     * System Clipboard.
     */
    private transient Thread backerThread = null;
    /**
     * a indicator that tell us the Service state. note: the service state alone
     * does NOT imply the service or thread is completely down. for such review
     * please call {@link ClipBoardListener.}
     */
    private transient volatile boolean ServiceOnline = false;

    /**
     * this flag determines if the service should <strong>as it's best
     * effort</strong>
     * top processing data. this flag is to be check for each task that could
     * take long to process data. in order to ensure responsiveness. However we
     * not the implementer of delegated task should held accountable if it hang
     * the execution. but be aware is bad practice to do so.
     *
     * @see StopClipBoardProcessing()
     */
    private transient volatile boolean ProcessingTask = false;

    /**
     * if set request the service to process the Clipboard
     * <strong>regardless</strong>
     * if there is any request booked to be processed.
     */
    private transient volatile boolean forceProcess = false;

    /**
     * an ArrayBlockingQueue that represent the Pending work to be performed by
     * the service. work is en-queued. by the Clipboard Listener.
     */
    private transient ArrayBlockingQueue<Clipboard> RequestQueue;

    private List<FlavorHandler> FlavorsListPriority;
    /**
     * a reference to the system Clipboard
     */
    private transient static final Clipboard Systemclipboard;

    /**
     * init the System clipboard
     */
    static {
        Clipboard tmp = null;
        try {
            // we will use the system clipboard.
            tmp = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
        }
        Systemclipboard = tmp;
    }

    /**
     * Constructor for the ClipBoardListener. this will initialize the handlers
     * for the flavors we are interested in. but does not Register the listener
     * with the system clipboard yet this is done when the service is started.
     * refer to {@link StartClipBoardService}
     *
     * @throws HeadlessException if System clipboard is not available.
     */
    public ClipBoardListener() {
        if (Systemclipboard == null) {
            throw new HeadlessException("No Clipboard available");
        }
        // we likely will not need more than 2 elements in the queue. if need
        // be the queue will grow.
        RequestQueue = new ArrayBlockingQueue<>(2);
        FlavorsListPriority = new LinkedList<>();
    }

    /**
     * Add a Flavor processor to the list of handlers at the end of the list.
     * NOTE: the order of the handlers is important as it will be used to
     * provide priority to the handlers. the first handler in the list will be
     * the first one to be called. and so on.
     *
     * @param flavor  the Flavor that we want to handle using the provided
     *                FlavorProcessor
     * @param handler the FlavorProcessor to register with the provided data
     *                flavor
     * @throws IllegalStateException if the service is running or processing
     */
    public final void addFlavorHandler(DataFlavor flavor, FlavorProcessor handler) {
        addFlavorHandler(flavor, handler, false);
    }

    /**
     * Add a FlavorHandler to the list of handlers at the start of the
     * list.NOTE: the order of the handlers is important as it will be used to
     * provide priority to the handlers. the first handler in the list will be
     * the first one to be called. and so on.
     *
     * @param flavor  the flavor the FlavorProcessor will handle
     * @param handler the FlavorProcessor to add
     * @throws IllegalStateException if the service is running or processing
     */
    public final void addPriorityFlavorHandler(DataFlavor flavor, FlavorProcessor handler) {
        addFlavorHandler(flavor, handler, true);
    }

    /**
     * Add a FlavorHandler to the list of handlers at the end of the list if no
     * prioritized otherwise add it at the start. NOTE: the order of the
     * handlers is important as it will be used to provide priority to the
     * handlers. the first handler in the list will be the first one to be
     * called. and so on.
     *
     * @param handler
     * @param priority
     * @throws IllegalStateException if the service is running or processing
     */
    private void addFlavorHandler(DataFlavor flavor, FlavorProcessor handler, boolean priority) {
        if (isServiceOnline() || isProcessingTask()) {
            throw new IllegalStateException(
                    "Cannot add FlavorHandler while the service is running or processing data.");
        }
        Objects.requireNonNull(handler, "FlavorProcessor cannot be null");
        Objects.requireNonNull(flavor, "the Flavor cannot be null");
        // check if the handler is already in the list. if so we will not add it.
        var containsFlavor = false;
        for (FlavorHandler flavorHandler : FlavorsListPriority) {
            if (flavorHandler.getFlavor().equals(flavor)) {
                containsFlavor = true;
                break;
            }
        }
        if (containsFlavor) {
            return;
        }
        if (FlavorsListPriority instanceof LinkedList<FlavorHandler>) {
            var itemHandler = new FlavorHandler(flavor, () -> {
                return !isProcessingTask();
            }, handler);
            if (priority) {
                FlavorsListPriority.addFirst(itemHandler);
            } else {
                FlavorsListPriority.add(itemHandler);
            }
        }
    }

    /**
     * Start The Clipboard Service. this will start the thread that will await
     * for Clipboard changes.
     *
     * @return true if the service started false otherwise (the service was
     *         already running, there are no Clipboard Handlers or otherwise fail to
     *         init)
     */
    public synchronized boolean StartClipBoardService() {
        // if service alredy running. then we cannot "start it"
        if (ServiceOnline) {
            return false;
        }
        if (FlavorsListPriority.isEmpty()) {
            return false;
        }
        // if service is set to finish but still processing or hang. we cannot restart
        // the service
        if (Objects.nonNull(backerThread)) {
            var notFinish = backerThread.isAlive() || backerThread.getState() != Thread.State.TERMINATED;
            if (notFinish) {
                return false;
            }
        }
        resetValues();
        /**
         * flagging the service as online here. this way is save as then we set
         * the flag when the service has been "book to start" and thus allowing
         * for the service to be immediately(after) stop if need be. the
         * alternative imply a risk of awaiting for the new Thread to come
         * online and set up the flag to true. and might have the risk of not
         * being able to be cancelled until the thread start its execution.
         * furthermore might imply a Threading risk. as the state might swap
         * without check if at any point between this request and the actual
         * execution of the Runnable Run. set to false it will be ignored. ALSO
         * allow us to immediately start listening to changes in clipboard and
         * book them to trigger which is an advantage to catch events as soon as
         * possible.
         */
        ServiceOnline = true;
        backerThread = new Thread(this, "ClipBoardListenerThread");
        Systemclipboard.addFlavorListener(this);
        backerThread.setDaemon(true);
        // kick the thread to start. lazy bum.
        backerThread.start();
        return true;
    }

    /**
     * Resets the Service State Variables.
     */
    private synchronized void resetValues() {
        ProcessingTask = false;
        forceProcess = false;
        // this should be overkill.
        RequestQueue.clear();
    }

    /**
     * set the service to stop. if the service is currently running and
     * processing data from the clipboard. it will continue with that task until
     * it is finished. and then the task finish it will stop the loop ending the
     * service. this means that this method will request for a "graceful" stop.
     * if a forced stop is needed, please review {@link StopClipBoardProcessing}
     * method
     *
     * @return true if the request to stop the service was accepted. false if
     *         the service was not running.
     */
    public synchronized boolean StopClipBoardService() {
        if (!ServiceOnline) {
            return false;
        }
        ServiceOnline = false;
        // we will stop the thread that will process the clipboard changes.
        Systemclipboard.removeFlavorListener(this);
        RequestQueue.clear();
        this.notifyAll();
        return true;
    }

    /**
     * This method request the service to <strong>Stop the Current Processing
     * </strong> from the Clipboard Data.<br>
     * <strong>if and only if the service is currently processing data.</strong>
     * <br>
     * <strong>IMPORTANT NOTES:</strong>
     * <ul>
     * <li>
     * This method <strong>does not stop the service</strong> it request to Stop
     * the <strong>current Processing (if any)</strong>
     * </li>
     * <li>
     * This method will <strong>Not</strong> stop the processing data
     * immediately as there might be long running step (for example Filling a
     * buffer from the system Clipboard) or Reading some other resource thus the
     * caller need to be aware of possible delays.
     * </li>
     * <li>
     * Once the Current Processing is stopped, if there are more work queued. it
     * will proceed to handle the next request. thus if you want the service to
     * stop Listening for changes. you need to top the service. using
     * {@link StopClipBoardService}
     * </li>
     * <li>
     * If the service by the time this function is called is NOT processing
     * anything yet. this function will return false and no action will be
     * performed, the service might immediately after call to process data. as
     * those calls are in parallel.
     * </li>
     * </ul>
     *
     * @return true if the request was successfully submitted. false if the
     *         service was not running. or not processing clipboard data.
     */
    public synchronized boolean StopClipBoardProcessing() {
        if (!ProcessingTask) {
            return false;
        }
        ProcessingTask = false;
        return true;
    }

    /**
     * Check if the Thread backing this Service is running. the service might be
     * set to stop. but the thread might still be running. to check if the
     * service has requested to stop please check the {@link isServiceOnline}
     *
     * @apiNote the result from this function is immediately deprecated as the
     *          service state might change while it is executed.
     * @return true if the service Thread is running. false otherwise.
     * @see ServiceOnline
     */
    public boolean isServiceThreadRunning() {
        if (ServiceOnline && backerThread != null && backerThread.isAlive()) {
            return ServiceOnline;
        }
        if (backerThread != null) {
            return backerThread.isAlive();
        }
        return false;
    }

    /**
     * Check the Service State flag, this returns true if the service is to be
     * running. However does not guarantee that the backing thread is running.
     * this means that the method might return false however the thread is still
     * running. or if the Thread Crashed for any reason. it is possible that the
     * function returns true. but the thread is not running.
     *
     * @apiNote the result from this function is immediately deprecated as the
     *          service state might change while it is executed.
     * @return true if the ServiceOnline flag is set to true. false otherwise.
     * @see isServiceThreadRunning
     */
    public boolean isServiceOnline() {
        return ServiceOnline;
    }

    /**
     * Check processing Flag and thus check if the last state of the thread was
     * reading/processing data out of the clipboard.
     * <Strong>Note:</strong> this does not mean that the service is running.
     * nor that the service is Online. for such check please refer to
     * {@link isServiceThreadRunning} or {@link isServiceOnline}
     *
     * @apiNote the result from this function is immediately deprecated as the
     *          service state might change while it is executed.
     * @return true if the service state is processing data from the clipboard.
     *         false otherwise.
     */
    public boolean isProcessingTask() {
        return ProcessingTask;
    }

    /**
     * Thread/Runnable method that will process the clipboard changes. this
     * function will loop until the service is stopped. meaning that
     * {@link StopClipBoardService} is called.
     */
    @Override
    public void run() {
        while (ServiceOnline) {
            synchronized (this) {
                // check if we have any clipboard changes to process. other
                if (RequestQueue.isEmpty() && !forceProcess) {
                    try {
                        // wait for a signal to process the clipboard changes.
                        this.wait();
                    } catch (InterruptedException e) {
                        LoggingHelper.getLogger(ClipBoardListener.class.getName()).log(Level.WARNING,
                                "Thread Interrupted", e);
                    }
                    // conclude this iteration. and loop again.
                    continue;
                }
                // we will process the clipboard changes.
                forceProcess = false;// disengage the force process flag.
                ProcessingTask = true;
            }
            var clipboard = RequestQueue.poll();
            if (clipboard == null) {
                clipboard = Systemclipboard;
            }
            // de-queue any other task that might be booked for the same Clipboard
            // check if empty first as it is faster than peeking, due locking
            while (!RequestQueue.isEmpty() && clipboard == RequestQueue.peek()) {
                RequestQueue.poll();
            }
            processClipboardChange(clipboard);
            synchronized (this) {
                ProcessingTask = false;
            }
        }
    }

    /**
     * request to process a change on the Clipboard. that was booked into this
     * service. for the specified Clipboard resource.
     *
     * @param clipboard the clipboard resource to pull the change from
     */
    private void processClipboardChange(Clipboard clipboard) {
        boolean errorstate;
        int pendingRetry = 3;
        do {
            errorstate = false;
            try {
                Transferable contents = clipboard.getContents(this);
                // if a interrupt to the process has been requested bail out.
                // also if the content of the Clipboard is null we will bail out.
                if (Objects.isNull(contents) || !ProcessingTask) {
                    return;
                }
                // check if the clipboard has any of the flavors we are interested in.
                DebugLog(contents);
                for (FlavorHandler handler : FlavorsListPriority) {
                    if (!ProcessingTask) {
                        return;
                    }
                    var result = handler.handleFlavor(contents, clipboard);
                    // we have processed the flavor. we can bail out.
                    if (result) {
                        break;
                    }
                }
            } catch (IllegalStateException ise) {
                LoggingHelper.getLogger(ClipBoardListener.class.getName()).log(Level.SEVERE,
                        "Clipboard State Error. Delaying the Procesesing", ise);
                try {
                    Thread.sleep(170);
                } catch (InterruptedException ex) {
                }
                // only retry if we have not exausted the retry count.
                // (and yes we decrement post usage)
                if (pendingRetry-- > 0) {
                    errorstate = true;
                }
            } catch (Exception ex) {
                LoggingHelper.getLogger(ClipBoardListener.class.getName()).log(Level.SEVERE, "Error reading clipboard",
                        ex);
            }
        } while (errorstate == true);
    }

    private void DebugLog(Transferable contents) {
        if (!LoggingHelper.RunningInDebugMode()) {
            return;
        }
        for (DataFlavor flavor : contents.getTransferDataFlavors()) {
            if (flavor == null) {
                continue;
            }
            LoggingHelper.getLogger(ClipBoardListener.class.getName()).log(Level.INFO, "Flavor: {0}",
                    flavor.getHumanPresentableName());
            LoggingHelper.getLogger(ClipBoardListener.class.getName()).log(Level.INFO, "FlavorHandling Class: {0}",
                    flavor.getDefaultRepresentationClassAsString());
            LoggingHelper.getLogger(ClipBoardListener.class.getName()).log(Level.INFO, "FlavorMimeType: {0}",
                    flavor.getMimeType());
        }
    }

    @Override
    public void flavorsChanged(FlavorEvent e) {
        /**
         * TODO: add some Notification Methodology to notify whomever might be
         * interested ChangeUIEnabledStatus(false); SendItermediateChange("a
         * Change on Clipboard was Detected.");
         */
        // check if we are running in EDT, in Java
        // as long as we use the Standar Java this would be called from the EDT
        if (SwingUtilities.isEventDispatchThread()) {
            synchronized (ClipBoardListener.this) {
                if (ServiceOnline) {
                    if (e.getSource() instanceof Clipboard clipboard) {
                        // we will add the clipboard to the request queue.
                        RequestQueue.add(clipboard);
                    } else {
                        // we will add the system clipboard to the request queue.
                        RequestQueue.add(Systemclipboard);
                    }
                    if (!ProcessingTask) {
                        ClipBoardListener.this.notify();// notify the thread to process the clipboard changes.
                    }
                }
            }
        } else {
            throw new NoSuchMethodError("this event should be triggered by the EDT otherwise smells as fabricated.");
        }

    }

}
