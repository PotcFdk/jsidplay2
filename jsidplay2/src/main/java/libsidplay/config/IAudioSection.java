package libsidplay.config;

import libsidplay.common.SamplingMethod;
import libsidplay.common.SamplingRate;
import sidplay.audio.Audio;

public interface IAudioSection {

	/**
	 * Getter of the audio to be used.
	 * 
	 * @return the audio to be used
	 */
	Audio getAudio();

	/**
	 * Setter of the audio to be used.
	 * 
	 * @param audio audio to be used
	 */
	void setAudio(Audio audio);

	int getDevice();

	void setDevice(int device);

	/**
	 * Getter of the Playback/Recording frequency.
	 * 
	 * @return Playback/Recording frequency
	 */
	SamplingRate getSamplingRate();

	/**
	 * Setter of the sampling rate.
	 * 
	 * @param sampligRate sampling rate
	 */
	void setSamplingRate(SamplingRate sampligRate);

	/**
	 * Getter of the sampling method.
	 * 
	 * @return the sampling method
	 */
	SamplingMethod getSampling();

	/**
	 * Setter of the sampling method.
	 * 
	 * @param method the sampling method
	 */
	void setSampling(SamplingMethod method);

	/**
	 * Do we play the recording?
	 * 
	 * @return play the recording
	 */
	boolean isPlayOriginal();

	/**
	 * Setter to play the recorded tune.
	 * 
	 * @param original Play recorded (original) or emulated tune
	 */
	void setPlayOriginal(boolean original);

	/**
	 * Getter of the recorded tune filename.
	 * 
	 * @return the recorded tune filename
	 */
	String getMp3File();

	/**
	 * Setter of the recorded tune filename.
	 * 
	 * @param recording the recorded tune filename
	 */
	void setMp3File(String recording);

	/**
	 * Getter of the main SID volume setting.
	 * 
	 * @return the main SID volume setting
	 */
	float getMainVolume();

	/**
	 * Setter of the main SID volume setting.
	 * 
	 * @param volume the main SID volume setting
	 */
	void setMainVolume(float volume);

	/**
	 * Getter of the second SID volume setting.
	 * 
	 * @return the second SID volume setting
	 */
	float getSecondVolume();

	/**
	 * Setter of the second SID volume setting.
	 * 
	 * @param volume the second SID volume setting
	 */
	void setSecondVolume(float volume);

	/**
	 * Getter of the third SID volume setting.
	 * 
	 * @return the third SID volume setting
	 */
	float getThirdVolume();

	/**
	 * Setter of the third SID setting.
	 * 
	 * @param volume the third SID volume setting
	 */
	void setThirdVolume(float volume);

	/**
	 * Getter of the main SID balance setting (0 - left, 1 - right speaker).
	 * 
	 * @return the main SID balance setting
	 */
	float getMainBalance();

	/**
	 * Setter of the main SID balance setting (0 - left, 1 - right speaker).
	 * 
	 * @param balance the main SID balance setting
	 */
	void setMainBalance(float balance);

	/**
	 * Getter of the second SID balance setting (0 - left, 1 - right speaker).
	 * 
	 * @return the second SID balance setting
	 */
	float getSecondBalance();

	/**
	 * Setter of the second SID balance setting (0 - left, 1 - right speaker).
	 * 
	 * @param balance the second SID balance setting
	 */
	void setSecondBalance(float balance);

	/**
	 * Getter of the third SID balance setting (0 - left, 1 - right speaker).
	 * 
	 * @return the third SID balance setting
	 */
	float getThirdBalance();

	/**
	 * Setter of the third SID balance setting (0 - left, 1 - right speaker).
	 * 
	 * @param balance the third SID balance setting
	 */
	void setThirdBalance(float balance);

	/**
	 * Getter of the main SID delay setting (0 - no delay, 200 - 200ms delay).
	 * 
	 * @return the main SID delay setting
	 */
	int getMainDelay();

	/**
	 * Setter of the main SID delay setting (0 - no delay, 200 - 200ms delay).
	 * 
	 * @param delay the main SID delay setting
	 */
	void setMainDelay(int delay);

	/**
	 * Getter of the second SID delay setting (0 - no delay, 200 - 200ms delay).
	 * 
	 * @return the second SID delay setting
	 */
	int getSecondDelay();

	/**
	 * Setter of the second SID delay setting (0 - no delay, 200 - 200ms delay).
	 * 
	 * @param delay the second SID delay setting
	 */
	void setSecondDelay(int delay);

	/**
	 * Getter of the third SID delay setting (0 - no delay, 200 - 200ms delay).
	 * 
	 * @return the third SID delay setting
	 */
	int getThirdDelay();

	/**
	 * Setter of the third SID delay setting (0 - no delay, 200 - 200ms delay).
	 * 
	 * @param delay the third SID delay setting
	 */
	void setThirdDelay(int delay);

	/**
	 * Getter of the output buffer size.
	 * 
	 * @return size of the output buffer
	 */
	int getBufferSize();

	/**
	 * Setter of the output buffer size.
	 * 
	 * @param bufferSize output buffer size
	 */
	void setBufferSize(int bufferSize);

	/**
	 * Getter of the audio buffer size.
	 * 
	 * @return size of the audio buffer
	 */
	int getAudioBufferSize();

	/**
	 * Setter of the audio buffer size.
	 * 
	 * @param bufferSize audio buffer size
	 */
	void setAudioBufferSize(int audioBufferSize);

	default float getVolume(int sidNum) {
		switch (sidNum) {
		case 0:
			return getMainVolume();
		case 1:
			return getSecondVolume();
		case 2:
			return getThirdVolume();
		default:
			throw new RuntimeException(String.format("Maximum supported SIDS exceeded: %d!", sidNum));
		}
	}

	default float getBalance(int sidNum) {
		switch (sidNum) {
		case 0:
			return getMainBalance();
		case 1:
			return getSecondBalance();
		case 2:
			return getThirdBalance();
		default:
			throw new RuntimeException(String.format("Maximum supported SIDS exceeded: %d!", sidNum));
		}
	}

	default int getDelay(int sidNum) {
		switch (sidNum) {
		case 0:
			return getMainDelay();
		case 1:
			return getSecondDelay();
		case 2:
			return getThirdDelay();
		default:
			throw new RuntimeException(String.format("Maximum supported SIDS exceeded: %d!", sidNum));
		}
	}
}