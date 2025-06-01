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
package com.aeongames.imageextractor.Pojo;

import com.aeongames.edi.utils.File.Properties_File;
import com.aeongames.edi.utils.Pojo.BooleanPropertyPojo;
import com.aeongames.edi.utils.Pojo.IntegerPropertyPojo;
import com.aeongames.edi.utils.Pojo.ListenableProperty;
import com.aeongames.edi.utils.Pojo.PropertyChangeListener;
import com.aeongames.edi.utils.Pojo.PropertyPojo;
import com.aeongames.edi.utils.visual.Panels.ImagePanel;
import com.aeongames.edi.utils.visual.UIlink.BaseBinder;
import com.aeongames.edi.utils.visual.UIlink.ImagePanelBinding;
import com.aeongames.edi.utils.visual.UIlink.JLabelComponentBind;
import com.aeongames.edi.utils.visual.UIlink.JSpinnerComponentBind;
import com.aeongames.edi.utils.visual.UIlink.JtextComponentAppendUpdateBind;
import com.aeongames.edi.utils.visual.UIlink.JtextComponentBind;
import com.aeongames.edi.utils.visual.UIlink.MCBoolCompEnableBind;
import com.aeongames.edi.utils.visual.UIlink.MCBoolEditableBind;
import com.aeongames.edi.utils.visual.UIlink.MCBoolProbarIndeterminate;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.text.JTextComponent;

/**
 * TODO: Encapsulate.
 *
 * @author Eduardo Vindas
 */
public final class ProgressObject {

    private final Properties_File Props;
    private final PropertyPojo<String> CurrentStatus = PropertyPojo.newStringPojo();
    private final PropertyPojo<String> ImageTypeString = PropertyPojo.newStringPojo();
    private final PropertyPojo<BufferedImage> ImageProperty = new PropertyPojo<>();
    private final PropertyPojo<String> SavingFilePath = PropertyPojo.newStringPojo();
    private final PropertyPojo<String> statusBarInfo = PropertyPojo.newStringPojo();
    private final IntegerPropertyPojo CurrentFileNumber = new IntegerPropertyPojo();
    private final BooleanPropertyPojo CurrentUIEnablement = new BooleanPropertyPojo();
    public final ArrayList<BaseBinder<?, ? extends JComponent>> Bindings;

    public ProgressObject() {
        Bindings = new ArrayList<>(10);
        Props = new Properties_File(Path.of("props.xml"));
        SavingFilePath.addPropertyListener((source, newValue) -> {
            Props.setProperty("folder", newValue);
            Props.SaveIfNeeded();
        });
        CurrentFileNumber.addPropertyListener((var source, var newValue) -> {
            Props.setProperty("Page", newValue.toString());
            Props.SaveIfNeeded();
        });
    }

    public void setImageTypeString(String value) {
        if (Objects.nonNull(value)) {
            ImageTypeString.setValue(value);
        } else {
            ImageTypeString.setValue("");
        }
    }
    
    public void updateFromSettings(){
        var folder= Props.getProperty("folder");
        var page= Props.getProperty("Page");
        if(Objects.nonNull(folder)){
            SavingFilePath.setValue(folder);
        }
        if(Objects.nonNull(page)){
            int currepage= Integer.parseInt(page);
            CurrentFileNumber.setValue(currepage);
        }       
    }

    public void setUIEnablement(boolean b) {
        CurrentUIEnablement.setValue(b);
    }

    public void setCurrentFileNumber(int integer) {
        CurrentFileNumber.setValue(integer);
    }

    public void setImageData(BufferedImage image) {
        ImageProperty.setValue(image);
    }

    public Integer getFileNumber() {
        return CurrentFileNumber.getValue();
    }

    public void fileNumberpplus() {
        CurrentFileNumber.plusplus();
    }

    public void setStatus(String str) {
        statusBarInfo.setValue(str);
    }

    public void updateStatus(String message) {
        CurrentStatus.setValue(message);
    }
    
    public void registerSavingFilePath(PropertyChangeListener<String, ListenableProperty<String>> Listener){
        SavingFilePath.addPropertyListener(Listener);
    }

    public MCBoolProbarIndeterminate bindIndeterminateProgressBar(JProgressBar tobind) {
        MCBoolProbarIndeterminate statusBind = new MCBoolProbarIndeterminate(tobind, CurrentUIEnablement, true);
        Bindings.add(statusBind);
        return statusBind;
    }

    public JtextComponentAppendUpdateBind bindCurrentStatus(JTextComponent tobind) {
        JtextComponentAppendUpdateBind statusBind = new JtextComponentAppendUpdateBind(tobind, CurrentStatus);
        Bindings.add(statusBind);
        return statusBind;
    }

    public JtextComponentBind bindSavingFile(JTextComponent tobind) {
        JtextComponentBind BindFilePath = new JtextComponentBind(tobind, SavingFilePath);
        Bindings.add(BindFilePath);
        return BindFilePath;
    }

    public MCBoolEditableBind bindEditableTxtComp(JTextComponent... tobind) {
        Objects.requireNonNull(tobind, "you need to provide at least 1 item");
        if (tobind.length < 1) {
            throw new IllegalArgumentException("you need to provide at least 1 item");
        }
        MCBoolEditableBind binding = new MCBoolEditableBind(tobind[0], CurrentUIEnablement);
        for (int i = 1; i < tobind.length; i++) {
            binding.addComponent(tobind[i]);
        }
        Bindings.add(binding);
        return binding;
    }

    public JLabelComponentBind bindLabel(PropertyPojo<String> pojo, JLabel label) {
        var lbind = new JLabelComponentBind(label, pojo);
        Bindings.add(lbind);
        return lbind;
    }

    public JLabelComponentBind bindImageType(JLabel label) {
        return bindLabel(ImageTypeString, label);
    }

    public JLabelComponentBind bindstatusBarInfo(JLabel label) {
        return bindLabel(statusBarInfo, label);
    }

    public MCBoolCompEnableBind bindEnabledComp(JComponent... comps) {
        Objects.requireNonNull(comps, "you need to provide at least 1 item");
        if (comps.length < 1) {
            throw new IllegalArgumentException("you need to provide at least 1 item");
        }
        MCBoolCompEnableBind binding = new MCBoolCompEnableBind(comps[0], CurrentUIEnablement);
        for (int i = 1; i < comps.length; i++) {
            binding.addComponent(comps[i]);
        }
        Bindings.add(binding);
        return binding;
    }

    public JSpinnerComponentBind bindFileNumber(JSpinner FileSpiner) {
        JSpinnerComponentBind BinFileNameSpinner = new JSpinnerComponentBind(FileSpiner, CurrentFileNumber);
        Bindings.add(BinFileNameSpinner);
        return BinFileNameSpinner;
    }

    public ImagePanelBinding bindImage(ImagePanel PImage) {
        ImagePanelBinding ImageBind = new ImagePanelBinding(PImage, ImageProperty);
        Bindings.add(ImageBind);
        return ImageBind;
    }

}
