package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  @Mock
  private NotificationService notificationService;
  
  @Before
  public void init() {
	  this.accountsService.setNotificationService(notificationService);
  }

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  
  /***Transfer Amount API***/
  @Test
  public void transfer_valid_Accounts_Amount() {
	  String uniqueId1 = "Id-" + System.currentTimeMillis();
	  Account account1 = new Account(uniqueId1);
	  account1.setBalance(BigDecimal.valueOf(100d));
	  this.accountsService.createAccount(account1);
	  
	  String uniqueId2 = "Id-2-" + System.currentTimeMillis();
	  Account account2 = new Account(uniqueId2);
	  this.accountsService.createAccount(account2);
	  
	  UUID txnId = this.accountsService.transfer(uniqueId1, uniqueId2, BigDecimal.TEN);
	  
	  assertNotNull(txnId.toString());
	  assertTrue(account1.getBalance().compareTo(new BigDecimal("90")) == 0);
	  assertTrue(account2.getBalance().compareTo(new BigDecimal("10")) == 0);
  }
  
  @Test
  public void transfer_valid_Accounts_failOnOverdraft() {
	  String uniqueId1 = "Id-" + System.currentTimeMillis();
	  Account account1 = new Account(uniqueId1);
	  account1.setBalance(BigDecimal.valueOf(100d));
	  this.accountsService.createAccount(account1);
	  
	  String uniqueId2 = "Id-2-" + System.currentTimeMillis();
	  Account account2 = new Account(uniqueId2);
	  this.accountsService.createAccount(account2);
	  
	  try {
		  this.accountsService.transfer(uniqueId1, uniqueId2, new BigDecimal("200"));
		  fail("Should have failed when amount balance becomes negative.");
	  } catch (RuntimeException ex) {
		  assertThat(ex.getMessage()).startsWith("Insufficient balance in the account");
	  }
  }
  
  @Test
  public void transfer_invalid_Accounts() {
	  String uniqueId1 = "Id-" + System.currentTimeMillis();
	  Account account1 = new Account(uniqueId1);
	  account1.setBalance(BigDecimal.valueOf(100d));
	  this.accountsService.createAccount(account1);
	  
	  String uniqueId2 = "Id-2-" + System.currentTimeMillis();
	  //Account account2 = new Account(uniqueId2);//Account2 does not exist
	  
	  try {
		  this.accountsService.transfer(uniqueId1, uniqueId2, new BigDecimal("200"));
		  fail("Should have failed when transferring to non-existent account.");
	  } catch (RuntimeException ex) {
		  assertThat(ex.getMessage()).startsWith("Non-existent account.");
	  }
  }
  
  @Test
  public void transfer_valid_Accounts_invalid_Amount() {
	  String uniqueId1 = "Id-" + System.currentTimeMillis();
	  Account account1 = new Account(uniqueId1);
	  account1.setBalance(BigDecimal.valueOf(100d));
	  this.accountsService.createAccount(account1);
	  
	  String uniqueId2 = "Id-2-" + System.currentTimeMillis();
	  Account account2 = new Account(uniqueId2);
	  this.accountsService.createAccount(account2);
	  
	  try {
		  this.accountsService.transfer(uniqueId1, uniqueId2, BigDecimal.TEN.negate());
		  fail("Should have failed when transferring negative amount.");
	  } catch (RuntimeException ex) {
		  assertThat(ex.getMessage()).isEqualTo("Transfer amount must be positive.");
	  }
  }
}
