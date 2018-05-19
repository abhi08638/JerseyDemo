package com.jersey.demo.resources;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.jersey.demo.annotations.PATCH;
import com.jersey.demo.dataObjects.Measurement;
import com.jersey.demo.dataObjects.Statistic;
import com.jersey.demo.exceptions.NotFoundException;
import com.jersey.demo.exceptions.TimestampMismatchException;
import com.jersey.demo.exceptions.ValidationException;
import com.jersey.demo.utils.Utils;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RootResource {
	@Context
	private UriInfo uriInfo;
	
	private static final Response BAD_REQUEST = Response.status(400).build();
	
	@PostConstruct
	public void init() {
		Utils.configure();
	}
	
	@GET
	public Response get() {		
		return Response
				.ok("Server is running!\n")
				.build();
	}

	@POST @Path("/measurements")
	@Consumes("application/json")
	public Response createMeasurement(JsonNode measurement) {
		try {
			String location=Utils.recordMeasurement(measurement);			
			Response response = Response.status(201).build();
			response.getHeaders().add("location", uriInfo.getPath()+"/"+location);
			return response;
		}catch(Exception e) {
			e.printStackTrace();
			return BAD_REQUEST;			
		}
	}

	@GET @Path("/measurements/{timestamp}")
	public Response getMeasurement(@PathParam("timestamp") String timestamp) {
		try {			
			Measurement[] list = Utils.getMeasurements(timestamp);
			if(list.length==0) {
				throw new NotFoundException();
			}else if(list.length==1) {
				return Response.ok(Utils.toJson(list[0]),MediaType.APPLICATION_JSON).build();
			}else {
				return Response.ok(Utils.toJson(list),MediaType.APPLICATION_JSON).build();
			}
		}catch(Exception e) {
			e.printStackTrace();
			if(e instanceof NotFoundException) {
				return ((NotFoundException) e).getResponse();
			}else {
				return BAD_REQUEST;	
			}			
		}
	}

	@PUT @Path("/measurements/{timestamp}")
	@Consumes("application/json")
	public Response replaceMeasurement(@PathParam("timestamp") String timestamp, JsonNode measurement) {
		try {
			Utils.replaceMeasurement(timestamp, measurement);
			Response response = Response.status(204).build();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof NotFoundException) {
				return ((NotFoundException) e).getResponse();
			}else if(e instanceof TimestampMismatchException) {
				return ((TimestampMismatchException) e).getResponse();	
			}else if(e instanceof ValidationException) {
				return ((ValidationException) e).getResponse();	
			}
			return BAD_REQUEST;
		}				
	}

	@PATCH @Path("/measurements/{timestamp}")
	@Consumes("application/json")
	public Response updateMeasurement(@PathParam("timestamp") String timestamp, JsonNode measurement) {
		try {
			Utils.updateMeasurement(timestamp, measurement);
			Response response = Response.status(204).build();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof NotFoundException) {
				return ((NotFoundException) e).getResponse();
			}else if(e instanceof TimestampMismatchException) {
				return ((TimestampMismatchException) e).getResponse();	
			}else if(e instanceof ValidationException) {
				return ((ValidationException) e).getResponse();	
			}
			return BAD_REQUEST;
		}		
	}

	@DELETE @Path("/measurements/{timestamp}")	
	public Response deleteMeasurement(@PathParam("timestamp") String timestamp) {
		try {
			Utils.deleteMeasurement(timestamp);
			Response response = Response.status(204).build();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof NotFoundException) {
				return ((NotFoundException) e).getResponse();
			}
			return BAD_REQUEST;
		}					
	}

	@GET @Path("/stats")
	public Response getStats(@QueryParam("metric") List<String> metrics, @QueryParam("stat") List<String> stats,
			@QueryParam("fromDateTime") String fromTimestamp,@QueryParam("toDateTime") String toTimestamp) {
		try {
			ArrayList<Statistic> list=Utils.generateStats(metrics,stats,fromTimestamp,toTimestamp);
			return Response.ok(Utils.toJson(list),MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return BAD_REQUEST;
		}
	}
}
