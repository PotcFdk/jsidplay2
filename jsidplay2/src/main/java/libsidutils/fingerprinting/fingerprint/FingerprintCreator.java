package libsidutils.fingerprinting.fingerprint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import builder.resid.resample.Resampler;
import libsidplay.common.SamplingMethod;
import libsidplay.common.SamplingRate;
import libsidutils.fingerprinting.ini.IFingerprintConfig;
import libsidutils.fingerprinting.rest.beans.WavBean;

/**
 * Created by hsyecheng on 2015/6/13. Generate Fingerprints. The sampling rate
 * of the sample data should be 8000.
 */
public class FingerprintCreator {

	private final Random RANDOM = new Random();
	private int oldRandomValue;

	public Fingerprint createFingerprint(IFingerprintConfig config, WavBean wavBean) throws IOException {
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavBean.getWav()));
			if (stream.getFormat().getSampleSizeInBits() != Short.SIZE) {
				throw new IOException("Sample size in bits must be " + Short.SIZE);
			}
			if (stream.getFormat().getEncoding() != Encoding.PCM_SIGNED) {
				throw new IOException("Encoding must be " + Encoding.PCM_SIGNED);
			}
			if (stream.getFormat().isBigEndian()) {
				throw new IOException("LittleEndian expected");
			}

			// 1. stereo to mono conversion
			byte[] bytes;
			if (stream.getFormat().getChannels() == 2) {
				bytes = new byte[(int) stream.getFrameLength() * stream.getFormat().getChannels() * Short.BYTES];
				stream.read(bytes);
				ShortBuffer stereoSamples = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
				ByteBuffer monoBuffer = ByteBuffer.allocate(bytes.length >> 1).order(ByteOrder.LITTLE_ENDIAN);
				ShortBuffer monoSamples = monoBuffer.asShortBuffer();
				while (stereoSamples.hasRemaining()) {
					monoSamples.put((short) ((stereoSamples.get() + stereoSamples.get()) / 2));
				}
				bytes = monoBuffer.array();
			} else if (stream.getFormat().getChannels() == 1) {
				bytes = new byte[(int) stream.getFrameLength() * Short.BYTES];
				stream.read(bytes);
			} else {
				throw new IOException("Number of channels must be one or two");
			}

			// 2. down sampling to 8KHz
			if (stream.getFormat().getSampleRate() != Fingerprint.SAMPLE_RATE) {
				Resampler downSampler = Resampler.createResampler(stream.getFormat().getSampleRate(),
						SamplingMethod.RESAMPLE, SamplingRate.VERY_LOW.getFrequency(),
						SamplingRate.VERY_LOW.getMiddleFrequency());

				ByteBuffer result = ByteBuffer.allocate(bytes.length).order(ByteOrder.LITTLE_ENDIAN);
				ShortBuffer sb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
				while (sb.hasRemaining()) {
					short val = sb.get();

					int downSamplerDither = triangularDithering();
					if (downSampler.input(val)) {
						if (!result.putShort(
								(short) Math.max(Math.min(downSampler.output() + downSamplerDither, Short.MAX_VALUE),
										Short.MIN_VALUE))
								.hasRemaining()) {
							((Buffer) result).flip();
						}
					}
				}
				bytes = result.array();
			}

			// 3. convert to float mono mix
			ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
			int len = buf.limit() >> 1;
			float[] data = new float[len];
			for (int i = 0; i < len; i++) {
				data[i] = buf.getShort() / 32768f;
			}
			return new Fingerprint(config, data);
		} catch (UnsupportedAudioFileException | IOException e) {
			throw new IOException(e);
		}
	}

	private int triangularDithering() {
		int prevValue = oldRandomValue;
		oldRandomValue = RANDOM.nextInt() & 0x1;
		return oldRandomValue - prevValue;
	}
}
