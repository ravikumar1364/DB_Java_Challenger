package com.db.awmd.challenge.web.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Data;

@Data
public class TransferResponse {
	private final String transactionId;
	
	@JsonCreator
	public TransferResponse(String txnId) {
		this.transactionId = txnId;
	}
}
