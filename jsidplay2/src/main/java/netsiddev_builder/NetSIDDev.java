package netsiddev_builder;

import libsidplay.common.ChipModel;
import libsidplay.common.Event;
import libsidplay.common.EventScheduler;
import libsidplay.common.SIDEmu;
import libsidplay.config.IConfig;
import libsidplay.config.IEmulationSection;

public class NetSIDDev extends SIDEmu {

	private static final int DELAY_CYCLES = 250;
	
	private int sidNum;
	private NetSIDConnection connection;
	private NetSIDDevBuilder netSIDDevBuilder;

	private final Event event = new Event("NetSIDDev Delay") {

		@Override
		public void event() {
			int cycles = delay();
			if (cycles > 0)
				connection.delay(sidNum, cycles);

			context.schedule(event, DELAY_CYCLES, Event.Phase.PHI2);
		}
	};

	private int delay() {
		int cycles = Math.max(4, clocksSinceLastAccess() / netSIDDevBuilder.getSidCount());
		while (cycles > 0xFFFF) {
			connection.delay(sidNum, 0xFFFF);
			cycles -= 0xFFFF;
		}
		return cycles;
	}

	public NetSIDDev(EventScheduler context, NetSIDDevBuilder builder, NetSIDConnection connection, final int sidNum, final ChipModel model) {
		super(context);
		this.connection = connection;
		this.sidNum = sidNum;
		this.netSIDDevBuilder = builder;
	}

	@Override
	public void reset(byte volume) {
		connection.flush(sidNum);
		connection.reset(sidNum, (byte) volume);
	}

	@Override
	public byte read(int addr) {
		clock();
		delay();
		return (byte) 0xff;
	}

	@Override
	public void write(int addr, final byte data) {
		clock();
		super.write(addr, data);
		connection.addWrite(sidNum, delay(), (byte) addr, data);
	}

	@Override
	public void clock() {
	}

	@Override
	public void setChipModel(ChipModel model) {
	}

	@Override
	public void setClockFrequency(double cpuFrequency) {
		connection.setClockFrequency(sidNum, cpuFrequency);
	}

	@Override
	public void input(int input) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getInputDigiBoost() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVoiceMute(int num, boolean mute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFilter(IConfig config, int sidNum) {
	}

	@Override
	public void setFilterEnable(IEmulationSection emulation, int sidNum) {
	}

	public void lock() {
		context.schedule(event, 0);
		reset((byte) 0xf);
	}
	public void unlock() {
		context.cancel(event);
	}

}