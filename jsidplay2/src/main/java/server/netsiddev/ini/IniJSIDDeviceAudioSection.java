package server.netsiddev.ini;

import libsidplay.common.SamplingRate;
import sidplay.audio.AudioConfig;
import sidplay.ini.IniReader;
import sidplay.ini.IniSection;

/**
 * Audio section of the INI file.
 *
 * @author Ken Händel
 *
 */
public class IniJSIDDeviceAudioSection extends IniSection {
	public IniJSIDDeviceAudioSection(IniReader iniReader) {
		super(iniReader);
	}

	/**
	 * Getter of the Playback/Recording frequency.
	 *
	 * @return Playback/Recording frequency
	 */
	public final SamplingRate getSamplingRate() {
		return iniReader.getPropertyEnum("Audio", "Sampling Rate", SamplingRate.MEDIUM, SamplingRate.class);
	}

	/**
	 * Setter of the Playback/Recording frequency.
	 *
	 * @param samplingRate Playback/Recording frequency
	 */
	public final void setSamplingRate(final SamplingRate samplingRate) {
		iniReader.setProperty("Audio", "Sampling Rate", samplingRate);
	}

	public int getAudioBufferSize() {
		return iniReader.getPropertyInt("Audio", "Audio Buffer Size", AudioConfig.getDefaultBufferSize());
	}

}