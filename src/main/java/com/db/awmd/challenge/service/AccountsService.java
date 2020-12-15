package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  @Setter
  private NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  public void transfer(String accountFromId, String accountToId, BigDecimal amount) {
	  if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
		  throw new RuntimeException("Transfer amount must be positive.");
	  }
	  Account accountFrom = getAccount(accountFromId);
	  Account accountTo = getAccount(accountToId);
	  
	  if(accountFrom == null || accountTo == null) {
		  throw new RuntimeException(String.format("Non-existent account. accountFrom=%s accountTo=%s", accountFrom, accountTo));
	  }
	  
	  //In concurrent env, ensure thread safety by acquiring locks on 2 accounts.
	  //To prevent deadlocks, ensure locks are always acquired in same order.
	  Account lock1 = null;
	  Account lock2 = null;
	  
	  if(accountFromId.compareTo(accountToId) >= 0) {
		  lock1 =  accountFrom;
		  lock2 = accountTo;
	  } else {
		  lock1 =  accountTo;
		  lock2 = accountFrom;
	  }
	  
	  synchronized(lock1) {
		  synchronized(lock2) {
			  if(accountFrom.getBalance().compareTo(amount) < 0) {
				  throw new RuntimeException(String.format("Insufficient balance in the account=%s, balance=%s, transfer=%s", accountFromId, accountFrom.getBalance().toString(), amount.toString()));
			  }
			  accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
			  accountTo.setBalance(accountTo.getBalance().add(amount));
		  }
	  }
	  
	  //send notifications to both account holders
	  String transferDescription = String.format("Amount=%s transferred FromAccountId=%s, ToAccountId=%s", amount.toString(), accountFromId, accountToId);
	  this.notificationService.notifyAboutTransfer(accountFrom, transferDescription);
	  this.notificationService.notifyAboutTransfer(accountTo, transferDescription);	  
  }
}
