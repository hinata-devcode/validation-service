package com.venky.validationservice.domain.service;

public class ProviderValidationResponse {
	
	 private final boolean success;
	 private final int nameMatchScore;

	    public ProviderValidationResponse(boolean success, int nameMatchScore) {
	        this.success = success;
	        this.nameMatchScore = nameMatchScore;
	    }

	    public boolean isSuccess() {
	        return success;
	    }

	    public int getNameMatchScore() {
	        return nameMatchScore;
	    }

}
