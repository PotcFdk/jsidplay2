package libsidplay;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Locale;

import libsidplay.config.IConfig;
import libsidplay.sidtune.SidTune;

/**
 * Makes use of the Ultimate64 (FPGA-C64) debug interface to use JSIDPlay2 and
 * Ultimate64 simultaneously.
 * 
 * https://github.com/GideonZ/1541ultimate/blob/master/software/network/socket_dma.cc
 * 
 * https://github.com/GideonZ/1541ultimate/blob/master/python/sock.py
 * 
 * @author ken
 *
 */
public interface Ultimate64 {

	public enum StreamingType {
		VIC(0x20, 0xFF, 0x30, 0xFF), SID(0x21, 0xFF, 0x31, 0xFF);

		public int onCmdLow, onCmdHigh, offCmdLow, offCmdHigh;

		private StreamingType(int onCmdLow, int onCmdHigh, int offCmdLow, int offCmdHigh) {
			this.onCmdLow = onCmdLow;
			this.onCmdHigh = onCmdHigh;
			this.offCmdLow = offCmdLow;
			this.offCmdHigh = offCmdHigh;
		}
	}

	/**
	 * Ultimate64 socket connection timeout.
	 */
	int SOCKET_CONNECT_TIMEOUT = 5000;

	/**
	 * Maximum length for a user typed-in command.
	 */
	int MAX_COMMAND_LEN = 16;

	Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * Send RAM to Ultimate64 C64-RAM and type Run.<BR>
	 * 
	 * <B>Note:</B> whole RAM is currently transfered, no matter how long the
	 * program is.
	 * 
	 * @param config configuration
	 * @param tune   tune to play
	 * @param c64Ram C64 emulator RAM
	 * @throws InterruptedException
	 */
	default void sendRamAndRun(IConfig config, SidTune tune, byte[] c64Ram) throws InterruptedException {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		int syncDelay = config.getEmulationSection().getUltimate64SyncDelay();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			int ramStart = 0x0800;
			int ramEnd = 0x10000;
			byte[] ram = new byte[ramEnd - ramStart + 6];
			ram[0] = (byte) 0x02;
			ram[1] = (byte) 0xff;
			ram[2] = (byte) (ram.length & 0xff);
			ram[3] = (byte) ((ram.length >> 8) & 0xff);
			ram[4] = (byte) (ramStart & 0xff);
			ram[5] = (byte) ((ramStart >> 8) & 0xff);
			System.arraycopy(c64Ram, ramStart, ram, 6, ramEnd - ramStart);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot send RAM and RUN: " + e.getMessage());
		}
		Thread.sleep(syncDelay);
	}

	/**
	 * Send RAM to Ultimate64 C64-RAM and start machine code.<BR>
	 * 
	 * <B>Note:</B> whole RAM is currently transfered, no matter how long the
	 * program is.
	 * 
	 * @param config    configuration
	 * @param tune      tune to play
	 * @param c64Ram    C64 emulator RAM
	 * @param startAddr start address of machine code to execute
	 * @throws InterruptedException
	 */
	default void sendRamAndSys(IConfig config, SidTune tune, byte[] c64Ram, int startAddr) throws InterruptedException {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		int syncDelay = config.getEmulationSection().getUltimate64SyncDelay();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			int ramStart = 0x0400;
			int ramEnd = 0x10000;
			byte[] ram = new byte[ramEnd - ramStart + 8];
			ram[0] = (byte) 0x09;
			ram[1] = (byte) 0xff;
			ram[2] = (byte) (ram.length & 0xff);
			ram[3] = (byte) ((ram.length >> 8) & 0xff);
			ram[4] = (byte) (startAddr & 0xff);
			ram[5] = (byte) ((startAddr >> 8) & 0xff);
			ram[6] = (byte) (ramStart & 0xff);
			ram[7] = (byte) ((ramStart >> 8) & 0xff);
			System.arraycopy(c64Ram, ramStart, ram, 8, ramEnd - ramStart);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot send RAM and SYS: " + e.getMessage());
		}
		Thread.sleep(syncDelay);
	}

	/**
	 * Send Reset to Ultimate64.<BR>
	 * 
	 * @param config configuration
	 * @param tune   tune to play
	 */
	default void sendReset(IConfig config, SidTune tune) {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			int len = 1;
			byte[] ram = new byte[len + 4];
			ram[0] = (byte) 0x04;
			ram[1] = (byte) 0xff;
			ram[2] = (byte) (len & 0xff);
			ram[3] = (byte) ((len >> 8) & 0xff);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot send RESET: " + e.getMessage());
		}
	}

	/**
	 * Send a keyboard command, as if a user pressed these keys.<BR>
	 * 
	 * <B>Note:</B> command length is limited to max. 16 characters. You can
	 * simulate a return press by sending carriage return (e.g.
	 * "LOAD\"*\",8,1\rRUN\r"). Sending special characters or pressing additional
	 * keys like shift is not supported.
	 * 
	 * @param config  configuration
	 * @param command command to send
	 */
	default void sendCommand(IConfig config, String command) {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		final int length = Math.min(command.length(), MAX_COMMAND_LEN);
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			byte[] ram = new byte[length + 4];
			ram[0] = (byte) 0x03;
			ram[1] = (byte) 0xff;
			ram[2] = (byte) (length);
			ram[3] = (byte) (0);
			System.arraycopy(command.toUpperCase(Locale.US).getBytes(US_ASCII), 0, ram, 4, length);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot send COMMAND: " + e.getMessage());
		}
	}

	/**
	 * Wait on the Ultimate64 server side
	 * 
	 * @param config configuration
	 * @param delay  delay to wait (600 ~ 3 seconds)
	 */
	default void sendWait(IConfig config, int delay) {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			byte[] ram = new byte[6];
			ram[0] = (byte) 0x05;
			ram[1] = (byte) 0xff;
			ram[2] = (byte) (delay & 0xff);
			ram[3] = (byte) ((delay >> 8) & 0xff);
			ram[4] = (byte) (delay & 0xff);
			ram[5] = (byte) ((delay >> 8) & 0xff);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot send WAIT: " + e.getMessage());
		}
	}

	/**
	 * Insert D64 image into Ultimate64 floppy disk drive.
	 * 
	 * @param config configuration
	 * @param file   d64 file
	 * @throws IOException I/O error
	 */
	default void sendInsertDisk(IConfig config, final File file) throws IOException {
		byte diskContents[] = Files.readAllBytes(file.toPath());
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			byte[] ram = new byte[diskContents.length + 5];
			ram[0] = (byte) 0x0a;
			ram[1] = (byte) 0xff;
			ram[2] = (byte) (diskContents.length & 0xff);
			ram[3] = (byte) ((diskContents.length >> 8) & 0xff);
			ram[4] = (byte) ((diskContents.length >> 16) & 0xff);
			System.arraycopy(diskContents, 0, ram, 5, diskContents.length);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot insert disk: " + e.getMessage());
		}
	}

	/**
	 * Start streaming.
	 * 
	 * <pre>
	 * 192.168.0.119:11000 unicast address on the local network, port number 11000
	 * myserver.com unicast address, using DNS and default port number
	 * myserver.com:4567 unicast address, using DNS and specific port number
	 * 239.0.1.64 multicast address, using default port number
	 * 239.0.2.77:64738 multicast address with port number specified
	 * </pre>
	 * 
	 * @param config        configuration
	 * @param streamingType VIC/SID
	 * @param target        network target to receive the stream
	 * @param streamNumber  0-15
	 * @param duration      duration in ticks (one tick is 5ms, 0=forever)
	 */
	default void startStreaming(IConfig config, StreamingType streamingType, String target, int streamNumber,
			int duration) {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			byte[] ram = new byte[target.length() + 6];
			ram[0] = (byte) streamingType.onCmdLow;
			ram[1] = (byte) streamingType.onCmdHigh;
			ram[2] = (byte) ((target.length() + 2) & 0xff);
			ram[3] = (byte) (((target.length() + 2) >> 8) & 0xff);
			ram[4] = (byte) (streamNumber);
			ram[5] = (byte) (duration);
			System.arraycopy(target.getBytes(US_ASCII), 0, ram, 6, target.length());
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot start streaming: " + e.getMessage());
		}
	}

	/**
	 * Stop streaming.
	 * 
	 * @param config        configuration
	 * @param streamingType VIC/SID
	 * @param streamNumber  0-15
	 */
	default void stopStreaming(IConfig config, StreamingType streamingType, int streamNumber) {
		String hostname = config.getEmulationSection().getUltimate64Host();
		int port = config.getEmulationSection().getUltimate64Port();
		try (Socket connectedSocket = new Socket()) {
			connectedSocket.connect(new InetSocketAddress(hostname, port), SOCKET_CONNECT_TIMEOUT);
			byte[] ram = new byte[5];
			ram[0] = (byte) streamingType.offCmdLow;
			ram[1] = (byte) streamingType.offCmdHigh;
			ram[2] = (byte) (0);
			ram[3] = (byte) (0);
			ram[4] = (byte) (streamNumber);
			connectedSocket.getOutputStream().write(ram);
		} catch (IOException e) {
			System.err.println("Ultimate64: cannot stop streaming: " + e.getMessage());
		}
	}
}
