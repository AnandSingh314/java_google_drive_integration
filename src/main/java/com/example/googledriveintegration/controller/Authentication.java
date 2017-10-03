/**
 * 
 */
package com.example.googledriveintegration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Anand Singh <email: avsmips@gmail.com>
 *
 */
@RestController
public class Authentication {
	
	@GetMapping(value="/check-status")
	public String checkStatus(){
		return "service is working";
	}
}
