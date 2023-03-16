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

    this.accountsRepository.withdraw(fromAccount, amount);
    this.accountsRepository.deposit(toAccount, amount);
    this.notificationService.notifyAboutTransfer(accountsRepository.getAccount(fromAccount),"Your transaction of amount " +amount
              +" has been successfully transferred to "+ toAccount + ". Please connect with our customer service if you have not done this transaction" );
    this.notificationService.notifyAboutTransfer(accountsRepository.getAccount(toAccount),"You have received " +amount
            +" from "+ fromAccount + ". Please connect with our customer service if you don't know the sender or this transaction is suspected fraud" );

  }
}