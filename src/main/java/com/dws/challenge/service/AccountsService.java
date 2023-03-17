package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.IncorrectAmountException;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.SortedSet;
import java.util.TreeSet;

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

    Account senderAccount= this.accountsRepository.getAccount(fromAccount);
    Account receiverAccount= this.accountsRepository.getAccount(toAccount);
    SortedSet<String> accounts= new TreeSet<>();
    accounts.add(senderAccount.getAccountId());
    accounts.add(receiverAccount.getAccountId());
    String acc1= accounts.last();
    String acc2= accounts.first();
    synchronized (acc1)
    {
      synchronized (acc2)
      {
              withdraw(senderAccount, amount);
              deposit(receiverAccount, amount,senderAccount);
      }
    }

    this.notificationService.notifyAboutTransfer(senderAccount,"Your transaction of amount " +amount
              +" has been successfully transferred to "+ toAccount + ". Please connect with our customer service if you have not done this transaction" );
    this.notificationService.notifyAboutTransfer(receiverAccount,"You have received " +amount
            +" from "+ fromAccount + ". Please connect with our customer service if you don't know the sender or this transaction is suspected fraud" );

  }

  public void  withdraw(Account senderAccount, BigDecimal amount) {

    BigDecimal amountBeforeTransfer= senderAccount.getBalance();
    if(amountBeforeTransfer.compareTo(amount)==-1)
    {
        throw  new InsufficientFundException("There is not enough funds on the account"+senderAccount.getAccountId());
    }
    else if(amount.compareTo(BigDecimal.ZERO)<=0)
    {
        throw new IncorrectAmountException("Amount to be transferred should be more than 0");

    }
    else
    {
        senderAccount.setBalance(amountBeforeTransfer.subtract(amount));

    }

  }
  //This method will be used  to deposit  money to  user account.

  public void deposit(Account receiverAccount, BigDecimal amount,Account senderAccount) {
    BigDecimal amountBeforeTransfer= receiverAccount.getBalance();
    if(amount.compareTo(BigDecimal.ZERO)<=0)
    {
       throw new IncorrectAmountException("Amount to be transferred should be more than 0");
    }
    else
    {
        try
        {
            receiverAccount.setBalance(amountBeforeTransfer.add(amount));
        }
        catch (RuntimeException ex)
        {
            log.info("Deposit to recepient account failed due to "+ex.getMessage());
            senderAccount.setBalance(amountBeforeTransfer.add(amount));
            //This is added so notification should not be sent to users
            throw  new RuntimeException("Deposit failed to recipient account");
        }

    }
  }
}