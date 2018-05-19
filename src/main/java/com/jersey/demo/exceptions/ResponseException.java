package com.jersey.demo.exceptions;

import javax.ws.rs.core.Response;

/*Author: ABGANDH
 *Description: Used to validate updates 
 */
public class ResponseException extends Exception{
   
   ResponseException(){
   }

   protected int responseCode;

   public Response getResponse() {
	   return Response.status(responseCode).build();	   
   }
}
