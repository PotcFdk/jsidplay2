package ui.oscilloscope;

import java.util.ArrayList;
import java.util.List;

import builder.resid.ReSIDBase;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import libsidplay.common.SIDEmu;
import sidplay.Player;
import ui.common.C64Window;

public final class WaveGauge extends SIDGauge {

	@FXML
	private TitledPane border;
	@FXML
	private Canvas area;

	private List<Image> images = new ArrayList<>();
	
	public WaveGauge() {
	}
	
	public WaveGauge(C64Window window, Player player) {
		super(window, player);
	}

	@Override
	protected Canvas getArea() {
		return area;
	}

	@Override
	protected List<Image> getImages() {
		return images;
	}

	@Override
	protected TitledPane getTitledPane() {
		return border;
	}

	@Override
	public SIDGauge sample(SIDEmu sidemu) {
		if (sidemu instanceof ReSIDBase) {
			accumulate((((ReSIDBase) sidemu).readOSC(getVoice()) & 0xff) / 255f);
		} else {
			accumulate(0f);
		}
		return this;
	}

	@Override
	public void addImage(SIDEmu sidemu) {
		super.addImage(sidemu);
		if (sidemu != null) {
			final byte wf = sidemu.readInternalRegister(4 + 7 * getVoice());
			final byte filt = sidemu.readInternalRegister(0x17);
			setText(String.format(localizer.getString("WAVE") + " %X %s%s%s%s", wf >> 4 & 0xf, (wf & 2) != 0 ? "S" : "",
					(wf & 4) != 0 ? "R" : "", (wf & 8) != 0 ? "T" : "", (filt & 1 << getVoice()) != 0 ? "F" : ""));
		}
	}

}