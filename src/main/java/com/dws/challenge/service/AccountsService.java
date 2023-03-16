package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.IncorrectAmountException;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Log
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;


  public NotificationService getNotificationService() {
    return notificationService;
  }

  public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  private NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;

  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  /*
  This method will be used for transferring the money from sender and depositing to receiver account.
  This is synchronized in order to work properly in multithreaded environment.
   */
  public void transfer(String fromAccount, String toAccount, BigDecimal amount) {

    synchronized (fromAccount)
    {
      synchronized (toAccount)
      {
        withdraw(fromAccount, amount);
        deposit(toAccount, amount);
      }
    }

    this.notificationService.notifyAboutTransfer(accountsRepository.getAccount(fromAccount),"Your transaction of amount " +amount
              +" has been successfully transferred to "+ toAccount + ". Please connect with our customer service if you have not done this transaction" );
    this.notificationService.notifyAboutTransfer(accountsRepository.getAccount(toAccount),"You have received " +amount
            +" from "+ fromAccount + ". Please connect with our customer service if you don't know the sender or this transaction is suspected fraud" );

  }


  public void  withdraw(String accountId, BigDecimal amount) {


    BigDecimal amountBeforeTransfer= this.accountsRepository.getAccount(accountId).getBalance();
    if(amountBeforeTransfer.compareTo(amount)==-1)
    {
      throw  new InsufficientFundException("There is not enough funds on the account"+accountId);
    }
    else if(amount.compareTo(BigDecimal.ZERO)<=0)
    {
      throw new IncorrectAmountException("Amount to be transferred should be more than 0");
    }
    else
    {
        this.accountsRepository.getAccount(accountId).setBalance(amountBeforeTransfer.subtract(amount));
    }

  }
  //This method will be used  to deposit  money to  user account.

  public void deposit(String accountId, BigDecimal amount) {
    BigDecimal amountBeforeTransfer= this.accountsRepository.getAccount(accountId).getBalance();
    if(amount.compareTo(BigDecimal.ZERO)<=0)
    {
      throw new IncorrectAmountException("Amount to be transferred should be more than 0");
    }
    else
    {
       this.getAccountsRepository().getAccount(accountId).setBalance(amountBeforeTransfer.add(amount));
      }
  }
}