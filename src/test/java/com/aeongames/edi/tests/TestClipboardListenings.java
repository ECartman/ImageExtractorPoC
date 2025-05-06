package com.aeongames.edi.tests;

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
 */
import com.aeongames.edi.utils.Clipboard.ClipBoardListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author cartman
 */
public class TestClipboardListenings {

    public TestClipboardListenings() {
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("Setting up the Enviroment");
        var os = System.getProperty("os.name");
        System.out.println(String.format("Running test on OS %s", os));

    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("Test completed.");
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    @Tag("SigleTest")
    public void ClipboardSingleTest() {
        System.out.println("Running ClipboardSingleTest");
        ClipBoardListener TestingClipboardListener;
        TestingClipboardListener = new ClipBoardListener();
        TestingClipboardListener.addFlavorHandler(DataFlavor.getTextPlainUnicodeFlavor(),(flavor, stopProvider, transferData, clipboard) -> {
            //asure that we are not to stop processing
            if(stopProvider.isStopSignalReceived()) return false;
            try {
                //assure that the flavot is backed by a input stream 
                if(!flavor.isRepresentationClassInputStream())return false;
                //InputStream TrasferableData= (InputStream) transferData.getTransferData(flavor);
                //now for this implementation we should have a InputStream 
                //lets assure that. 
                var reader= flavor.getReaderForText(transferData);
                if(stopProvider.isStopSignalReceived()) return false;
                StringBuilder DataSofar= new StringBuilder();
                char buffer[]= new char[30];
                while(reader.read(buffer)!=-1){
                    DataSofar.append(buffer);
                    System.out.println(DataSofar.toString());
                    if(stopProvider.isStopSignalReceived()) return false;
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(TestClipboardListenings.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        });
        var result = TestingClipboardListener.StartClipBoardService();
        Assertions.assertTrue(result, "The clipboard service fail to start");
        try {
            synchronized (this) {
                this.wait(1000 * 30);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(TestClipboardListenings.class.getName()).log(Level.SEVERE, null, ex);
            Assertions.fail();
        }
       TestingClipboardListener.StopClipBoardService();
       TestingClipboardListener.StopClipBoardProcessing();
    }
}
