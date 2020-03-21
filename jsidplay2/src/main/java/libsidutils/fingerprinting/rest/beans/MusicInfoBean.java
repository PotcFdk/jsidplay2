package libsidutils.fingerprinting.rest.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "musicInfo")
@XmlType(propOrder = { "title", "artist", "album", "fileDir", "infoDir", "audioLength" })
public class MusicInfoBean {

	private String title, artist, album, fileDir, infoDir;
	private double audioLength;

	public String getTitle() {
		return title;
	}

	@XmlElement(name = "title")
	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	@XmlElement(name = "artist")
	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	@XmlElement(name = "album")
	public void setAlbum(String album) {
		this.album = album;
	}

	public String getFileDir() {
		return fileDir;
	}

	@XmlElement(name = "fileDir")
	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getInfoDir() {
		return infoDir;
	}

	@XmlElement(name = "infoDir")
	public void setInfoDir(String infoDir) {
		this.infoDir = infoDir;
	}

	public double getAudioLength() {
		return audioLength;
	}

	@XmlElement(name = "audioLength")
	public void setAudioLength(double audioLength) {
		this.audioLength = audioLength;
	}

	@Override
	public String toString() {
		return "title=" + title + ", artist=" + artist + ", album=" + album + ", audioLength=" + audioLength;
	}

}