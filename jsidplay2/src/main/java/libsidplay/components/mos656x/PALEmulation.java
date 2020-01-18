package libsidplay.components.mos656x;

import java.util.function.Consumer;

public class PALEmulation {

	/**
	 * If PAL emulation is turned off: use this palette for VIC colors 0-15.
	 */
	private static int[] VIC_PALETTE_NO_PAL = new int[] { 0xff000000, 0xffffffff, 0xff880000, 0xffaaffee, 0xffcc44cc,
			0xff00cc55, 0xff0000aa, 0xffeeee77, 0xffdd8855, 0xff664400, 0xffff7777, 0xff333333, 0xff777777, 0xffaaff66,
			0xff0088ff, 0xffbbbbbb };

	/**
	 * Alpha channel of ARGB pixel data.
	 */
	private static final int ALPHA = 0xff000000;

	private int[] combinedLinesCurrent;
	/**
	 * Table for looking up color using a packed 2x8 value for even rasterlines
	 */
	private final int[] combinedLinesEven = new int[256 * 256];
	/**
	 * Table for looking up color using a packed 2x8 value for odd rasterlines
	 */
	private final int[] combinedLinesOdd = new int[256 * 256];
	/** Prevailing VIC color palette for current line (odd/even) */
	private byte[] linePaletteCurrent;
	/** VIC color palette for even rasterlines */
	private final byte[] linePaletteEven = new byte[16 * 16 * 16 * 16];
	/** VIC color palette for odd rasterlines */
	private final byte[] linePaletteOdd = new byte[16 * 16 * 16 * 16];
	/** Last line's color */
	private final byte[] previousLineDecodedColor = new byte[65 * 8];
	/** Index into last line */
	private int previousLineIndex;

	/** Previous sequencer data */
	protected int oldGraphicsData;

	private final VIC.Model model;

	/** System's palette */
	private final Palette palette = new Palette();

	public PALEmulation(VIC.Model model) {
		this.model = model;
	}

	/**
	 * Gets the currently used palette.
	 *
	 * @return The currently used palette.
	 */
	public Palette getPalette() {
		return palette;
	}

	/**
	 * Updates the palette
	 */
	public void updatePalette() {
		palette.calculatePalette(Palette.buildPaletteVariant(model));
		System.arraycopy(palette.getEvenLines(), 0, combinedLinesEven, 0, combinedLinesEven.length);
		System.arraycopy(palette.getOddLines(), 0, combinedLinesOdd, 0, combinedLinesOdd.length);
		System.arraycopy(palette.getEvenFiltered(), 0, linePaletteEven, 0, linePaletteEven.length);
		System.arraycopy(palette.getOddFiltered(), 0, linePaletteOdd, 0, linePaletteOdd.length);
	}

	public void determineCurrentPalette(int rasterY, boolean isFrameStart) {
		previousLineIndex = 0;
		if (isFrameStart) {
			/* current row odd? -> start with even, init, swap */
			linePaletteCurrent = (rasterY & 1) != 0 ? linePaletteEven : linePaletteOdd;
			combinedLinesCurrent = (rasterY & 1) != 0 ? combinedLinesEven : combinedLinesOdd;
			for (int i = 0; i < previousLineDecodedColor.length; i++) {
				previousLineDecodedColor[i] = linePaletteCurrent[0];
			}
		}
		linePaletteCurrent = linePaletteCurrent == linePaletteOdd ? linePaletteEven : linePaletteOdd;
		combinedLinesCurrent = combinedLinesCurrent == combinedLinesOdd ? combinedLinesEven : combinedLinesOdd;
		oldGraphicsData = 0;
	}

	public void drawPixels(int graphicsDataBuffer, Consumer<Integer> pixelConsumer, boolean enable) {
		/* Pixels arrive in 0x12345678 order. */
		for (int j = 0; j < 2; j++) {
			oldGraphicsData |= graphicsDataBuffer >>> 16;
			for (int i = 0; i < 4; i++) {
				oldGraphicsData <<= 4;
				final int vicColor = oldGraphicsData >>> 16;
				final byte lineColor = linePaletteCurrent[vicColor];
				final byte previousLineColor = previousLineDecodedColor[previousLineIndex];
				if (enable) {
					pixelConsumer
							.accept(ALPHA | combinedLinesCurrent[lineColor & 0xff | previousLineColor << 8 & 0xff00]);
				} else {
					pixelConsumer.accept(VIC_PALETTE_NO_PAL[vicColor & 0x0f]);
				}
				previousLineDecodedColor[previousLineIndex++] = lineColor;
			}
			graphicsDataBuffer <<= 16;
		}

	}

}