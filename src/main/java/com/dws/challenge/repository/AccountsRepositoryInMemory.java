package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.IncorrectAmountException;
import com.dws.challenge.exception.InsufficientFundException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    //This method will be used to deduct money from user account.
    @Override
    public void  withdraw(String accountId, BigDecimal amount) {


           BigDecimal amountBeforeTransfer= accounts.get(accountId).getBalance();
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
               synchronized (accountId)
               {
                   accounts.get(accountId).setBalance(amountBeforeTransfer.subtract(amount));
               }

           }

    }
    //This method will be used  to deposit  money to  user account.
    @Override
    public void deposit(String accountId, BigDecimal amount) {
        BigDecimal amountBeforeTransfer= accounts.get(accountId).getBalance();
        if(amount.compareTo(BigDecimal.ZERO)<=0)
        {
            throw new IncorrectAmountException("Amount to be transferred should be more than 0");
        }
        else
        {
            synchronized (accountId)
            {
                accounts.get(accountId).setBalance(amountBeforeTransfer.add(amount));
            }

        }

    }

}
