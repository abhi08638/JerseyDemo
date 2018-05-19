package com.jersey.demo.exceptions;

/*Author: ABGANDH
 *Description: Used to validate updates 
 */
public class TimestampMismatchException extends ResponseException{
   
   public TimestampMismatchException(){
	   responseCode=409;
   }
}
