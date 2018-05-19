package com.jersey.demo.exceptions;

/*Author: ABGANDH
 *Description: Used to validate updates 
 */
public class ValidationException extends ResponseException{
   
   public ValidationException(){
	   responseCode=400;
   }
}
