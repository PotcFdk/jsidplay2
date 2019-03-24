package ui.entities.config;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Access(AccessType.PROPERTY)
public class Assembly64Column {
	private Integer id;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@XmlTransient
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Assembly64ColumnType columnType;

	public Assembly64ColumnType getColumnType() {
		return columnType;
	}

	public void setColumnType(Assembly64ColumnType columnType) {
		this.columnType = columnType;
	}

	private Double width;

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}
}