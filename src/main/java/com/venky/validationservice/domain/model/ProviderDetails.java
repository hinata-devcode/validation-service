package com.venky.validationservice.domain.model;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProviderDetails {

	private Integer nameMatchScore;
	private String registeredName;
	@JsonRawValue
	private String bankDetailsJson;

}
