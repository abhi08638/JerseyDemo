package com.jersey.demo.utils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.beanutils.BeanUtilsBean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.jersey.demo.dataObjects.Measurement;
import com.jersey.demo.dataObjects.Statistic;
import com.jersey.demo.exceptions.NotFoundException;
import com.jersey.demo.exceptions.TimestampMismatchException;
import com.jersey.demo.exceptions.ValidationException;

public class Utils {
	
	/*Author: ABGANDH
     *Description: Used to store the data sent to the server for the lifetime of the app
     */
    private static ConcurrentSkipListMap<String, Measurement> recordedMap=new ConcurrentSkipListMap<String, Measurement>();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static JavaTimeModule module = new JavaTimeModule();    
    private static ObjectMapper objectMapper= new ObjectMapper();
    
    public static void configure(){    	
    	module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
    	module.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
    	objectMapper.registerModule(module);			
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }
    
    /**Start Controller operation methods**/
    public static String recordMeasurement(JsonNode json) throws Exception {
		Measurement measurement = toObj(json);
		if(!isEmpty(measurement)) {
			addToRecordedMap(measurement);
		}
		
		return getTimestampString(measurement.getTimestamp());
	}	
    
    public static void replaceMeasurement(String timestamp,JsonNode json) throws Exception {
    	Measurement measurement = toObj(json);
    	if(!isEmpty(measurement)) {
			replaceItem(timestamp,measurement);
		}
	}
    
    public static void updateMeasurement(String timestamp,JsonNode json) throws Exception {
    	Measurement measurement = toObj(json);
    	if(!isEmpty(measurement)) {
			updateItem(timestamp,measurement);
		}
	}
    
    public static void deleteMeasurement(String timestamp) throws Exception {
    	if(!isEmpty(timestamp)) {
    		deleteItem(timestamp);
		}
	}
    
	public static ArrayList<Statistic> generateStats(List<String> metrics, List<String> stats, String fromTimestamp, String toTimestamp) throws Exception{    	
		int resultSize=metrics.size()*stats.size();    	
    	ArrayList<Statistic> report = new ArrayList(resultSize);
    	Measurement[] list=getMeasurements(fromTimestamp,toTimestamp);
    	
    	for(String metric:metrics) {  
    		try {
    			ArrayList<Float> vals = new ArrayList<Float>(list.length-1);
    			Float avg=0.0f;
    			//Convert obj to map to access fields by string val, on error metric is skipped
    			//avg is preprocessed to avoid having to loop through the list again
        		for(int i=0;i<list.length-1;i++) {
            		JsonNode map = objectMapper.valueToTree(list[i]);            	            		
            		if(!map.findValue(metric).isNull()) {
            			Float val = map.findValue(metric).floatValue();
            			vals.add(val);
            			avg=Float.sum(avg, val);
            		}
            	}    		
        		
        		for(String statStr:stats) {
        			//generate a new statistic and add to the report if not empty
        			Statistic stat = new Statistic(metric,statStr);        			
        			if(statStr.equalsIgnoreCase("min")) {        				
        				stat.setValue(Collections.min(vals));
        			}else if(statStr.equalsIgnoreCase("max")){
        				stat.setValue(Collections.max(vals));
        			}else if(statStr.equalsIgnoreCase("average")){
        				stat.setValue(avg/vals.size());
        			}	
        			if(!isEmpty(stat.getValue())) {
        				report.add(stat);
        			}
        		}
    		}catch(Exception e) {
    			//skip metric
    			e.printStackTrace();
    		}    				
		}  
    	return report;	    	    
    }
    /**End Controller operation methods**/
    
    /**Start Json Mapper methods**/	
	public static JsonNode toJson(final Object obj) {
		//maps a given object into a JsonNode 		
		JsonNode node = objectMapper.valueToTree(obj);
		return node;
	}
	
	public static Measurement toObj(JsonNode json)throws Exception {
		//convert json to Measurement obj and validate. If not valid throw exception.
		try {					
			Measurement measurement = new Measurement();
			measurement=objectMapper.convertValue(json, Measurement.class);			
			isValidMeasurement(measurement);
			return measurement;
		}catch(Exception e) {
			throw new ValidationException();
		}
	}
	
	public static boolean isValidMeasurement(Measurement measurement)throws Exception {
		//validation conditions
		if(isEmpty(measurement)) {
			throw new ValidationException();
		}
		if(isEmpty(measurement.getTimestamp())){
			throw new ValidationException();
		}
		return true;
	}
	/**End Json Mapper methods**/					
	
	/**Start Helper Methods**/
	public static String getTimestampString(LocalDateTime ldt){
		//this is so we don't have to deal with toString trimming off the trailing zeros
		try {
			if(!isEmpty(ldt)) {
				return ldt.format(formatter);	
			}else {
				return null;
			}
		}catch(Exception e) {
			return ldt.toString();
		}		
	}
	
	public static boolean isEmpty(Object val){
		if(val==null) {
			return true;
		}else if(val.toString().trim().equals("")) {
			return true;
		}
		return false;
	}
	
	public static void nullAwareBeanCopy(Object dest, Object source) throws IllegalAccessException, InvocationTargetException {
		//used to copy property values from one obj to another when the field is not null
		//more efficient to do it manually but if extra instruments are added then this code will
		//	still function
	    new BeanUtilsBean() {
	        @Override
	        public void copyProperty(Object dest, String name, Object value)
	                throws IllegalAccessException, InvocationTargetException {
	            if(value != null) {
	                super.copyProperty(dest, name, value);
	            }
	        }
	    }.copyProperties(dest, source);
	}
	
	public static String getCamelCase(String field) {
		if(Character.isUpperCase(field.charAt(0))) {
			return Character.toLowerCase(field.charAt(0))+field.substring(1);
		}else {
			return Character.toUpperCase(field.charAt(0))+field.substring(1);
		}
	}
	/**End Helper Methods**/
		
	/**Start Map CRUD methods**/
	public static ConcurrentSkipListMap<String, Measurement> getRecordedMap() {		
		return recordedMap;
	}

	private static void setRecordedMap(ConcurrentSkipListMap<String, Measurement> newRecordedMap) {
		recordedMap = newRecordedMap;
	}
	
	private static void addToRecordedMap(Measurement measurement) throws Exception {
		//adds a measurement obj to the map if it contains a timestamp and it does not already exist
		//null timestamp check is not needed because the object has been validated
		String timestamp = getTimestampString(measurement.getTimestamp());
		if(!recordedMap.containsKey(timestamp))
			recordedMap.put(timestamp, measurement);
	}
	
	private static void replaceItem(String timestamp,Measurement measurement) throws Exception{
		//updates a given record in the map using the timestamp as the key		
		//null timestamp check is not needed because the object has been validated
		if(recordedMap.containsKey(timestamp)) {
			if(timestamp.equals(getTimestampString(measurement.getTimestamp())))
				recordedMap.put(timestamp, measurement);
			else
				throw new TimestampMismatchException();
		}else {
			throw new NotFoundException();
		}
	}
	
	private static void updateItem(String timestamp,Measurement measurement) throws Exception{
		//updates a given record in the map using the timestamp as the key		
		//null timestamp check is not needed because the object has been validated
		if(recordedMap.containsKey(timestamp)) {
			if(timestamp.equals(getTimestampString(measurement.getTimestamp()))) {
				Measurement orgMeas = recordedMap.get(timestamp);
				nullAwareBeanCopy(orgMeas,measurement);
				recordedMap.put(timestamp, orgMeas);
			}				
			else
				throw new TimestampMismatchException();
		}else {
			throw new NotFoundException();
		}
	}
	
	private static void deleteItem(String timestamp) throws Exception{
		//deletes a given record in the map using the timestamp as the key		
		//null timestamp check is not needed because the string has been validated
		if(recordedMap.containsKey(timestamp)) {
			recordedMap.remove(timestamp);
		}else {
			throw new NotFoundException();
		}
	}
	
	public static Measurement[] getMeasurements(String...prefix){
		//gets all keys in the skiplist that start with the given prefix in O(log(n)) time
		SortedMap<String, Measurement> results;
		if(prefix.length==1)
			results = recordedMap.subMap(prefix[0], prefix[0] + Character.MAX_VALUE );
		else {
			results = recordedMap.subMap(prefix[0], prefix[1] + Character.MAX_VALUE );
		}
		
		//converts result map to an array according to requirements
		Collection<Measurement> list = results.values();
		return list.toArray(new Measurement[list.size()]);
	}	
	/**End Map CRUD methods**/
}
