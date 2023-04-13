

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The test class BankAccountTest.
 *
 * @Ahmed El-Hawat
 * @version 329.89V6
 * @Test class to test the encapsulated variables from the BankAccount class
 */
public class BankAccountTest
{
    /**
     * Default constructor for test class BankAccountTest
     */
    public BankAccountTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @BeforeEach
    public void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @AfterEach
    public void tearDown()
    {
    }

    /**
     * Method testSetAccNumber
     *Simple test to make sure the getters and setters for getAccNumber and setAccNumber work.
     */
    @Test
    public void testSetAccNumber()
    {
        BankAccount bankAcco1 = new BankAccount();
        bankAcco1.setAccNumber(10002);
        assertEquals(10002, bankAcco1.getAccNumber());
    }

    /**
     * Method testSetAccPasswd
     *Test to make sure the getters and setters for getAccPasswd and setAccPasswd work.
     */
    @Test
    public void testSetAccPasswd()
    {
        BankAccount bankAcco1 = new BankAccount();
        bankAcco1.setAccPasswd(12345);
        assertEquals(12345, bankAcco1.getAccPasswd());
    }

    /**
     * Method testSetBal
     *Test to make sure the getters and setters for SetBal and getBal work.
     */
    @Test
    public void testSetBal()
    {
        BankAccount bankAcco1 = new BankAccount();
        bankAcco1.setBal(1000);
        assertEquals(1000, bankAcco1.getBal());
    }

    /**
     * Method testGetBalance
     *Test to make sure the values for getBal and getBalance are the same.
     */
    @Test
    public void testGetBalance()
    {
        BankAccount bankAcco1 = new BankAccount();
        assertSame(bankAcco1.getBalance(), bankAcco1.getBal());
    }
}




