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
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 *
 * Preface: java AWT implementation to read the Clipboard is bad because it
 * hides a lot of nuance and computation that might happens. and does not
 * disclose nuances.
 *
 * when pulling data from the Clipboard the best approach for long chunks of
 * data (and do note that the clipboard has no limit on the amount of data it
 * could hold is best to use a stream. as a Stream remove some overhead from
 * other ways. and provide a finer Control unfortunately it still imply some
 * overhead. due char Encoding. according to the JDK code at
 * <code>DataTransfer.translateBytes</code> Target data is an InputStream. For
 * arbitrary flavors, just return // the raw bytes. For text flavors, decode to
 * strip terminators and // search-and-replace EOLN, then <Strong>re
 * encode</Strong> according to the requested // flavor. example:: at
 * java.nio.HeapCharBuffer.ix(HeapCharBuffer.java:162) at
 * java.nio.HeapCharBuffer.put(HeapCharBuffer.java:216) at
 * sun.nio.cs.UnicodeDecoder.decodeLoop(UnicodeDecoder.java:116) at
 * java.nio.charset.CharsetDecoder.decode(CharsetDecoder.java:586) at
 * sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:385) at
 * sun.nio.cs.StreamDecoder.lockedRead(StreamDecoder.java:217) at
 * sun.nio.cs.<Strong>StreamDecoder</Strong>.read(StreamDecoder.java:171) at
 * java.io.InputStreamReader.read(InputStreamReader.java:188) at
 * java.io.BufferedReader.fill(BufferedReader.java:160) at
 * java.io.BufferedReader.implRead(BufferedReader.java:196) at
 * java.io.BufferedReader.read(BufferedReader.java:181) at
 * sun.awt.datatransfer.DataTransferer$ReencodingInputStream.readChar(DataTransferer.java:1523)
 * at
 * sun.awt.datatransfer.DataTransferer$<Strong>ReencodingInputStream</Strong>.read(DataTransferer.java:1548)
 * at java.io.InputStream.read(InputStream.java:296) at
 * java.io.InputStream.readNBytes(InputStream.java:412) at
 * java.io.InputStream.readAllBytes(InputStream.java:349) at [your code] at
 * java.lang.Thread.runWith(Thread.java:1596) at
 * java.lang.Thread.run(Thread.java:1583)
 *
 * the Advantage of this approach is that you can process as much data as you
 * need to and stop as you deemed it has reached a point that is too much.
 * rather that awaiting on a stuck processing. for example using another
 * approach would be:
 *
 *
 * that most of my Rant is on regards of the general abstract code and the
 * specific Windows implementation.
 *
 * 1) the Clipboard pre-gathers the clipboard data upon request so for example
 * from the moment you get the "Transferable object" it PREFETCHS the
 * information as far as i have seen it might pre fetch partial but seems to do
 * the totality of the data when playing with plain text. on
 * sun.awt.datatransfer.ClipboardTransferable class, now this would not be a big
 * deal if this were properly documented but it is not, on the abstraction and
 * the specification is hidden on the "SUN" dreaded package. furthermore the
 * documentation on this class source is not the best.
 *
 * This hybrid pre-fetch/delayed-rendering approach allows us to circumvent the
 * API restriction(WHICH API????) that client code cannot lock the Clipboard to
 * discover its formats before requesting data in a particular format, while
 * avoiding the overhead of fully rendering all data ahead of time. (this does
 * not seem to be true)
 *
 * now up to this point this is passable as what it is doing is gathering the
 * SYSTEM(OS) (assuming the clipboard is the os /system one) clipboard now the
 * pre fetching is all good stuff. it MIGHT consume a good chunk of memory. but
 * might be safer as the content might change if not fetched? then if you call
 * the transferable getTransferData([whatever]);
 *
 * here is where problems might arise. because when the data is text the JDK sun
 * code will NO MATTER THE FLAVOR take the data, and parse it into a String and
 * then provide the bytes. is is extremely wasteful and potentially slow. for
 * one. if I setup a flavor to handle the data as bytes why convert them into
 * string? if I desire to see the raw data this process will hinder that as it
 * does conversion via charset. this imagine this: the text is UTF-8 java will
 * parse to UTF-16, then parse back to UTF-8 and furthermore as a string.
 * meaning it will waste space on the string pool for that COULD be possibly
 * CONFIDENTIAL and require secure handling
 *
 * now generally speaking. besides the SECURITY problem the other issue is that
 * i am facing working on this code can hold A LOT of data. and that causes
 * performance and memory consumptions problems Raymond Chen himself. states
 * "No, there is no pre-set maximum size for clipboard data. You are limited
 * only by available memory and address space"
 *
 * https://devblogs.microsoft.com/oldnewthing/20220608-00/?p=106727
 *
 * so when java pulls the data initially it hast its raw bytes. but upon asking
 * for something workable. it try's to translate them... is extremely annoying.
 *
 * windows nuance:
 * https://learn.microsoft.com/en-us/windows/win32/dataxchg/clipboard
 * https://learn.microsoft.com/en-us/windows/win32/dataxchg/using-the-clipboard
 *
 */
/**
 * ClipboardService defines a service or background (thread/runnable) that will
 * await and process changes from the system clipboard.<br> 
 * The clipboard changes will be listened by the service own interface (itself)
 * ({@link FlavorListener}). and when detected it will set a trigger for the service
 * thread to check and process, thus note there might be a delay between the time
 * the clipboard changes and the time the thread is able to process the change. 
 * <br>
 * NOTE: this class does NOT process the clipboard changes in the EDT,
 * this is intentional as per design, as working with the EDT would cause
 * irresponsive UI, or delays
 * <br>
 * this service by itself does not handle the data. the interested need to register
 * a {@link FlavorProcessor} and register using 
 * {@link ClipboardService#addFlavorHandler(com.aeongames.edi.utils.Clipboard.FlavorProcessor, java.awt.datatransfer.DataFlavor...) }
 * or
 * {@link ClipboardService#addPriorityFlavorHandler(com.aeongames.edi.utils.Clipboard.FlavorProcessor, java.awt.datatransfer.DataFlavor...) }
 * once register the service will call this interface to handle the clipboard data.
 * <br>
 * NOTE: a Clipboard Event will be consider handled when the first possible Handler
 * reports that it has successfully handled the event. and no other listener will
 * be called, it allow to register multiple handlers. (sort of a hybrid Unicast event)
 * @see FlavorListener
 * @see FlavorProcessor
 * @since 1.2
 * @author Eduardo Vindas
 */
public final class ClipboardService implements Runnable, FlavorListener, ClipboardOwner {

    //<editor-fold defaultstate="collapsed" desc="Statics">
    /**
     * Internal Name for this Class Logger.
     */
    private static final String LOGGERNAME = "ClipBoardListenerLogger";

    /**
     * a reference to the system Clipboard
     */
    private static final Clipboard SYSTEM_CLIPBOARD;

    /**
     * initialization of the SystemClipboard
     */
    static {
        Clipboard tmp = null;
        try {
            tmp = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
            //there is no UI or our enviroment is Really contrained.
        }
        SYSTEM_CLIPBOARD = tmp;
    }

    private static ClipboardService Singleton_Instance = null;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Globals">
    /**
     * this is the underline Thread that will be running this service. this
     * Thread can change. as we can stop and restart the service. doing so
     * implies we stop (or request to stop) the service(and thus the thread) and
     * spawn a new one as well as resetting and re attaching the listener to the
     * System Clipboard.
     */
    private Thread backerThread = null;
    /**
     * a indicator that tell us the Service state. note: the service state alone
     * does NOT imply the service or thread is completely down. for such review
     * please call {@link ClipBoardListener.}
     */
    private volatile boolean serviceOnline = false;

    /**
     * this flag defines if the thread should continue processing the Clipboard
     * data if set to false it will try <strong>as best effort</strong> to stop
     * processing data. this flag is to be check for each task that could take
     * long to process data, for example if reading a stream it should
     * periodically check and it set to false. exit the loop reading the stream.
     * in order to ensure responsiveness. However we nor the implementer of
     * delegated task should held accountable if it hang the execution. but be
     * aware is bad practice to do so.
     *
     * @see StopClipBoardProcessing()
     */
    private volatile boolean processingData = false;

    /**
     * if set request the service to process the Clipboard
     * <strong>regardless</strong>
     * if there is any request booked to be processed.
     */
    private volatile boolean forceProcess = false; //TODO: remove or implement functionality to trigger a request for processing on demand

    /**
     * an ArrayBlockingQueue that represent the Pending work to be performed by
     * the service. work is en-queued. by the Clipboard Listener.
     */
    private ArrayBlockingQueue<Clipboard> RequestQueue;

    /**
     * a list of unique values. that contains the Flavors handlers that are
     * instances that can handle specific flavor(s) of data. from the clipboard.
     * <br>
     * when a change on the Clipboard is to be handled the list (in order) and
     * check for each handler if they are fit to handle the flavor.
     * <br>
     * this process runs in "first come first serve". meaning. that if there are
     * multiple handlers for the same flavor the one that has registered with
     * hight priority(first encountered on the list) and is allocated first will
     * process the data.
     */
    private final LinkedHashSet<FlavorHandler> FlavorsListPriority;
    /**
     * a mapping for the {@code FlavorHandler} that wrap a
     * {@code FlavorProcessor} this is required for ease of adding or removing
     * handlers
     */
    private final HashMap<FlavorProcessor, FlavorHandler> MapProcessors;

    /**
     * an atomic bool that indicates if this instance is the owner of the
     * clipboard.
     */
    private final AtomicBoolean Owner = new AtomicBoolean(false);

    /**
     * a Boolean that determine if we should ignore a Flavor change this is so
     * we ignore when we retake ownership of the clipboard. as this causes a
     * trigger on the listener.
     */
    private final AtomicBoolean SkipNext = new AtomicBoolean(false);

    /**
     * a listener for Shutdown. we register it to be able at a best effort to
     * gracefully. shutdown this service.
     */
    private final ShutdownListener Myshutdownlistener;
    //</editor-fold>

    /**
     * get and returns the ClipboardService for this execution. it is lazy
     * created. and please note the instance might not be setup or executing.
     * the caller is responsible of check if the service is setup and running or
     * otherwise for setup and starting the service.
     *
     * @return a instance of {@code ClipboardService}
     */
    public static final ClipboardService getClipboardService() {
        if (Objects.isNull(Singleton_Instance)) {
            Singleton_Instance = new ClipboardService();
        }
        return Singleton_Instance;
    }

    /**
     * Constructor for the ClipBoardListener. this will initialize the handlers
     * for the flavors we are interested in. but does not Register the listener
     * with the system clipboard yet this is done when the service is started.
     * refer to {@link StartClipBoardService}
     *
     * @throws HeadlessException if System clipboard is not available.
     */
    private ClipboardService() {
        if (SYSTEM_CLIPBOARD == null) {
            throw new HeadlessException("No Clipboard available");
        }
        // we likely will not need more than 5 elements in the queue. if need
        // be the queue will grow.
        RequestQueue = new ArrayBlockingQueue<>(5);
        FlavorsListPriority = new LinkedHashSet<>();
        MapProcessors = new HashMap<>();
        Myshutdownlistener = new ShutdownListener(this);
    }

    /**
     * Resets the Service State Variables.
     */
    private synchronized void resetValues() {
        processingData = false;
        forceProcess = false;
        if (!Myshutdownlistener.CanEngage()) {
            Myshutdownlistener.reset(this);
        }
        try {
            Runtime.getRuntime().addShutdownHook(Myshutdownlistener);
        } catch (Throwable err) {
            LoggingHelper.getLogger(LOGGERNAME).log(Level.SEVERE, "wait to hook the shutdown", err);
        }
        // this should be overkill.
        RequestQueue.clear();
    }

    //<editor-fold defaultstate="collapsed" desc="Add/Remove Handlers">
    /**
     * Add a FlavorHandler to the list of handlers at the end of the list if no
     * prioritized otherwise add it at the start. NOTE: the order of the
     * handlers is important as it will be used to provide priority to the
     * handlers. the first handler in the list will be the first one to be
     * called. and so on.
     *
     * @param @param handler the FlavorProcessor to add
     * @param flavors the Flavor(s) that we want to handle using the provided
     * FlavorProcessor
     * @param priority where to insert the handle. the priority means that it
     * will be added at the start of the List rather than appending it.
     * @throws IllegalStateException if the service is running or processing
     */
    private synchronized void addFlavorHandler(FlavorProcessor handler, boolean priority, DataFlavor... flavors) {
        if (isServiceOnline() || isProcessingTask()) {
            throw new IllegalStateException(
                    "Cannot add FlavorHandler while the service is running or processing data.");
        }
        Objects.requireNonNull(handler, "FlavorProcessor cannot be null");
        Objects.requireNonNull(flavors, "the Flavor cannot be null");
        if (MapProcessors.containsKey(handler)) {
            return;
        }
        var itemHandler = new FlavorHandler(() -> {
            return !isProcessingTask();
        }, handler, flavors);
        MapProcessors.put(handler, itemHandler);
        if (priority) {
            FlavorsListPriority.addFirst(itemHandler);
        } else {
            FlavorsListPriority.addLast(itemHandler);
        }
    }

    /**
     * Add a Flavor processor to the list of handlers at the end of the list.
     * NOTE: the order of the handlers is important as it will be used to
     * provide priority to the handlers. the first handler in the list will be
     * the first one to be called. and so on.
     *
     * @param flavors the Flavor(s) that we want to handle using the provided
     * FlavorProcessor
     * @param handler the FlavorProcessor to register with the provided data
     * flavor
     * @throws IllegalStateException if the service is running or processing
     */
    public final void addFlavorHandler(FlavorProcessor handler, DataFlavor... flavors) {
        addFlavorHandler(handler, false, flavors);
    }

    /**
     * Add a FlavorHandler to the list of handlers at the start of the
     * list.NOTE: the order of the handlers is important as it will be used to
     * provide priority to the handlers. the first handler in the list will be
     * the first one to be called. and so on.
     *
     * @param flavors the Flavor(s) that we want to handle using the provided
     * FlavorProcessor
     * @param handler the FlavorProcessor to add
     * @throws IllegalStateException if the service is running or processing
     */
    public final void addPriorityFlavorHandler(FlavorProcessor handler, DataFlavor... flavors) {
        addFlavorHandler(handler, true, flavors);
    }

    /**
     * removes the specified FlavorProcessor from the list of handling Flavors.
     * if and only if:
     * <ul>
     * <li>the service is <strong>Not</strong> online</li>
     * <li>the service is <strong>Not</strong> processing data</li>
     * </ul>
     *
     * @param handler the {@code FlavorProcessor} to be excluded.
     * @return true if item was removed false otherwise.
     * @throws IllegalStateException if the Service is currently processing data
     * or is online.
     */
    public synchronized boolean RemoveFlavorHandler(FlavorProcessor handler) {
        if (isServiceOnline() || isProcessingTask()) {
            throw new IllegalStateException(
                    "Cannot remove FlavorHandler while the service is running or processing data.");
        }
        var tmp = MapProcessors.remove(handler);
        if (tmp != null) {
            return FlavorsListPriority.remove(tmp);
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Start/Stop Service">
    /**
     * Start The Clipboard Service. this will start the thread that will await
     * for Clipboard changes.
     *
     * @return true if the service started false otherwise (the service was
     * already running, there are no Clipboard Handlers or otherwise fail to
     * init)
     */
    public synchronized boolean StartClipBoardService() {
        // if service alredy running. then we cannot "start it"
        if (serviceOnline) {
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

        serviceOnline = true;
        backerThread = new Thread(this, "ClipBoardListenerThread");
        SYSTEM_CLIPBOARD.addFlavorListener(this);
        backerThread.setDaemon(true);
        // kick the thread to start. lazy bum.
        backerThread.start();
        return true;
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
     * the service was not running.
     */
    public synchronized boolean StopClipBoardService() {
        if (!serviceOnline) {
            return false;
        }
        serviceOnline = false;
        // we will stop the thread that will process the clipboard changes.
        SYSTEM_CLIPBOARD.removeFlavorListener(this);
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
     * service was not running. or not processing clipboard data.
     */
    public synchronized boolean StopClipBoardProcessing() {
        if (!processingData) {
            return false;
        }
        processingData = false;
        return true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Status methods">
    /**
     * Check if the Thread backing this Service is running.the service might be
     * set to stop. but the thread might still be running. to check if the
     * service has requested to stop please check the {@link isServiceOnline}
     *
     * @apiNote the result from this function is immediately deprecated as the
     * service state might change while it is executed.
     * @return true if the service Thread is running. false otherwise.
     * @see ServiceOnline#serviceOnline
     */
    public boolean isServiceThreadRunning() {
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
     * service state might change while it is executed.
     * @return true if the ServiceOnline flag is set to true. false otherwise.
     * @see isServiceThreadRunning
     */
    public boolean isServiceOnline() {
        return serviceOnline;
    }

    /**
     * Check processing Flag and thus check if the last state of the thread was
     * reading/processing data out of the clipboard.
     * <Strong>Note:</strong> this does not mean that the service is running.
     * nor that the service is Online. for such check please refer to
     * {@link isServiceThreadRunning} or {@link isServiceOnline}
     *
     * @apiNote the result from this function is immediately deprecated as the
     * service state might change while it is executed.
     * @return true if the service state is processing data from the clipboard.
     * false otherwise.
     */
    public boolean isProcessingTask() {
        return processingData;
    }
    //</editor-fold>
    
    /**
     * Thread/Runnable method that will process the clipboard changes. this
     * function will loop until the service is stopped. meaning that
     * {@link StopClipBoardService} is called.
     */
    @Override
    public void run() {
        while (serviceOnline) {
            synchronized (this) {
                // if we have work pending lets process it instead.
                if (RequestQueue.isEmpty() && !forceProcess) {
                    try {
                        //wait until notified that we have work to process.
                        this.wait();
                    } catch (InterruptedException e) {
                        LoggingHelper.getLogger(LOGGERNAME).log(Level.WARNING,
                                "Thread Interrupted", e);
                    }
                    //the thead could have been interupted loop and check
                    continue;
                }
                // we will process the clipboard changes.
                forceProcess = false;// disengage the force process flag.
                processingData = true;
            }
            processClipboardChange(getNextQueuedClip());
            synchronized (this) {
                processingData = false;
            }
        }
        Myshutdownlistener.softDisengage();
        try {
            Runtime.getRuntime().removeShutdownHook(Myshutdownlistener);
        } catch (java.lang.IllegalStateException ISE) {
            // this happens if we try to remove a hook while VM is shutting. we dont
            //have a better way to detect this case. thus... lets just absorb it here
        }
    }

    /**
     * gathers the next Clipboard resource from the pool. if there is none it
     * pull the System as it is likely that we were asked to force process the
     * data from the System Clipboard. (most of the time one and the same)
     *
     * @return the clipboard to work with
     */
    private Clipboard getNextQueuedClip() {
        var clipboard = RequestQueue.poll();
        if (clipboard == null) {
            clipboard = SYSTEM_CLIPBOARD;
        }
        // de-queue all the instance for the same Clipboard
        while (!RequestQueue.isEmpty() && clipboard == RequestQueue.peek()) {
            RequestQueue.poll();
        }
        return clipboard;
    }

    /**
     * request to process a change on the Clipboard. that was booked into this
     * service specified Clipboard resource.
     *
     * @param clipboard the clipboard resource to pull the change from
     */
    private void processClipboardChange(Clipboard clipboard) {
        Transferable contents = null;
        boolean errorstate;//we fail to open the clipboard and read its data?
        int pendingRetry = 3;
        do {
            errorstate = false;
            try {
                //request the clipboard to open and provide the metadata. 
                contents = clipboard.getContents(this);
                //the prior call can take a few seconds for exesive ammounts of data on the clipboard so check if we should bail
                if (Objects.isNull(contents) || !processingData) {
                    return;
                }
            } catch (IllegalStateException ise) {
                LoggingHelper.getLogger(LOGGERNAME).log(Level.SEVERE,
                        "Clipboard State Error. Delaying the Procesesing", ise);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                errorstate = true;
            }
        } while (errorstate && pendingRetry-- > 0);
        try {
            // if a interrupt to the process has been requested bail out.
            // also if the content of the Clipboard is null we will bail out.
            if (Objects.isNull(contents) || !processingData) {
                return;
            }
            // check if the clipboard has any of the flavors we are interested in.
            DebugLog(contents);
            for (FlavorHandler handler : FlavorsListPriority) {
                if (!processingData) {
                    return;
                }
                var result = handler.handleFlavor(contents, clipboard);
                // we have processed the flavor. we can bail out.
                if (result) {
                    break;
                }
            }
        } catch (Throwable ex) {
            LoggingHelper.getLogger(LOGGERNAME)
                    .log(Level.SEVERE, "Error Has been catch at processClipboardChange", ex);
        }
        try {
            if (Objects.isNull(contents) || !processingData) {
                return;
            }
            if (!Owner.get()) {
                regainOwnership(contents);
            }
        } catch (IllegalStateException ise) {
            LoggingHelper.getLogger(LOGGERNAME)
                    .log(Level.SEVERE, "cannot regain ownership", ise);
        }
    }

    private void DebugLog(Transferable contents) {
        if (!LoggingHelper.RunningInDebugMode()) {
            return;
        }
        for (DataFlavor flavor : contents.getTransferDataFlavors()) {
            if (flavor == null) {
                continue;
            }
            LoggingHelper.getLogger("Clipboard.Info").log(Level.INFO, "\nFlavor: {0}\nFlavorHandling Class: {1}\nFlavorMimeType: {2}",
                    new Object[]{
                        flavor.getHumanPresentableName(),
                        flavor.getDefaultRepresentationClassAsString(),
                        flavor.getMimeType()}
            );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e the Chang event.
     */
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
            synchronized (ClipboardService.this) {
                if (SkipNext.get()) {
                    SkipNext.set(false);
                    return;
                }
                if (serviceOnline) {
                    if (e.getSource() instanceof Clipboard clipboard) {
                        // we will add the clipboard to the request queue.
                        if (RequestQueue.isEmpty() || !clipboard.equals(RequestQueue.peek())) {
                            try {
                                RequestQueue.add(clipboard);
                            } catch (IllegalStateException fullExpt) {
                                LoggingHelper.getLogger(LOGGERNAME)
                                        .log(Level.WARNING, "Queue is full and all items are different.", fullExpt);
                                ErrorResearchLog(RequestQueue);
                            }
                        }
                    } else if (RequestQueue.isEmpty() || !SYSTEM_CLIPBOARD.equals(RequestQueue.peek())) {
                        RequestQueue.add(SYSTEM_CLIPBOARD);
                    }
                    if (!processingData) {
                        ClipboardService.this.notify();// notify the thread to process the clipboard changes.
                    }
                }
            }
        } else {
            throw new NoSuchMethodError("this event should be triggered by the EDT otherwise smells as fabricated.");
        }

    }

    /**
     * Notifies that this class is not longer the owner of the clipboard. this
     * happens when another application or another object within this
     * application asserts ownership of the clipboard.
     *
     * @param clipboard the clipboard that is no longer owned
     * @param contents the contents which this owner had placed on the
     * {@code clipboard}
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        LoggingHelper.getLogger(LOGGERNAME).log(Level.INFO, "Clipboard Ownership Loss");
        Owner.set(false);
    }

    /**
     * attempts to regain ownership of the SYSTEM Clipboard.
     *
     * @param t Transferable data to set on the clipboard in order to regain
     * ownership. we suggest to set as the same data that it was originally
     * there
     */
    private void regainOwnership(Transferable t) {

        try {
            SkipNext.set(true);
            //if the clipboard has Copious ammount of data.
            //and we just allow the T to go back it will take
            //forever to process the data. 
            //as there are some enconding and reconding that could be happening.
            //to avoid delays lets Wrap it on one that support faster processing
            //using Streams. 

            final var UnderlineTrasferible = t;
            // Create a custom Transferable for byte[] data
            t = new Transferable() {
                private final LinkedHashSet<DataFlavor> StreamFlavored = new LinkedHashSet<>();

                @SuppressWarnings("deprecation")/*ignore. when java remove it we remove the specific part that we need to check*/
                private void populateFlavors() {
                    if (StreamFlavored.isEmpty()) {
                        //first check if the flavor that is native to the ENV is supported and listed. 
                        if (UnderlineTrasferible.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
                            StreamFlavored.add(DataFlavor.getTextPlainUnicodeFlavor());
                        }
                        //add the rest
                        for (DataFlavor transferDataFlavor : UnderlineTrasferible.getTransferDataFlavors()) {
                            if (transferDataFlavor.isRepresentationClassInputStream()
                                    //exclude plainTextFlavor as this one is REALLLY Slow.
                                    && !transferDataFlavor.equals(DataFlavor.plainTextFlavor)) {
                                StreamFlavored.add(transferDataFlavor);
                            }
                        }
                    }
                }

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    populateFlavors();
                    return StreamFlavored.toArray(DataFlavor[]::new);
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    populateFlavors();
                    return StreamFlavored.contains(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws IOException, UnsupportedFlavorException {
                    if (!isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return UnderlineTrasferible.getTransferData(flavor);
                }
            };
            //welp...let hope the data is not exesive. 
            if (t.getTransferDataFlavors().length == 0) {
                t = UnderlineTrasferible;
            }
            SYSTEM_CLIPBOARD.setContents(t, this);
            Owner.set(true);
        } catch (Throwable e) {
            SkipNext.set(false);
            LoggingHelper.getLogger(LOGGERNAME).log(Level.WARNING, "Error attempting to Regain Clipboard Ownership", e);
        }
    }

    /**
     * log the information of a rare edge case that is THEORETICALLY possible.
     *
     * @param RequestQueue the queue that caused a error adding items into it.
     */
    private void ErrorResearchLog(ArrayBlockingQueue<Clipboard> RequestQueue) {
        LoggingHelper.getLogger(LOGGERNAME).entering(this.getClass().getName(), "ErrorResearchLog");
        RequestQueue.forEach((clippy) -> {
            LoggingHelper.getLogger(LOGGERNAME)
                    .log(Level.INFO, "ErrorResearchLog :: Clipboard {0}, Class {1}",
                            new Object[]{clippy.getName(), clippy.getClass().getName()});
        });
        LoggingHelper.getLogger(LOGGERNAME).exiting(this.getClass().getName(), "ErrorResearchLog");
    }

    /**
     * a private class to support the "auto stop" of the service upon VM
     * shutdown
     */
    private class ShutdownListener extends Thread {

        ClipboardService attachedListener;

        private ShutdownListener(ClipboardService theListener) {
            Objects.requireNonNull(theListener);
            attachedListener = theListener;
        }

        private synchronized void reset(ClipboardService theListener) {
            Objects.requireNonNull(theListener);
            attachedListener = theListener;
        }

        private synchronized boolean CanEngage() {
            return Objects.nonNull(attachedListener);
        }

        private synchronized boolean StoptheService() {
            if (Objects.isNull(attachedListener)) {
                return false;
            }
            if (attachedListener.isServiceThreadRunning()) {
                attachedListener.StopClipBoardProcessing();
                attachedListener.StopClipBoardService();
            }
            attachedListener = null;
            return true;
        }

        private synchronized boolean softDisengage() {
            if (Objects.isNull(attachedListener)) {
                return false;
            }
            if (!attachedListener.isServiceThreadRunning() || !attachedListener.isServiceOnline()) {
                attachedListener = null;
            }
            return false;
        }

        @Override
        public void run() {
            if (Objects.nonNull(attachedListener)) {
                StoptheService();
            }
        }

    }
}
