package sidplay;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsidplay.Player;
import libsidplay.common.CPUClock;
import libsidplay.player.DriverSettings;
import libsidplay.player.Emulation;
import libsidplay.sidtune.SidTune;
import libsidplay.sidtune.SidTuneError;
import libsidutils.PathUtils;
import libsidutils.SidDatabase;
import resid_builder.resid.ChipModel;
import sidplay.audio.Audio;
import sidplay.consoleplayer.ConsoleIO;
import sidplay.ini.IniConfig;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(resourceBundle = "sidplay.consoleplayer.ConsolePlayer")
public class ConsolePlayer {
	/**
	 * Parse [mm:]ss (parse time in minutes and seconds and store as seconds)
	 */
	public static class TimeConverter implements IStringConverter<Integer> {
		@Override
		public Integer convert(String value) {
			String[] s = value.split(":");
			if (s.length == 1) {
				return Integer.parseInt(s[0]);
			} else if (s.length == 2) {
				return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
			}
			throw new ParameterException(
					"Invalid time, expected [mm:]ss (found " + value + ")");
		}
	}

	public static class VerboseValidator implements IParameterValidator {
		public void validate(String name, String value)
				throws ParameterException {
			int n = Integer.parseInt(value);
			if (n < 0 || n > 2) {
				throw new ParameterException("Invalid " + name
						+ " value, expected 0, 1 or 2 (found " + value + ")");
			}
		}
	}

	@Parameter(names = { "--help", "-h" }, descriptionKey = "USAGE", help = true)
	private Boolean help = Boolean.FALSE;

	@Parameter(names = "--cpuDebug", hidden = true, descriptionKey = "DEBUG")
	private Boolean cpuDebug = Boolean.FALSE;

	@Parameter(names = "-audio", descriptionKey = "DRIVER")
	private Audio audio = Audio.SOUNDCARD;

	@Parameter(names = "-emulation", descriptionKey = "EMULATION")
	private Emulation emulation = Emulation.RESID;

	@Parameter(names = "-recordingFilename", descriptionKey = "RECORDING_FILENAME")
	private String recordingFilename = "jsidplay2";

	@Parameter(names = "-startSong", descriptionKey = "START_SONG")
	private Integer song = null;

	@Parameter(names = "-loop", descriptionKey = "LOOP")
	private Boolean loop = Boolean.FALSE;

	@Parameter(names = "-single", descriptionKey = "SINGLE")
	private Boolean single = Boolean.FALSE;

	@Parameter(names = "-frequency", descriptionKey = "FREQUENCY")
	private Integer frequency = 48000;

	@Parameter(names = "-dualSID", descriptionKey = "DUAL_SID")
	private Boolean dualSID = Boolean.FALSE;

	@Parameter(names = "-forceClock", descriptionKey = "FORCE_CLOCK")
	private CPUClock forceClock = null;

	@Parameter(names = "-defaultClock", descriptionKey = "DEFAULT_CLOCK")
	private CPUClock defaultClock = CPUClock.PAL;

	@Parameter(names = "-disableFilter", descriptionKey = "DISABLE_FILTER")
	private Boolean disableFilter = Boolean.FALSE;

	@Parameter(names = "-forceModel", descriptionKey = "FORCE_MODEL")
	private ChipModel forceModel = null;

	@Parameter(names = "-defaultModel", descriptionKey = "DEFAULT_MODEL")
	private ChipModel defaultModel = ChipModel.MOS6581;

	@Parameter(names = "-startTime", descriptionKey = "START_TIME", converter = TimeConverter.class)
	private Integer startTime = 0;

	@Parameter(names = "-defaultLength", descriptionKey = "DEFAULT_LENGTH", converter = TimeConverter.class)
	private Integer defaultLength = 0;

	@Parameter(names = "-enableSidDatabase", descriptionKey = "ENABLE_SID_DATABASE", arity = 1)
	private Boolean enableSidDatabase = Boolean.TRUE;

	@Parameter(names = "-verbose", descriptionKey = "VERBOSE", validateWith = VerboseValidator.class)
	private Integer verbose = 0;

	@Parameter(names = "-quiet", descriptionKey = "QUIET")
	private Boolean quiet = Boolean.FALSE;

	@Parameter(description = "filename")
	private List<String> filenames = new ArrayList<String>();

	private ConsolePlayer(String[] args) {
		try {
			JCommander commander = new JCommander(this, args);
			commander.setProgramName(getClass().getName());
			commander.setCaseSensitiveOptions(true);
			if (help || filenames.size() != 1) {
				commander.usage();
				exit(help ? 0 : 1);
			}
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			exit(1);
		}
		// Cannot loop while recording audio files
		if (isRecording()) {
			loop = false;
		}
		final IniConfig config = new IniConfig(true);
		config.getSidplay2().setLoop(loop);
		config.getSidplay2().setSingle(single);
		config.getSidplay2().setDefaultPlayLength(defaultLength);
		config.getSidplay2().setEnableDatabase(enableSidDatabase);
		config.getAudio().setFrequency(frequency);
		config.getEmulation().setForceStereoTune(dualSID);
		config.getEmulation().setUserClockSpeed(forceClock);
		config.getEmulation().setDefaultClockSpeed(defaultClock);
		config.getEmulation().setUserSidModel(forceModel);
		config.getEmulation().setDefaultSidModel(defaultModel);
		config.getEmulation().setFilter(!disableFilter);

		final Player player = new Player(config);
		try {
			final SidTune tune = SidTune.load(new File(filenames.get(0)));
			player.setTune(tune);
			tune.setSelectedSong(song);
			player.setRecordingFilenameProvider(() -> {
				File file = new File(recordingFilename);
				String filename = new File(file.getParentFile(), PathUtils
						.getBaseNameNoExt(file.getName())).getAbsolutePath();
				if (tune.getInfo().getSongs() > 1) {
					filename += String.format("-%02d", tune.getInfo()
							.getCurrentSong());
				}
				return filename;
			});
			player.setDebug(cpuDebug);
			player.setDriverSettings(new DriverSettings(audio, emulation));
			player.getTimer().setStart(startTime);

			// check song length
			if (defaultLength == 0) {
				setSIDDatabase(player);
				int length = player.getSidDatabaseInfo(db -> db.getSongLength(tune));
				if (isRecording()
						&& (!config.getSidplay2().isEnableDatabase() || length == 0)) {
					System.err
							.println("ERROR: unknown song length in record mode"
									+ " (please use option -defaultLength or configure song length database)");
					exit(1);
				}
			}
			ConsoleIO consoleIO = new ConsoleIO(config, filenames.get(0));
			player.setMenuHook(obj -> consoleIO.menu(obj, verbose, quiet,
					System.out));
			player.setInteractivityHook(obj -> consoleIO.decodeKeys(obj));

			player.startC64();
		} catch (IOException | SidTuneError e) {
			System.err.println(e.getMessage());
			exit(1);
		}
	}

	private void setSIDDatabase(final Player player) {
		String hvscRoot = player.getConfig().getSidplay2().getHvsc();
		if (hvscRoot != null) {
			File file = new File(hvscRoot, SidDatabase.SONGLENGTHS_FILE);
			try (FileInputStream input = new FileInputStream(file)) {
				player.setSidDatabase(new SidDatabase(input));
			} catch (IOException e) {
				// silently ignored!
			}
		}
	}

	private boolean isRecording() {
		return audio == Audio.WAV || audio == Audio.MP3
				|| audio == Audio.LIVE_WAV || audio == Audio.LIVE_MP3;
	}

	private void exit(int rc) {
		try {
			if (rc == 0) {
				System.out.println("Press <enter> to exit the player!");
				System.in.read();
			}
			System.exit(rc);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static void main(final String[] args) {
		new ConsolePlayer(args);
	}

}
