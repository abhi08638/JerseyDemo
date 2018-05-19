package com.jersey.demo.dataObjects;

import java.time.LocalDateTime;

/*Author: ABGANDH
 *Description: This class is used to represent measurements in the garden
 *			   When a new instrument is installed, then we simply need to
 *					add a field for that measurement in this class.
 */
public class Measurement {	
	private LocalDateTime timestamp;
	private Float temperature;
	private Float dewPoint;
	private Float precipitation;
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public Float getTemperature() {
		return temperature;
	}
	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}
	public Float getDewPoint() {
		return dewPoint;
	}
	public void setDewPoint(Float dewPoint) {
		this.dewPoint = dewPoint;
	}
	public Float getPrecipitation() {
		return precipitation;
	}
	public void setPrecipitation(Float precipitation) {
		this.precipitation = precipitation;
	}

}
