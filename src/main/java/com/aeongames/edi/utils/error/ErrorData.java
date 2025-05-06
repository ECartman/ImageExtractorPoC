/*
 *  Copyright © 2024 Eduardo Vindas. All rights reserved.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.aeongames.edi.utils.error;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 *
 * @author Eduardo Vindas cartman aeongames
 */
public class ErrorData {

    private final String ErrorTittle;
    private final String ErrorMessage;
    private final Throwable error;

    public ErrorData(Throwable error) {
        error=Objects.requireNonNull(error, "the error cannot be null");
        ErrorTittle = "Error on Execution";
        ErrorMessage = Objects.requireNonNullElse(error.getMessage(),
               Objects.requireNonNullElse(error.getCause().getMessage(),"Error during Execution"));
        this.error = error;
    }

    public ErrorData(String title, String Message, Throwable err) {
        error = Objects.requireNonNull(err, "the error cannot be null");
        title = Objects.requireNonNullElse(title, "Error on Execution");
        ErrorTittle = title.strip().equals("") ? "Error on Execution" : title;
        if (Message != null &&  !Message.strip().equals("") ) {
            ErrorMessage = Message;
        } else if (error != null) {
            ErrorMessage = Objects.requireNonNullElse(error.getMessage(),
               Objects.requireNonNullElse(error.getCause().getMessage(),"Error during Execution"));
        } else {
            ErrorMessage = "Error on the Application, details are not provided.";
        }
    }

    public ErrorData(String Message, Throwable err) {
        error = Objects.requireNonNull(err, "the error cannot be null");
        ErrorTittle = "Error on Execution";
        if (Message != null &&  !Message.strip().equals("") ) {
            ErrorMessage = Message;
        } else if (error != null) {
            ErrorMessage = error.getMessage();
        } else {
            ErrorMessage = "Error on the Application, details are not provided.";
        }
    }

    /**
     * @return the ErrorTittle
     */
    public String getErrorTittle() {
        return ErrorTittle;
    }

    /**
     * @return the ErrorMessage
     */
    public String getErrorMessage() {
        return ErrorMessage;
    }

    /**
     * builds and returns a String representation of the stack when the error 
     * was captured. 
     * @return a String with the output from the Error.
     * @see Throwable#printStackTrace(java.io.PrintWriter)
     */
    public String getErrorStack() {
        String ErrorStackString = "";
        try (StringWriter writer = new StringWriter()) {
            try (PrintWriter out = new PrintWriter(writer)) {
                error.printStackTrace(out);
                if(error.getCause()!=null){
                    error.getCause().printStackTrace(out);
                }
                //out.flush(); //StringWritter flush does nothing.
               ErrorStackString= writer.toString();
            }
        } catch (IOException e) {
        }
        return ErrorStackString;
    }
}
