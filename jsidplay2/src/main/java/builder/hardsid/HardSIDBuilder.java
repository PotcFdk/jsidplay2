package builder.hardsid;

import static libsidplay.common.Engine.HARDSID;
import static libsidplay.components.pla.PLA.MAX_SIDS;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;

import libsidplay.common.CPUClock;
import libsidplay.common.ChipModel;
import libsidplay.common.Event;
import libsidplay.common.EventScheduler;
import libsidplay.common.HardwareSIDBuilder;
import libsidplay.common.Mixer;
import libsidplay.common.SIDEmu;
import libsidplay.config.IConfig;
import libsidplay.sidtune.SidTune;
import sidplay.audio.AudioDriver;

/**
 * 
 * @author Ken Händel
 * 
 */
public class HardSIDBuilder implements HardwareSIDBuilder, Mixer {

	private static final short REGULAR_DELAY = 128;

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
	 * Already used HardSIDs.
	 */
	private List<HardSIDEmu> sids = new ArrayList<HardSIDEmu>();

	/**
	 * Device number, if more than one USB devices is connected.
	 */
	private byte deviceID;

	protected long lastSIDWriteTime;

	private int fastForwardFactor;

	private int[] delayInCycles = new int[MAX_SIDS];

	public HardSIDBuilder(EventScheduler context, IConfig config, CPUClock cpuClock) {
		this.context = context;
		this.config = config;
		this.cpuClock = cpuClock;
		if (hardSID == null) {
			try {
				hardSID = Native.load("hardsid_usb", HardSID.class);
			} catch (UnsatisfiedLinkError e) {
				System.err.println("Error: 32-bit Java for Windows is required to use " + HARDSID + " soundcard!");
				throw e;
			}
		}
	}

	@Override
	public SIDEmu lock(SIDEmu oldHardSID, int sidNum, SidTune tune) {
		ChipModel chipModel = ChipModel.getChipModel(config.getEmulationSection(), tune, sidNum);
		Integer chipNum = getModelDependantChipNum(chipModel, sidNum);
		if (deviceID < hardSID.HardSID_Devices() && chipNum != null && chipNum < hardSID.HardSID_SIDCount(deviceID)) {
			if (oldHardSID != null) {
				// always re-use hardware SID chips, if configuration changes
				// the purpose is to ignore chip model changes!
				return oldHardSID;
			}
			HardSIDEmu hsid = createSID(deviceID, chipNum, sidNum, tune, chipModel);
			hsid.lock();
			sids.add(hsid);
			setDelay(sidNum, config.getAudioSection().getDelay(sidNum));
			return hsid;
		}
		System.err.println(/* throw new RuntimeException( */
				String.format(
						"HARDSID ERROR: System doesn't have enough SID chips. Requested: (DeviceID=%d, sidNum=%d)",
						deviceID, sidNum));
		if (SidTune.isFakeStereoSid(config.getEmulationSection(), tune, sidNum)) {
			// Fake stereo chip not available? Re-use original chip
			return oldHardSID;
		}
		return SIDEmu.NONE;
	}

	private HardSIDEmu createSID(byte deviceId, int chipNum, int sidNum, SidTune tune, ChipModel chipModel) {
		if (SidTune.isFakeStereoSid(config.getEmulationSection(), tune, sidNum)) {
			return new HardSIDEmu.FakeStereo(context, config, this, hardSID, deviceId, chipNum, sidNum, chipModel,
					sids);
		} else {
			return new HardSIDEmu(context, this, hardSID, deviceId, chipNum, sidNum, chipModel);
		}
	}

	@Override
	public void unlock(final SIDEmu sidEmu) {
		HardSIDEmu hardSid = (HardSIDEmu) sidEmu;
		hardSid.unlock();
		sids.remove(sidEmu);
	}

	@Override
	public int getDeviceCount() {
		return hardSID != null ? hardSID.HardSID_SIDCount(deviceID) : null;
	}

	@Override
	public Integer getDeviceId(int sidNum) {
		return sidNum < sids.size() ? Integer.valueOf(sids.get(sidNum).getChipNum()) : null;
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
		System.err.println("Fade-in unsupported by HardSID4U");
		// XXX unsupported by HardSID4U
	}

	@Override
	public void fadeOut(double fadeOut) {
		System.err.println("Fade-out unsupported by HardSID4U");
		// XXX unsupported by HardSID4U
	}

	@Override
	public void setVolume(int sidNum, float volume) {
		System.err.println("Volume unsupported by HardSID4U");
		// XXX unsupported by HardSID4U
	}

	@Override
	public void setBalance(int sidNum, float balance) {
		System.err.println("Balance unsupported by HardSID4U");
		// XXX unsupported by HardSID4U
	}

	public int getDelayInCycles(int sidNum) {
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
		hardSID.HardSID_Flush(deviceID);
	}

	/**
	 * Get HardSID device index based on the desired chip model.
	 * 
	 * @param chipModel desired chip model
	 * @param sidNum    current SID number
	 * @return SID index of the desired HardSID device
	 */
	private Integer getModelDependantChipNum(final ChipModel chipModel, int sidNum) {
		int sid6581 = config.getEmulationSection().getHardsid6581();
		int sid8580 = config.getEmulationSection().getHardsid8580();
		if (sidNum == 0) {
			if (0 < hardSID.HardSID_SIDCount(deviceID)) {
				// Mono SID: choose according to the chip model type
				return chipModel == ChipModel.MOS6581 ? sid6581 : sid8580;
			}
		} else {
			// Stereo or 3-SID: use next free slot (prevent already used one and wrong type)
			for (int hardSidIdx = 0; hardSidIdx < hardSID.HardSID_SIDCount(deviceID); hardSidIdx++) {
				final int theHardSIDIdx = hardSidIdx;
				if (sids.stream().filter(sid -> theHardSIDIdx == sid.getChipNum()).findFirst().isPresent()
						|| hardSidIdx == sid6581 || hardSidIdx == sid8580) {
					continue;
				}
				return hardSidIdx;
			}
		}
		// no slot left
		return null;
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
			hardSID.HardSID_Delay(deviceID, (short) (REGULAR_DELAY >> fastForwardFactor));
		}
		return REGULAR_DELAY;
	}

}
