package com.jersey.demo.dataObjects;

/*Author: ABGANDH
 *Description: This class is used to represent Statistics the user requests
 */
public class Statistic {
	public Statistic() {
		
	}
	
	public Statistic(String metric, String stat) {
		this.metric=metric;
		this.stat=stat;
	}
	
	private String metric;
	private String stat;
	private Float value;
	public String getMetric() {
		return metric;
	}
	public void setMetric(String metric) {
		this.metric = metric;
	}
	public String getStat() {
		return stat;
	}
	public void setStat(String stat) {
		this.stat = stat;
	}
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	
}
