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
package com.aeongames.imgext.components;

import com.aeongames.edi.utils.file.Properties_File;
import com.aeongames.edi.utils.pojo.BooleanPropertyPojo;
import com.aeongames.edi.utils.pojo.IntegerPropertyPojo;
import com.aeongames.edi.utils.pojo.ListenableProperty;
import com.aeongames.edi.utils.pojo.PathPropertyPojo;
import com.aeongames.edi.utils.pojo.PropertyChangeListener;
import com.aeongames.edi.utils.pojo.PropertyPojo;
import com.aeongames.edi.utils.visual.Panels.ImagePanel;
import com.aeongames.edi.utils.visual.pojouilink.BaseBinder;
import com.aeongames.edi.utils.visual.pojouilink.BaseBinder.BindSync;
import com.aeongames.edi.utils.visual.pojouilink.ImagePanelBinding;
import com.aeongames.edi.utils.visual.pojouilink.JLabelComponentBind;
import com.aeongames.edi.utils.visual.pojouilink.JSpinnerComponentBind;
import com.aeongames.edi.utils.visual.pojouilink.JtextComponentAppendUpdateBind;
import com.aeongames.edi.utils.visual.pojouilink.JtextPathBind;
import com.aeongames.edi.utils.visual.pojouilink.MCBoolCompEnableBind;
import com.aeongames.edi.utils.visual.pojouilink.MCBoolEditableBind;
import com.aeongames.edi.utils.visual.pojouilink.MCBoolProbarIndeterminate;
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
    private final PathPropertyPojo SavingFilePath = new PathPropertyPojo();
    private final PropertyPojo<String> statusBarInfo = PropertyPojo.newStringPojo();
    private final IntegerPropertyPojo CurrentFileNumber = new IntegerPropertyPojo();
    private final BooleanPropertyPojo CurrentUIEnablement = new BooleanPropertyPojo();
    public final ArrayList<BaseBinder<?, ? extends JComponent>> Bindings;

    public ProgressObject() {
        Bindings = new ArrayList<>(10);
        Props = new Properties_File(Path.of("props.xml"));
        SavingFilePath.setValue(Props.getProperty("folder"));
        SavingFilePath.addPropertyListener((source, newValue) -> {
            Props.setProperty("folder", newValue.toAbsolutePath().toString());
            Props.SaveIfNeeded();
        });
        CurrentFileNumber.addPropertyListener((var source, var newValue) -> {
            Props.setProperty("Page", newValue.toString());
            Props.SaveIfNeeded();
        });
    }
    
    public void updateFromSettings() {
        var folder = Props.getProperty("folder");
        var page = Props.getProperty("Page");
        if (Objects.nonNull(folder)) {
            SavingFilePath.setValue(folder);
        }
        if (Objects.nonNull(page)) {
            int currepage = Integer.parseInt(page);
            CurrentFileNumber.setValue(currepage);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Setters">
    public void setImageTypeString(String value) {
        if (Objects.nonNull(value)) {
            ImageTypeString.setValue(value);
        } else {
            ImageTypeString.setValue("");
        }
    }

    public void setUIEnablement(boolean b) {
        CurrentUIEnablement.setValue(b);
    }

    public void setCurrentFileNumber(int integer) {
        CurrentFileNumber.setValue(integer);
    }

    public boolean setSavingFilePath(Path path) {
        return SavingFilePath.tryUpdateProperty(path);
    }

    public void setImageData(BufferedImage image) {
        ImageProperty.setValue(image);
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
    //</editor-fold>
    
    public Integer getFileNumber() {
        return CurrentFileNumber.getValue();
    }
    
    public Path getSaveFilePath(){
        return SavingFilePath.getValue();
    }

    public void registerSavingFilePath(PropertyChangeListener<Path, ListenableProperty<Path>> Listener) {
        SavingFilePath.addPropertyListener(Listener);
    }

    //<editor-fold defaultstate="collapsed" desc="Binds">
    public MCBoolProbarIndeterminate bindIndeterminateProgressBar(JProgressBar tobind) {
        var statusBind = new MCBoolProbarIndeterminate(tobind, CurrentUIEnablement, true);
        Bindings.add(statusBind);
        return statusBind;
    }

    public JtextComponentAppendUpdateBind bindCurrentStatus(JTextComponent tobind) {
        var statusBind = new JtextComponentAppendUpdateBind(tobind, CurrentStatus);
        Bindings.add(statusBind);
        return statusBind;
    }

    public JtextPathBind bindSavingFile(JTextComponent tobind) {
        var BindFilePath = new JtextPathBind(tobind, SavingFilePath,BindSync.SYNC_FROM_BEAN);
        Bindings.add(BindFilePath);
        return BindFilePath;
    }

    public MCBoolEditableBind bindEditableTxtComp(JTextComponent... tobind) {
        Objects.requireNonNull(tobind, "you need to provide at least 1 item");
        if (tobind.length < 1) {
            throw new IllegalArgumentException("you need to provide at least 1 item");
        }
        var binding = new MCBoolEditableBind(tobind[0], CurrentUIEnablement);
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
        var binding = new MCBoolCompEnableBind(comps[0], CurrentUIEnablement);
        for (int i = 1; i < comps.length; i++) {
            binding.addComponent(comps[i]);
        }
        Bindings.add(binding);
        return binding;
    }

    public JSpinnerComponentBind bindFileNumber(JSpinner FileSpiner) {
        var BinFileNameSpinner = new JSpinnerComponentBind(FileSpiner, CurrentFileNumber);
        Bindings.add(BinFileNameSpinner);
        return BinFileNameSpinner;
    }

    public ImagePanelBinding bindImage(ImagePanel PImage) {
        var ImageBind = new ImagePanelBinding(PImage, ImageProperty);
        Bindings.add(ImageBind);
        return ImageBind;
    }
    //</editor-fold>
}
