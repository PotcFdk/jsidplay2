package builder.sidblaster;

import static libsidplay.common.Engine.SIDBLASTER;
import static libsidplay.components.pla.PLA.MAX_SIDS;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.EnumConverter;

import builder.hardsid.WState;
import libsidplay.common.CPUClock;
import libsidplay.common.ChipModel;
import libsidplay.common.Event;
import libsidplay.common.EventScheduler;
import libsidplay.common.HardwareSIDBuilder;
import libsidplay.common.Mixer;
import libsidplay.common.SIDEmu;
import libsidplay.config.IAudioSection;
import libsidplay.config.IConfig;
import libsidplay.config.IEmulationSection;
import libsidplay.sidtune.SidTune;
import sidplay.audio.AudioDriver;

/**
 * 
 * Support of SIDBlaster mini USB devices.
 *
 * @author Ken Händel
 *
 */
public class SIDBlasterBuilder implements HardwareSIDBuilder, Mixer {

	private static final short REGULAR_DELAY = 4096;

	/**
	 * System event context.
	 */
	private EventScheduler context;

	/**
	 * Configuration
	 */
	private IConfig config;

	/**
	 * CPU clock.
	 */
	private CPUClock cpuClock;

	/**
	 * Native library wrapper.
	 */
	private static HardSID hardSID;

	/**
	 * Number of SIDBlaster devices.
	 */
	private static int deviceCount;

	/**
	 * Serial numbers of SIDBlaster devices.
	 */
	private static String[] serialNumbers;

	/**
	 * Already used SIDBlaster SIDs.
	 */
	private List<SIDBlasterEmu> sids = new ArrayList<>();

	private long lastSIDWriteTime;

	private int fastForwardFactor;

	private int[] delayInCycles = new int[MAX_SIDS];

	public SIDBlasterBuilder(EventScheduler context, IConfig config, CPUClock cpuClock) {
		this.context = context;
		this.config = config;
		this.cpuClock = cpuClock;
		if (hardSID == null) {
			try {
				hardSID = Native.load("hardsid", HardSID.class, createOptions());
				init();
			} catch (UnsatisfiedLinkError e) {
				System.err.println("Error: Windows is required to use " + SIDBLASTER + " soundcard!");
				throw e;
			}
		}
	}

	private Map<String, Object> createOptions() {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(Library.OPTION_TYPE_MAPPER, new DefaultTypeMapper() {
			{
				addTypeConverter(WState.class, new EnumConverter<WState>(WState.class));
				addTypeConverter(SIDType.class, new EnumConverter<SIDType>(SIDType.class));
			}
		});
		return options;
	}

	private void init() {
		hardSID.HardSID_SetWriteBufferSize((byte) config.getEmulationSection().getSidBlasterWriteBufferSize());
		deviceCount = hardSID.HardSID_Devices();
		serialNumbers = new String[deviceCount];
		for (byte deviceId = 0; deviceId < deviceCount; deviceId++) {
			serialNumbers[deviceId] = hardSID.GetSerial(deviceId);
		}
	}

	@Override
	public SIDEmu lock(SIDEmu oldHardSID, int sidNum, SidTune tune) {
		IAudioSection audioSection = config.getAudioSection();
		IEmulationSection emulationSection = config.getEmulationSection();
		ChipModel chipModel = ChipModel.getChipModel(emulationSection, tune, sidNum);

		SimpleEntry<Integer, ChipModel> deviceIdAndChipModel = getModelDependantDeviceId(chipModel, sidNum,
				emulationSection.getSidBlasterSerialNumber());

		Integer deviceId = deviceIdAndChipModel.getKey();
		ChipModel model = deviceIdAndChipModel.getValue();

		if (deviceId != null && deviceId < deviceCount) {
			if (oldHardSID != null) {
				// always re-use hardware SID chips, if configuration changes
				// the purpose is to ignore chip model changes!
				return oldHardSID;
			}
			SIDBlasterEmu hsid = createSID(deviceId.byteValue(), sidNum, tune, model);

			if (hsid.lock()) {
				sids.add(hsid);
				setDeviceName(sidNum, serialNumbers[deviceId]);
				setDelay(sidNum, audioSection.getDelay(sidNum));
				return hsid;
			}
		}
		System.err.printf("SIDBLASTER ERROR: System doesn't have enough SID chips. Requested: (sidNum=%d)\n", sidNum);
		return SIDEmu.NONE;
	}

	private SIDBlasterEmu createSID(byte deviceId, int sidNum, SidTune tune, ChipModel chipModel) {
		if (SidTune.isFakeStereoSid(config.getEmulationSection(), tune, sidNum)) {
			return new SIDBlasterEmu.FakeStereo(context, config, this, hardSID, deviceId, sidNum, chipModel, sids);
		} else {
			return new SIDBlasterEmu(context, this, hardSID, deviceId, sidNum, chipModel);
		}
	}

	@Override
	public void unlock(final SIDEmu sidEmu) {
		SIDBlasterEmu hardSid = (SIDBlasterEmu) sidEmu;
		hardSid.unlock();
		sids.remove(sidEmu);
	}

	@Override
	public int getDeviceCount() {
		return deviceCount;
	}

	public static String[] getSerialNumbers() {
		return serialNumbers;
	}

	public static SIDType getSidType(int deviceId) {
		return hardSID.HardSID_GetSIDType((byte) deviceId);
	}

	public static int setSidType(int deviceId, SIDType sidType) {
		return hardSID.HardSID_SetSIDType((byte) deviceId, sidType);
	}

	@Override
	public Integer getDeviceId(int sidNum) {
		return sidNum < sids.size() ? Integer.valueOf(sids.get(sidNum).getDeviceId()) : null;
	}

	@Override
	public String getDeviceName(int sidNum) {
		return sidNum < sids.size() ? sids.get(sidNum).getDeviceName() : null;
	}

	public void setDeviceName(int sidNum, String serialNo) {
		if (sidNum < sids.size()) {
			sids.get(sidNum).setDeviceName(serialNo);
		}
	}

	@Override
	public ChipModel getDeviceChipModel(int sidNum) {
		return sidNum < sids.size() ? sids.get(sidNum).getChipModel() : null;
	}

	@Override
	public void setAudioDriver(AudioDriver audioDriver) {
	}

	@Override
	public void start() {
	}

	@Override
	public void fadeIn(double fadeIn) {
		System.err.println("Fade-in unsupported by SIDBlaster");
		// XXX unsupported by SIDBlaster
	}

	@Override
	public void fadeOut(double fadeOut) {
		System.err.println("Fade-out unsupported by SIDBlaster");
		// XXX unsupported by SIDBlaster
	}

	@Override
	public void setVolume(int sidNum, float volume) {
		System.err.println("Volume unsupported by SIDBlaster");
		// XXX unsupported by SIDBlaster
	}

	@Override
	public void setBalance(int sidNum, float balance) {
		System.err.println("Balance unsupported by SIDBlaster");
		// XXX unsupported by SIDBlaster
	}

	public int getDelay(int sidNum) {
		return delayInCycles[sidNum];
	}

	@Override
	public void setDelay(int sidNum, int delay) {
		delayInCycles[sidNum] = (int) (cpuClock.getCpuFrequency() / 1000. * delay);
	}

	@Override
	public void fastForward() {
		fastForwardFactor++;
	}

	@Override
	public void normalSpeed() {
		fastForwardFactor = 0;
	}

	@Override
	public boolean isFastForward() {
		return fastForwardFactor != 0;
	}

	@Override
	public int getFastForwardBitMask() {
		return (1 << fastForwardFactor) - 1;
	}

	@Override
	public void pause() {
		for (SIDBlasterEmu hSid : sids) {
			hardSID.HardSID_Flush(hSid.getDeviceId());
		}
	}

	/**
	 * Get SIDBlaster device index based on the desired chip model.
	 *
	 * @param chipModel              desired chip model
	 * @param sidNum                 current SID number
	 * @param sidBlasterSerialNumber hard-wired serial number of device to test with
	 *                               (null - choose best fitting device)
	 * @return SID index of the desired SIDBlaster device
	 */
	private SimpleEntry<Integer, ChipModel> getModelDependantDeviceId(final ChipModel chipModel, int sidNum,
			String sidBlasterSerialNumber) {
		if (sidBlasterSerialNumber == null) {
			// DEFAULT: choose best fitting device for sound output

			final Map<String, ChipModel> deviceMap = config.getEmulationSection().getSidBlasterDeviceMap();

			// use next free slot (prevent wrong type)
			for (int deviceId = 0; deviceId < deviceCount; deviceId++) {
				String serialNo = serialNumbers[deviceId];

				if (!isSerialNumAlreadyUsed(serialNo) && chipModel == deviceMap.get(serialNo)) {
					return new SimpleEntry<>(deviceId, chipModel);
				}
			}
			// Nothing matched? Use next free slot (no matter what type)
			for (int deviceId = 0; deviceId < deviceCount; deviceId++) {
				String serialNo = serialNumbers[deviceId];

				if (!isSerialNumAlreadyUsed(serialNo) && deviceMap.get(serialNo) != null) {
					return new SimpleEntry<>(deviceId, deviceMap.get(serialNo));
				}
			}
		} else {
			// TEST: Choose one specific device for sound output

			for (int deviceId = 0; deviceId < deviceCount; deviceId++) {
				if (Objects.equals(serialNumbers[deviceId], sidBlasterSerialNumber)) {
					return new SimpleEntry<Integer, ChipModel>(deviceId, chipModel);
				}
			}
		}
		// no slot left
		return new SimpleEntry<>(null, null);
	}

	private boolean isSerialNumAlreadyUsed(String serialNo) {
		return sids.stream().filter(sid -> Objects.equals(sid.getDeviceName(), serialNo)).findFirst().isPresent();
	}

	int clocksSinceLastAccess() {
		final long now = context.getTime(Event.Phase.PHI2);
		int diff = (int) (now - lastSIDWriteTime);
		lastSIDWriteTime = now;
		return diff >> fastForwardFactor;
	}

	long eventuallyDelay() {
		final long now = context.getTime(Event.Phase.PHI2);
		int diff = (int) (now - lastSIDWriteTime);
		if (diff > REGULAR_DELAY) {
			lastSIDWriteTime += REGULAR_DELAY;
			hardSID.HardSID_Delay((byte) 0, (short) (REGULAR_DELAY >> fastForwardFactor));
		}
		return REGULAR_DELAY;
	}
}
