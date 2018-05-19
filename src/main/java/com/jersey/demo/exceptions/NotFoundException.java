package com.jersey.demo.exceptions;

/*Author: ABGANDH
 *Description: Used to validate updates 
 */
public class NotFoundException extends ResponseException{
   
   public NotFoundException(){
	   responseCode=404;
   }   
}
