package sidplay.audio;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import libsidplay.config.IAudioSection;
import libsidplay.sidtune.MP3Tune;
import libsidplay.sidtune.SidTune;
import sidplay.audio.MP3Driver.MP3File;
import sidplay.audio.WAVDriver.WavFile;

/**
 * Audio output.
 * 
 * @author ken
 *
 */
public enum Audio {
	/** Java Sound API. */
	SOUNDCARD(null, JavaSound.class),
	/** WAV file write. */
	WAV(".wav", WavFile.class),
	/** MP3 file write. */
	MP3(".mp3", MP3File.class),
	/** AVI file write. */
	AVI(".avi", AVIDriver.class),
	/** MP4 file write. */
	MP4(".mp4", MP4Driver.class),
	/** Java Sound API plus WAV file write. */
	LIVE_WAV(".wav", ProxyDriver.class, JavaSound.class, WavFile.class),
	/** Java Sound API plus MP3 file write. */
	LIVE_MP3(".mp3", ProxyDriver.class, JavaSound.class, MP3File.class),
	/** Java Sound API plus AVI file write. */
	LIVE_AVI(".avi", ProxyDriver.class, JavaSound.class, AVIDriver.class),
	/** MP4 file write. */
	LIVE_MP4(".mp4", ProxyDriver.class, JavaSound.class, MP4Driver.class),
	/** Java Sound API plus play-back of MP3 recording. */
	COMPARE_MP3(null, CmpMP3File.class);

	private String extension;
	private final Class<? extends AudioDriver> audioDriverClass, parameterClasses[];
	private AudioDriver audioDriver;

	/**
	 * Create audio output using the audio driver
	 * 
	 * @param audioDriver audio driver
	 */
	@SafeVarargs
	Audio(String extension, Class<? extends AudioDriver> audioDriverClass, Class<? extends AudioDriver>... parameters) {
		this.extension = extension;
		this.audioDriverClass = audioDriverClass;
		this.parameterClasses = parameters;
	}

	/**
	 * @return file extension for recordings
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Get audio driver.
	 * 
	 * <B>Note:</B> Audio drivers are instantiated at runtime on demand. We do not
	 * want to load unused libraries like jump3r, if not required!<BR>
	 * 
	 * <B>Note2:</B> We try to reuse audio driver instances for the proxy driver's
	 * sub-driver. The reason for this is, that it may have already been configured
	 * by ConsolePlayer's command-line parameters (e.g. quality settings of
	 * MP3Driver).
	 * 
	 * @return audio driver
	 */
	public final AudioDriver getAudioDriver() {
		try {
			if (audioDriver == null) {
				Class<?> parameterTypes[] = Stream.<Class<?>>generate(() -> AudioDriver.class)
						.limit(parameterClasses.length).toArray(Class<?>[]::new);
				Collection<Object> initArgs = new ArrayList<>();
				for (Class<?> parameterClass : parameterClasses) {
					initArgs.add(Stream.of(values()).map(audio -> audio.audioDriver)
							.filter(audioDriver -> parameterClass.isInstance(audioDriver)).findFirst()
							.orElse((AudioDriver) parameterClass.getConstructor().newInstance()));
				}
				audioDriver = audioDriverClass.getConstructor(parameterTypes).newInstance(initArgs.toArray());
			}
			return audioDriver;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Audiodriver cannot be instanciated: " + audioDriverClass.getName(), e);
		}
	}

	/**
	 * Get audio driver for tune.<BR>
	 * <B>Note:</B>Use MP3 comparison driver for MP3 play-back.
	 * 
	 * @param audioSection configuration
	 * @param tune         SID tune
	 */
	public final AudioDriver getAudioDriver(final IAudioSection audioSection, final SidTune tune) {
		AudioDriver audioDriver = getAudioDriver();
		if (tune instanceof MP3Tune) {
			audioSection.setMp3File(((MP3Tune) tune).getMP3Filename());
			audioSection.setPlayOriginal(true);
			audioDriver = COMPARE_MP3.getAudioDriver();
		}
		if (COMPARE_MP3.audioDriver == audioDriver) {
			((CmpMP3File) audioDriver).setAudioSection(audioSection);
		}
		return audioDriver;
	}

}