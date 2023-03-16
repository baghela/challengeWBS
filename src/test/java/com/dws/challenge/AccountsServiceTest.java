package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.IncorrectAmountException;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;
import com.sun.tools.javac.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Mock
  private NotificationService notificationService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
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

  @Test
  void given_transferValidAmount_ThenSuccess()  {

      String fromAccount = "Id-" + System.nanoTime();
      Account accountSender = new Account(fromAccount,BigDecimal.valueOf(1000));
      String toAccount = "Id-" + System.nanoTime();
      Account accountReceiver = new Account(toAccount,BigDecimal.valueOf(4000));
      this.accountsService.createAccount(accountSender);
      this.accountsService.createAccount(accountReceiver);
      this.accountsService.setNotificationService(Mockito.mock(EmailNotificationService.class));
      accountsService.transfer(fromAccount,toAccount,BigDecimal.valueOf(500));
      assertEquals(this.accountsService.getAccount(fromAccount).getBalance(),(BigDecimal.valueOf(500)),"Balance left  should be 500");
      assertEquals(this.accountsService.getAccount(toAccount).getBalance(),(BigDecimal.valueOf(4500)),"Balance left should be  4500");


  }


  @Test
  void given_transferValidAmountSimultaneous_ThenSuccess() throws InterruptedException {

    String fromAccount = "Id-" + System.nanoTime();
    Account accountSender = new Account(fromAccount,BigDecimal.valueOf(1000));
    String toAccount = "Id-" + System.nanoTime();
    Account accountReceiver = new Account(toAccount,BigDecimal.valueOf(4000));
    this.accountsService.createAccount(accountSender);
    this.accountsService.createAccount(accountReceiver);
    this.accountsService.setNotificationService(Mockito.mock(EmailNotificationService.class));

    Thread thread1= new Thread(()->accountsService.transfer(fromAccount,toAccount,BigDecimal.valueOf(500)));
    Thread thread2=  new Thread(()->accountsService.transfer(toAccount,fromAccount,BigDecimal.valueOf(400)));
    thread2.start();
    thread1.start();
    thread1.join();
    thread2.join();
    assertEquals(this.accountsService.getAccount(fromAccount).getBalance(),(BigDecimal.valueOf(900)),"Balance left  should be 500");
    assertEquals(this.accountsService.getAccount(toAccount).getBalance(),(BigDecimal.valueOf(4100)),"Balance left should be  4500");


  }

  @Test
  void given_transferInValidAmount_ThenFail()
  {

    String fromAccount = "Id-" + System.nanoTime();
    Account accountSender = new Account(fromAccount,BigDecimal.valueOf(1000));
    String toAccount = "Id-" + System.nanoTime();
    Account accountReceiver = new Account(toAccount,BigDecimal.valueOf(4000));
    this.accountsService.createAccount(accountSender);
    this.accountsService.createAccount(accountReceiver);
    this.accountsService.setNotificationService(Mockito.mock(EmailNotificationService.class));

    try {
      this.accountsService.transfer(fromAccount,toAccount,BigDecimal.valueOf(0));
      fail("Should have failed when transferring invalid amount");
    } catch (IncorrectAmountException ex) {
      assertThat(ex.getMessage()).isEqualTo("Amount to be transferred should be more than 0");
    }
    try {
      this.accountsService.transfer(fromAccount,toAccount,BigDecimal.valueOf(-1));
      fail("Should have failed when transferring invalid amount");
    } catch (IncorrectAmountException ex) {
      assertThat(ex.getMessage()).isEqualTo("Amount to be transferred should be more than 0");
    }
    try {
      this.accountsService.transfer(fromAccount,toAccount,BigDecimal.valueOf(2000));
      fail("Should have failed when transferring invalid amount");
    } catch (InsufficientFundException ex) {
      assertThat(ex.getMessage()).isEqualTo("There is not enough funds on the account"+fromAccount);
    }

  }

  @Test
  public void given_multipleTransactions_thenSuccess() throws InterruptedException {
    int numberOfThreads = 10;
    ExecutorService service = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    AtomicInteger counter= new AtomicInteger(0);
    for (int i = 0; i < numberOfThreads; i++) {
      service.execute(() -> {
        given_transferValidAmount_ThenSuccess();
        counter.getAndAdd(1);
        latch.countDown();
      });
    }
    latch.await();
    assertEquals(numberOfThreads, counter.get(),"Process should have  executed "+numberOfThreads+" Times");


  }


}

