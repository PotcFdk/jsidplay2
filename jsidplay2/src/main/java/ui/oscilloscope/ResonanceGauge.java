package ui.oscilloscope;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import libsidplay.common.SIDEmu;
import sidplay.Player;
import ui.common.C64Window;

public final class ResonanceGauge extends SIDGauge {

	@FXML
	private TitledPane border;
	@FXML
	private Canvas area;

	private List<Image> images = new ArrayList<>();
	
	public ResonanceGauge() {
	}
	
	public ResonanceGauge(C64Window window, Player player) {
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
		int res = (sidemu.readInternalRegister(0x17) >> 4) & 0xf;
		accumulate(res / 15f);
		return this;
	}
}