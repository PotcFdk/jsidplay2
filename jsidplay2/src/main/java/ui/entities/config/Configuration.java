package ui.entities.config;

import static java.util.stream.Collectors.toList;
import static sidplay.ini.IniDefaults.DEFAULTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.beust.jcommander.ParametersDelegate;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import libsidplay.components.keyboard.KeyTableEntry;
import libsidplay.config.IConfig;
import sidplay.ini.converter.BeanToStringConverter;
import ui.common.properties.LazyListField;
import ui.common.properties.ObservableLazyListField;
import ui.common.properties.ShadowField;
import ui.console.Console;
import ui.videoscreen.Video;

/**
 * 
 * Configuration of the UI version of JSIDPlay2.
 * 
 * @author ken
 *
 */
@Entity
@Access(AccessType.PROPERTY)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Configuration implements IConfig {

	public static final List<FilterSection> DEFAULT_FILTERS = DEFAULTS.getFilterSection().stream()
			.map(FilterSection::new).collect(toList());

	public static final List<ViewEntity> DEFAULT_VIEWS = Arrays.asList(new ViewEntity(Console.ID),
			new ViewEntity(Video.ID));

	public static final List<FavoritesSection> DEFAULT_FAVORITES = Arrays.asList(new FavoritesSection());

	public static final List<KeyTableEntity> DEFAULT_KEYCODES = Arrays.asList(
			new KeyTableEntity(KeyCode.A.getName(), KeyTableEntry.A),
			new KeyTableEntity(KeyCode.BACK_SLASH.getName(), KeyTableEntry.ARROW_LEFT),
			new KeyTableEntity(KeyCode.DIGIT1.getName(), KeyTableEntry.ONE),
			new KeyTableEntity(KeyCode.DIGIT2.getName(), KeyTableEntry.TWO),
			new KeyTableEntity(KeyCode.DIGIT3.getName(), KeyTableEntry.THREE),
			new KeyTableEntity(KeyCode.DIGIT4.getName(), KeyTableEntry.FOUR),
			new KeyTableEntity(KeyCode.DIGIT5.getName(), KeyTableEntry.FIVE),
			new KeyTableEntity(KeyCode.DIGIT6.getName(), KeyTableEntry.SIX),
			new KeyTableEntity(KeyCode.DIGIT7.getName(), KeyTableEntry.SEVEN),
			new KeyTableEntity(KeyCode.DIGIT8.getName(), KeyTableEntry.EIGHT),
			new KeyTableEntity(KeyCode.DIGIT9.getName(), KeyTableEntry.NINE),
			new KeyTableEntity(KeyCode.DIGIT0.getName(), KeyTableEntry.ZERO),
			new KeyTableEntity(KeyCode.OPEN_BRACKET.getName(), KeyTableEntry.PLUS),
			new KeyTableEntity(KeyCode.CLOSE_BRACKET.getName(), KeyTableEntry.MINUS),
			new KeyTableEntity(KeyCode.POUND.getName(), KeyTableEntry.POUND),
			new KeyTableEntity(KeyCode.HOME.getName(), KeyTableEntry.CLEAR_HOME),
			new KeyTableEntity(KeyCode.BACK_SPACE.getName(), KeyTableEntry.INS_DEL),

			new KeyTableEntity(KeyCode.Q.getName(), KeyTableEntry.Q),
			new KeyTableEntity(KeyCode.W.getName(), KeyTableEntry.W),
			new KeyTableEntity(KeyCode.E.getName(), KeyTableEntry.E),
			new KeyTableEntity(KeyCode.R.getName(), KeyTableEntry.R),
			new KeyTableEntity(KeyCode.T.getName(), KeyTableEntry.T),
			new KeyTableEntity(KeyCode.Y.getName(), KeyTableEntry.Y),
			new KeyTableEntity(KeyCode.U.getName(), KeyTableEntry.U),
			new KeyTableEntity(KeyCode.I.getName(), KeyTableEntry.I),
			new KeyTableEntity(KeyCode.O.getName(), KeyTableEntry.O),
			new KeyTableEntity(KeyCode.P.getName(), KeyTableEntry.P),
			new KeyTableEntity(KeyCode.SEMICOLON.getName(), KeyTableEntry.AT),
			new KeyTableEntity(KeyCode.PLUS.getName(), KeyTableEntry.STAR),
			new KeyTableEntity(KeyCode.LESS.getName(), KeyTableEntry.ARROW_UP),

			new KeyTableEntity(KeyCode.ESCAPE.getName(), KeyTableEntry.RUN_STOP),
			new KeyTableEntity(KeyCode.A.getName(), KeyTableEntry.A),
			new KeyTableEntity(KeyCode.S.getName(), KeyTableEntry.S),
			new KeyTableEntity(KeyCode.D.getName(), KeyTableEntry.D),
			new KeyTableEntity(KeyCode.F.getName(), KeyTableEntry.F),
			new KeyTableEntity(KeyCode.G.getName(), KeyTableEntry.G),
			new KeyTableEntity(KeyCode.H.getName(), KeyTableEntry.H),
			new KeyTableEntity(KeyCode.J.getName(), KeyTableEntry.J),
			new KeyTableEntity(KeyCode.K.getName(), KeyTableEntry.K),
			new KeyTableEntity(KeyCode.L.getName(), KeyTableEntry.L),
			new KeyTableEntity(KeyCode.BACK_QUOTE.getName(), KeyTableEntry.COLON),
			new KeyTableEntity(KeyCode.QUOTE.getName(), KeyTableEntry.SEMICOLON),
			new KeyTableEntity(KeyCode.SLASH.getName(), KeyTableEntry.EQUALS),
			new KeyTableEntity(KeyCode.ENTER.getName(), KeyTableEntry.RETURN),

			new KeyTableEntity(KeyCode.Z.getName(), KeyTableEntry.Z),
			new KeyTableEntity(KeyCode.X.getName(), KeyTableEntry.X),
			new KeyTableEntity(KeyCode.C.getName(), KeyTableEntry.C),
			new KeyTableEntity(KeyCode.V.getName(), KeyTableEntry.V),
			new KeyTableEntity(KeyCode.B.getName(), KeyTableEntry.B),
			new KeyTableEntity(KeyCode.N.getName(), KeyTableEntry.N),
			new KeyTableEntity(KeyCode.M.getName(), KeyTableEntry.M),
			new KeyTableEntity(KeyCode.COMMA.getName(), KeyTableEntry.COMMA),
			new KeyTableEntity(KeyCode.PERIOD.getName(), KeyTableEntry.PERIOD),
			new KeyTableEntity(KeyCode.MINUS.getName(), KeyTableEntry.SLASH),
			new KeyTableEntity(KeyCode.DOWN.getName(), KeyTableEntry.CURSOR_UP_DOWN),
			new KeyTableEntity(KeyCode.UP.getName(), KeyTableEntry.CURSOR_UP_DOWN),
			new KeyTableEntity(KeyCode.RIGHT.getName(), KeyTableEntry.CURSOR_LEFT_RIGHT),
			new KeyTableEntity(KeyCode.LEFT.getName(), KeyTableEntry.CURSOR_LEFT_RIGHT),

			new KeyTableEntity(KeyCode.SPACE.getName(), KeyTableEntry.SPACE),

			new KeyTableEntity(KeyCode.F1.getName(), KeyTableEntry.F1),
			new KeyTableEntity(KeyCode.F3.getName(), KeyTableEntry.F3),
			new KeyTableEntity(KeyCode.F5.getName(), KeyTableEntry.F5),
			new KeyTableEntity(KeyCode.F7.getName(), KeyTableEntry.F7));

	private Integer id;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@XmlTransient
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private SidPlay2Section sidplay2 = new SidPlay2Section();

	@Embedded
	@Override
	public SidPlay2Section getSidplay2Section() {
		return sidplay2;
	}

	public void setSidplay2Section(SidPlay2Section sidplay2) {
		this.sidplay2 = sidplay2;
	}

	private OnlineSection online = new OnlineSection();

	@Embedded
	public OnlineSection getOnlineSection() {
		return online;
	}

	public void setOnlineSection(OnlineSection online) {
		this.online = online;
	}

	private C1541Section c1541 = new C1541Section();

	@Override
	@Embedded
	public C1541Section getC1541Section() {
		return c1541;
	}

	public void setC1541Section(C1541Section c1541) {
		this.c1541 = c1541;
	}

	private PrinterSection printer = new PrinterSection();

	@Embedded
	@Override
	public PrinterSection getPrinterSection() {
		return printer;
	}

	public void setPrinterSection(PrinterSection printer) {
		this.printer = printer;
	}

	private JoystickSection joystickSection = new JoystickSection();

	@Embedded
	public JoystickSection getJoystickSection() {
		return joystickSection;
	}

	public void setJoystickSection(JoystickSection joystick) {
		this.joystickSection = joystick;
	}

	private AudioSection audioSection = new AudioSection();

	@Embedded
	@Override
	public AudioSection getAudioSection() {
		return audioSection;
	}

	public void setAudioSection(AudioSection audio) {
		this.audioSection = audio;
	}

	@ParametersDelegate
	private EmulationSection emulationSection = new EmulationSection();

	@Embedded
	@Override
	public EmulationSection getEmulationSection() {
		return emulationSection;
	}

	public void setEmulationSection(EmulationSection emulation) {
		this.emulationSection = emulation;
	}

	@ParametersDelegate
	private WhatsSidSection whatsSidSection = new WhatsSidSection();

	@Embedded
	@Override
	public WhatsSidSection getWhatsSidSection() {
		return whatsSidSection;
	}

	public void setWhatsSidSection(WhatsSidSection whatsSidSection) {
		this.whatsSidSection = whatsSidSection;
	}

	private ShadowField<StringProperty, String> currentFavorite = new ShadowField<>(SimpleStringProperty::new, null);

	public String getCurrentFavorite() {
		return currentFavorite.get();
	}

	public void setCurrentFavorite(String currentFavorite) {
		this.currentFavorite.set(currentFavorite);
	}

	public StringProperty currentFavoriteProperty() {
		return currentFavorite.property();
	}

	private ObservableLazyListField<FavoritesSection> favorites = new ObservableLazyListField<>();

	@OneToMany(cascade = CascadeType.ALL)
	public List<FavoritesSection> getFavorites() {
		return favorites.get(() -> new ArrayList<>(DEFAULT_FAVORITES));
	}

	public void setFavorites(List<FavoritesSection> favorites) {
		this.favorites.set(favorites);
	}

	@Transient
	public ObservableList<FavoritesSection> getObservableFavorites() {
		return favorites.getObservableList();
	}

	private Assembly64Section assembly64Section = new Assembly64Section();

	@Embedded
	public Assembly64Section getAssembly64Section() {
		return assembly64Section;
	}

	public void setAssembly64Section(Assembly64Section assembly64Section) {
		this.assembly64Section = assembly64Section;
	}

	private ObservableLazyListField<ViewEntity> views = new ObservableLazyListField<>();

	@OneToMany(cascade = CascadeType.ALL)
	public List<ViewEntity> getViews() {
		return views.get(() -> new ArrayList<>(DEFAULT_VIEWS));
	}

	public void setViews(List<ViewEntity> views) {
		this.views.set(views);
	}

	@Transient
	public ObservableList<ViewEntity> getObservableViews() {
		return views.getObservableList();
	}

	private LazyListField<FilterSection> filter = new LazyListField<>();

	@OneToMany(cascade = CascadeType.ALL)
	@Override
	public List<FilterSection> getFilterSection() {
		return filter.get(() -> DEFAULT_FILTERS.stream().map(FilterSection::new).collect(toList()));
	}

	public void setFilterSection(List<FilterSection> filter) {
		this.filter.set(filter);
	}

	private LazyListField<KeyTableEntity> keyCodeMap = new LazyListField<>();

	@OneToMany(cascade = CascadeType.ALL)
	public List<KeyTableEntity> getKeyCodeMap() {
		return keyCodeMap.get(() -> DEFAULT_KEYCODES.stream().map(KeyTableEntity::new).collect(toList()));
	}

	public void setKeyCodeMap(List<KeyTableEntity> keyCodeMap) {
		this.keyCodeMap.set(keyCodeMap);
	}

	@Override
	public String toString() {
		return BeanToStringConverter.toString(this);
	}
}
