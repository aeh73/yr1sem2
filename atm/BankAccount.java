// BankAccount class
// This class has instance variables for the account number, password and balance, and methods
// to withdraw, deposit, check balance etc.

// This class contains methods which you need to complete to make the basic ATM work.
// Tutors can help you get this part working in lab sessions. 
import java.util.Arrays;
import java.util.ArrayList;//
// If you choose the ATM for your project, you should make other modifications to 
// the system yourself, based on similar examples we will cover in lectures and labs.
/**
 * Privatised variables with their getters and setters for encapsulation
 */
public class BankAccount
{
    private int accNumber = 0;
    private int accPasswd = 0;
    private int balance = 0;
    
    /******GETTERS******/
    public int getAccNumber() {
      return accNumber;
    }
    public int getAccPasswd() {
      return accPasswd;
    }
    public int getBal() {
      return balance;
    }
    /******SETTERS******/
    public void setAccNumber(int newAccNumber) {
      accNumber = newAccNumber;
    }
    public void setAccPasswd(int newAccPasswd) {
      accPasswd = newAccPasswd;
    }
    public void setBal(int newBalance) {
      balance = newBalance;
    }
    public BankAccount()
    {
        
    }
    
    public BankAccount(int a, int p, int b)
    {
        accNumber = a;
        accPasswd = p;
        balance = b;
    }
    
    // withdraw money from the account. Return true if successful, or 
    // false if the amount is negative, or less than the amount in the account 
    public boolean withdraw( int amount ) 
    { 
        Debug.trace( "BankAccount::withdraw: amount =" + amount ); 

        // CHANGE CODE HERE TO WITHDRAW MONEY FROM THE ACCOUNT
        if (amount < 0){// || amount > balance+overLim) /*||balance<100)*/ {//|| balance - amount <=100
            return false;
        } /*else if(balance > amount){
            balance = balance - amount;  // subtract amount from balance
            return true; 
        }*/
        else {
            balance = balance - amount;  // subtract amount from balance
            return true; 
        }
    }
        /*ORIGINAL CODE*/
        /*if (amount < 0 || balance < amount) {
          return false;
        } else {
            balance = balance - amount;  // subtract amount from balance
            return true; 
        }
    }*/
    
    // deposit the amount of money into the account. Return true if successful,
    // or false if the amount is negative 
    public boolean deposit( int amount )
    { 
        Debug.trace( "LocalBank::deposit: amount = " + amount ); 
        // CHANGE CODE HERE TO DEPOSIT MONEY INTO THE ACCOUNT
        if (amount < 0) {
            return false;
        } else {
            balance = balance + amount;  // add amount to balance
            return true; 
        }
    }
    
    // Return the current balance in the account
    public int getBalance() 
    { 
        Debug.trace( "LocalBank::getBalance" ); 

        // CHANGE CODE HERE TO RETURN THE BALANCE
        return balance;
    }
    
}
