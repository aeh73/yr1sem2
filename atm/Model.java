import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.*;
import javafx.scene.paint.Color;
import javafx.scene.media.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;//
import java.io.*;
import java.io.PrintWriter;

// The model represents all the actual content and functionality of the app
// For the ATM, it keeps track of the information shown in the display
// (the title and two message boxes), and the interaction with the bank, executes
// commands provided by the controller and tells the view to update when
// something changes

public class Model 
{
    // the ATM model is always in one of three states - waiting for an account number, 
    
    // waiting for a password, or logged in and processing account requests. 
    // We use string values to represent each state:
    // (the word 'final' tells Java we won't ever change the values of these variables)
    final String ACCOUNT_NO = "account_no";
    final String PASSWORD = "password";
    final String LOGGED_IN = "logged_in";
    final String BAL_WITHDRAW = "bal_with";
    
    //Initialises a global MediaPlayer object
    MediaPlayer mediaPlayer;
    
    //ArrayList <Integer> transactions = new ArrayList<>();
    ArrayList<Integer> tran = new ArrayList<Integer>();// Initialises the arraylist 
    int previousTransaction;                           //variable to be stored in an arraylist at the end of each deposit or withdrawal

    // variables representing the ATM model
    String state = ACCOUNT_NO;      // the state it is currently in
    int  number = 0;                // current number displayed in GUI (as a number, not a string)
    Bank  bank = null;              // The ATM talks to a bank, represented by the Bank object.
    int accNumber = -1;             // Account number typed in
    int accPasswd = -1;             // Password typed in
    int withLim = 0;                //Withdrawal limit variable, this will increment with each withdrawal and you can set the limit in the
                                    //processWithdraw() method
    int withLim2 = 100;             //Withdrawal limit 2 variable, this will take away the amount withdrawn each time until limit is reached
    int overdraft = -100;           //Overdraft limit variable
   
    String msgWithdraw = "You withdraw £";//String for withdraw message
    String msgDeposit = "You deposited £";//String for deposit message
    
    // These three are what are shown on the View display
    String title = "Bank ATM";      // The contents of the title message
    String display1 = null;         // The contents of the Message 1 box (a single line)
    String display2 = null;         // The contents of the Message 2 box (may be multiple lines)

    // The other parts of the model-view-controller setup
    public View view;
    public Controller controller;

    // Model constructor - we pass it a Bank object representing the bank we want to talk to
    public Model(Bank b)
    {
        Debug.trace("Model::<constructor>");          
        bank = b;
    }

    // Initialising the ATM (or resetting after an error or logout)
    // set state to ACCOUNT_NO, number to zero, and display message 
    // provided as argument and standard instruction message
    public void initialise(String message) {
        setState(ACCOUNT_NO);
        number = 0;
        display1 = message; 
        display2 =  "Enter your account number\n" +
        "Followed by \"Ent\"";
    }

    // use this method to change state - mainly so we print a debugging message whenever 
    //the state changes
    public void setState(String newState) 
    {
        if ( !state.equals(newState) ) 
        {
            String oldState = state;
            state = newState;
            Debug.trace("Model::setState: changed state from "+ oldState + " to " + newState);
        }
    }

    // These methods are called by the Controller to change the Model
    // when particular buttons are pressed on the GUI
    
    // process a number key (the key is specified by the label argument)
    public void processNumber(String label)
    {
        // a little magic to turn the first char of the label into an int
        // and update the number variable with it
        char c = label.charAt(0);
        number = number * 10 + c-'0';           // Build number 
        // show the new number in the display
        display1 = "" + number;
        display();  // update the GUI
    }

    // process the Clear button - reset the number (and number display string)
    /**
     * Method processClear
     *ADDED THE LINE display2 =""; AS I DECIDED TO USE THE CLEAR BUTTON AS A CANCEL BUTTON IN SOME METHODS 
     */
    public void processClear()
    {
        // clear the number stored in the model
        number = 0;
        display1 = "";
        display2 = ""; //THIS LINE CLEARS display2
        display();  // update the GUI
    }

    // process the Enter button
    // this is the most complex operation - the Enter key causes the ATM to change state
    // from account number, to password, to logged_in and back to account number
    // (when you log out)
    public void processEnter()
    {
        // Enter was pressed - what we do depends what state the ATM is already in
        switch ( state )
        {
            case ACCOUNT_NO:
                // we were waiting for a complete account number - save the number we have
                // reset the tyed in number to 0 and change to the state where we are expecting 
                // a password
                accNumber = number;
                number = 0;
                setState(PASSWORD);
                display1 = "";
                display2 = "Now enter your password\n" +
                "Followed by \"Ent\"";
                break;
            case BAL_WITHDRAW:
                //A state to pause the withdrawal to allow for balance checking
                //
                //
                processWithdraw();
                break;
            case PASSWORD:
                // we were waiting for a password - save the number we have as the password
                // and then cotnact the bank with accumber and accPasswd to try and login to
                // an account
                accPasswd = number;
                number = 0;
                display1 = "";
                // now check the account/password combination. If it's ok go into the LOGGED_IN
                // state, otherwise go back to the start (by re-initialsing)
                if ( bank.login(accNumber, accPasswd) )
                {
                    setState(LOGGED_IN);
                    display2 = "Accepted\n" +
                    "Now enter the transaction you require";
                } else {
                    initialise("Unknown account/password");
                }
                break;
            case LOGGED_IN:     
            default: 
                // do nothing in any other state (ie logged in)
        }  
        display();  // update the GUI
    }

    // Withdraw button - check we are logged in and if so try and withdraw some money from
    // the bank (number is the amount showing in the interface display)
    /**
     * Method processWithdraw
     *ADDITIONS TO THE processWithdraw METHOD INCLUDE ADDING A WITHDRAWAL LIMIT, AN IF STATEMENT THAT CHECKS WETHER THE VARIABLE 
     *withLim IS LESS THAN 5 (THE WITHDRAWAL LIMIT) AND IF IT IS, THE NORMAL WITHDRAW PROCEDURE TAKES PLACE WITH THE ELSE AS AN ERROR 
     *MESSAGE TO BE DISPLAYED IN display2 IF IT HAS BEEN REACHED,IT HAS THE ORIGINAL processWithdraw METHOD NESTED INSIDE OF IT AND 
     *THE VARIABLE INCREMENTS WITH EACH WITHDRAWAL.
     *ALSO ADDED THE OVERDRAFT LIMIT AS THE INITIAL CHECK SO IF THE BALANCE MINUS THE AMOUNT IS GREATER THAN OR EQUAL TO THE OVERDRAFT
     *AMOUNT (WHICH CAN SET USING THE GLOBAL OVERDRAFT VARIABLE) THEN YOU WILL GET AN OVERDRAFT ERROR MESSAGE OR RUN THE NORMAL CODE.
     *AN IF-ELSE STATEMENT TO CHECK WETHER YOU HAVE ENTERED YOUR OVERDRAFT OR NOT AND DISPLAY THE CORRESPONDING TEXT.
     */
    public void processWithdraw()
    {
    if(number==0){
        if(state.equals(LOGGED_IN)){
            display2 = "Please enter a valid amount!";
            display();// update the GUI
        }
    }else{
    if(withLim2>0 && withLim2>=number){//FIRST CHECK TO MAKE SURE THE WITHDRAWAL LIMIT HAS NOT BEEN REACHED
       if((bank.getBalance()-number) >= overdraft){//CHECK TWO TO MAKE SURE THE OVERDRAFT CANT BE BREACHED
        if(withLim<5){//NESTED WITHDRAWAL METHOD INSIDE AN IF STATEMENT THAT CHECKS WETHER THE WITHDRAWAL LIMIT OF 5 HAS BEEN REACHED
            if (state.equals(LOGGED_IN) ) {     
                if (bank.getBalance() >=  number  )
                {
                    
                    bank.withdraw( number );
                    display2 =   "Withdrawn: " + number;
                    withLim++;//WITHDRAWAL LIMIT COUNTER INCREMENTS WITH EACH WITHDRAWAL
                    withLim2 = withLim2 - number;//WITHDRAWAL LIMIT 2 COUNTER DECREMENTS WITH EACH WITHDRAWAL
                    previousTransaction = -number;//VARIABLE TO STORE TRANSACTION AS A NEGATIVE AS YOUR ARE WITHDRAWING
                    tran.add(previousTransaction);//STORE VARIABLE IN TRAN ARRAYLIST  
                }else if( bank.getBalance() <  number  )
                    {
                        bank.withdraw( number );
                        display2 =   "You are in your overdraft! " + "\nWithdrawn: " + number;
                        withLim++;//WITHDRAWAL LIMIT COUNTER INCREMENTS WITH EACH WITHDRAWAL
                        withLim2 = withLim2 - number;//WITHDRAWAL LIMIT 2 COUNTER DECREMENTS WITH EACH WITHDRAWAL
                        previousTransaction = -number;//VARIABLE TO STORE TRANSACTION AS A NEGATIVE AS YOUR ARE WITHDRAWING
                        tran.add(previousTransaction);//STORE VARIABLE IN TRAN ARRAYLIST
                    } 
                else {
                    display2 =   "You do not have sufficient funds";
                    wdError();//PLAYS ERROR SOUND USING wdError METHOD
                }
                number = 0;
                display1 = "";
            } else {
                initialise("You are not logged in");
                wdError();//PLAYS ERROR SOUND USING wdError METHOD
            }
        } else {
            display2 = "Sorry you can't withdraw more than 5 times a day, try again tomorrow!";
            display1 = "";//RESETS DISPLAY1
            wdError();//PLAYS ERROR SOUND USING wdError METHOD
        }
        }else{
            display2 = "Sorry you have reached your overdraft limit!";
            display1 = "";//RESETS DISPLAY1
            wdError();//PLAYS ERROR SOUND USING wdError METHOD
        } 
    }else{
        display2 = "Sorry that is over your withdrawal limit, try again tomorrow!";
        display1 = "";//RESETS DISPLAY1
        wdError();//PLAYS ERROR SOUND USING wdError METHOD
    }
    display();// update the GUI
    }
    }
    /**
     * Method processWithBal
     *A METHOD TO GIVE THE OPTION TO SEE YOUR BALANCE BEFORE WITHDRAWAL.
     *AN IF STATEMENT TO CHECK STATE IF LOGGED IN THEN DISPLAY THE OPTION TO VIEW BALANCE, GIVES THE USER 2 OPTIONS YES FOR BALANCE AND
     *NO FOR WITHDRAWAL.
     *YES RUNS THE METHOD BELOW THIS CALLED processWithBal AND NO RUNS processWithdraw.
     *ILL NEED TO CHANGE OR CREATE A NEW STATE TO USE THE YES AND NO BUTTONS FOR A DIFFERENT FEATURE***
     *
     */
    public void processBalOpt()
    {
        if (state.equals(LOGGED_IN) ) {
            display2 = "Would you like to check your balance before withdrawal? \nYes for balance.. Press No to withdraw the amount entered..";
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI  
    }
    
    /**
     * Method processWithBal
     * THIS METHOD IS RUN WHEN THE USER CLICKS YES WHEN ASKED WETHER THEY WANT TO CHECK BALANCE BEFORE WITHDRAWAL
     *A METHOD TO DISPLAY TEXT WITH THE USERS BALANCE AND THE OPTION TO CANCEL TRANSACTION OR WITHDRAW
     */
    public void processWithBal()
    {
        if (state.equals(LOGGED_IN) ) {
            display2 = "Press No to withdraw amount entered or Clear to cancel \nYour balance is " + bank.getBalance();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }
    // Deposit button - check we are logged in and if so try and deposit some money into
    // the bank (number is the amount showing in the interface display)
    public void processDeposit()
    {
        if (state.equals(LOGGED_IN) ) {
            bank.deposit( number );
            previousTransaction = +number;//VARIABLE TO STORE TRANSACTION AS A POSITIVE AS YOUR ARE DEPOSITING
            tran.add(previousTransaction);//STORE VARIABLE IN TRAN ARRAYLIST
            display1 = "";
            display2 = "Deposited: " + number;
            number = 0;
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Balance button - check we are logged in and if so access the current balance
    // and display it
    public void processBalance()
    {
        if (state.equals(LOGGED_IN) ) {
            number = 0;
            display2 = "Your balance is: " + bank.getBalance();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Finish button - check we are logged in and if so log out
    public void processFinish()
    {
        if (state.equals(LOGGED_IN) ) {
            // go back to the log in state
            setState(ACCOUNT_NO);
            number = 0;
            display2 = "Welcome: Enter your account number";
            bank.logout();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Any other key results in an error message and a reset of the GUI
    public void processUnknownKey(String action)
    {
        // unknown button, or invalid for this state - reset everything
        Debug.trace("Model::processUnknownKey: unknown button \"" + action + "\", re-initialising");
        // go back to initial state
        initialise("Invalid command");
        display();
    }
    /**
     * Method processMiniStatement
     *THIS METHOD WHEN CLICKED WILL DISPLAY THE USER HIS MOST RECENT TRANSACTIONS, THE ARRAYLIST IS INITIALISED GLOBALLY SO IT CAN BE USED.
     *AFTER EVERY WITHDRAWAL OR DEPOSIT THE AMOUNT IS ASSIGNED TO MY VARIABLE previousTransaction AND IF IT IS A WITHDRAWAL I PUT A
     *NEGATIVE SYMBOL TO DENOTE A WITHDRAWAL AND A POSITIVE SYMBOL TO REPRESENT A DEPOSIT(THIS HAPPENS IN THE processWithdraw METHOD
     *ONCE ADDED TO THE ARRAY LIST A NEW BUTTON "M/S" WAS CREATED AND THE METHOD BELOW IS EXECUTED.
     *IT INITIALLY CLEARS THE DISPLAY AND USING  A FOR LOOP I ITERATE THROUGH THE TRANSACTIONS, USING A NESTED IF STATEMENT I CAN 
     *CHECK WETHER THE TRANSACTION WAS POSITIVE OR NEGATIVE AND DISPLAY THE CORRESPONDING TEXT WITH THE AMOUNT.
     */
    public void processMiniStatement(){
        if (state.equals(LOGGED_IN) ) {
            processClear();//CLEARS the DISPLAY TO SHOW THE STATEMENT
            
            for(int i = 0; i<tran.size();i++){//for loop to output the ministatement, its length is based on the arraylist length
                //display2 = ( transaction ) + "\n";
                display1 = "Your lastest transactions with the latest at the bottom - Press Clear to continue..";
                /*During withdrawal and depositing I assign the amount to a variable(previousTransaction), if it is desposited I give it 
                *a positive symbol, if it is negative I give it the minus symbol this is in order to display a different output on
                *the mini statement depending on if your withdrew or deposited. We use the ArrayList method .get(i) to retrieve the
                *value of each element of the arraylist and if its positive or negative we assign a different output*/
                if(tran.get(i)<0){// IF NEGATIVE YOU HAVE WITHDRAWN
                    display2 +=  msgWithdraw + tran.get(i) + "\n";//msgWithdraw string holds withdrawn message
                }
                else{//ELSE POSITIVE AND YOU HAVE DEPOSITED
                display2 +=  msgDeposit + tran.get(i) + "\n";//msgDeposit string holds deposit message
                }
                //display2 +=  tran.get(i) + "\n";
            }
            
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }
    /**
     * Method printStatement
     * A METHOD THAT TAKES THE MINI STATEMENT ARRAYLIST AND THEN PRINTS IT TO A TEXT FILE CALLED "STATEMENTS".
     * THE FILE OBJECT CREATES A NEW FILE WITH A .TXT EXTENTION TO BE A TEXT FILE, IF THE FILENAME ALREADY EXISTS IT WILL OVERWRITE
     * THE FILE. 
     * THE FILEWRITER OBJECT CONSTRUCTOR TAKES THE FILE AS AN ARGUMENT IN ORDER TO BE ABLE TO WRITE TO IT THEN WE CREATE A PRINTWRITER
     * OBJECT THAT GETS PASSED THE FILEWRITER "FW". THE FILEWRITER OBJECT GAVE ME AN IO EXCEPTION ERROR SO WITH THE IOEXCEPTION WE CAN 
     * RUN THE CODE.
     * THE PRINTWRITER OBJECT THEN PRINTS EACH TRANSACTION ON A NEW LINE TO THE TEXTFILE WHICH WILL BE FOUND IN YOUR ROOT FOLDER 
     * FOLLOWED BY THE CLOSE METHOD TO CLOSE THE PRINTWRITER OBJECT AND DISPLAY TO UPDATE THE GUI.
     * @see https://www.youtube.com/watch?v=ScUJx4aWRi0 & https://www.youtube.com/watch?v=k3K9KHPYZFc
     */
    
    public void printStatement() throws IOException{
        if (state.equals(LOGGED_IN) ) {
             display1 = "Your most recent transaction will be at the bottom.."+"\nPress Clear to continue..";
             display2 = "You have saved your recent transactions to a text file"
                         +"\nCheck the root folder for a file named \"statements\"..";
             File file = new File("statement.txt");//FILE OBJECT CALLED FILE
             FileWriter fw = new FileWriter(file, true);//FILEWRITER OBJECT FW THAT TAKES FILE AS AN ARGUMENT, TRUE APPENDS TO FILE
             PrintWriter pw = new PrintWriter(fw);//PRINTWRITER OBJECT PW THAT TAKES FW AS AN ARGUMENT
             for (int i = 0; i < tran.size(); i++){//Loops through the tran arraylist and stops when it reaches max size of the arraylist
             pw.write(tran.get(i) + "\n");//prints each element followed by a new line
             }
             //pw.write(tran + "\n");//prints all contents as a block
             pw.close();
             fw.close();
             display();  // update the GUI
           }
        }
    /**
     * Method loadStatement
     *A METHOD TO LOAD A TEXT FILE(THE LAST STATEMENT)AND DISPLAY IT
     *BUFFEREDREADER TAKES IN A FILEREADER AS AN ARGUMENT WHICH IN TURN TAKES OUR STATEMENT.TXT FILE AS AN ARGUMENT, WE CREATE A STRING
     *VARIABLE AND USING A WHILE LOOP WE ITERATE THROUGH THE CONTENTS OF THE FILE, THE WHILE CONDITION IS ASLONG AS THE BUFFEREDREADER 
     *METHOD .readLine()(WHICH READS THE CONTENT) DOES NOT EQUAL NULL I.E. IT LOOPS THROUGH UNTIL ITS EMPTY THEN DISPLAY2 WILL UPDATE
     *WITH EACHLINE OF THE FILE FOLLOWED BY A LINEBREAK.
     *HAD TO THROW TWO EXCEPTIONS ONE FOR THE FILEREADER(FILENOTFOUNDEXCEPTION) AND THE OTHER AN IO EXCEPTION THROWN BY THE BUFFERED
     *READER OBJECT 'in'.
     */
    public void loadStatement() throws IOException, FileNotFoundException {
        //Takes in the text file as an argument for FileReader which is the argument for the BufferedReader and get assigned to
        //the BufferedReader object 'in'
        BufferedReader in = new BufferedReader(new FileReader("statement.txt"));
        String tran;//DECLARES STRING VARIABLE TO BE ABLE TO OUTPUT THE FILE AS STRING
        processClear();//CLEARS the DISPLAY TO SHOW THE STATEMENT
        display1 = "Your most recent transaction will be at the bottom.."+"\nPress Clear to continue..";
        while((tran = in.readLine()) != null)//WHILE READLINE(THE NEXT LINE)OF "statement.txt" IS NOT NULL THEN TRAN = READLINE
        {
            display2 += (tran)+"\n";//LOOPS THROUGH EACH LINE OF THE FILE AND WRITES IT TO DISPLAY2 FOLLOWED BY A LINE BREAK
        }
        try
        {
            in.close();//CLOSES THE BUFFEREDREADER
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        display();  // update the GUI - this allows the data to buffer up and display at once
    }
    /**
     * Method startSound
     *STARTUP SOUND FUNCTION THAT WILL BE CALLED UPON DURING INITIALISATION.
     *THIS METHOD CREATES AN OBJECT USING THE MEDIA CLASS WHICH IS A MEDIA RESOURCE THAT ALLOWS YOU TO INTEGRATE AUDIO AND VIDEO IN JAVAFX
     *getClass METHOD IS USED RETURN THE CLASS NAME AND TO GET INFORMATION ABOUT THE MEDIA CLASS AND USING getResource METHOD WE CAN LOAD 
     *RESOURCES FROM THE CLASS PATH DIRECTORY WITH A GIVEN NAME IN THIS CASE OUR STARTUP SOUND OF A CASH MACHINE AND THE .toExternalForm 
     *OR .toString METHODS RETURN A STRING REPRESENTATION OF THE URL OR PATH WE ENTER, THIS IS IN ORDER TO PASS THE startup OBJECT AS AN 
     *ARGUMENT IN THE MEDIAPLAYER CLASS.
     *NOW WE CAN PASS THE AUDIO CLIP OBJECT AS AN ARGUMENT TO THE MEDIAPLAYER WHICH GIVES US CONTROL AND THE ABILITY TO STOP AND START 
     *AUDIO BY USING THE .play & .stop PROPERTIES.
     *THE MEDIA AND MEDIAPLAYER CLASSES WORK IN UNISON AS THE MEDIA CLASS LOADS THE RESOURCE AND THE MEDIAPLAYER CLASS GIVES YOU THE 
     *CONTROL AND ABILITY TO USE THE MEDIA.*/
    public void startSound(){//this method is called in the main class to play on startup
        //STARTUP AUDIO
        Media startup = new Media(getClass().getResource("/startup.mp3").toString());//Loading file into media object
        mediaPlayer = new MediaPlayer(startup);//Loads the media into a media player object which gives us control to play and stop
        mediaPlayer.play();//Plays the sound clip 
        
    }
    
    /**
     * Method btnClk
     * BUTTON CLICK SOUND FUNCTION
     *THIS METHOD ENDED UP BEING ALOT SIMPLER AND RESOURCE EFFICIENT AS I HAD ALOT OF STABILITY AND STUTTER ISSUES TRYING TO RUN MULTIPLE 
     *MEDIAPLAYER OBJECTS AT THE SAME TIME AS IT SEEMS THE GARBAGE COLLECTOR (GC) DELETES EACH INSTANCE OF A MEDIAPLAYER AFTER EACH CALL AND
     *RE-INITIALISES THEM EVERYTIME A MEDIAPLAYER OBJECT IS CALLED SO I ENDED UP CREATING THE MEDIA PLAYER OBJECT AT THE START OF THE MODEL 
     *CLASS AND THEN CALLING UPON IT, AUDIOCLIP ALSO SEEMS BETTER FOR RESOURCE EFFICIENCY AS THE PROGRAM SEEMS TO RUN BETTER USING IT
     *- I BELIEVE AUDIOCLIP IS MENT FOR SMALLER FILES AND MEDIAPLAYER USED FOR LARGER FILES.
     *THIS METHOD CREATES AN AUDIOCLIP OBJECT AND USING getClass AND getResource WE CAN LOAD THE RESOURCE INTO THE OBJECT AND THEN PLAY IT.
     */
    public void btnClk()
    {
        //BUTTON AUDIO - this method is called in the buttonClicked event handler in the view class
        AudioClip btnBeep = new AudioClip(getClass().getResource("/beep3cool.wav").toExternalForm());//Loading file into audio component
        btnBeep.play();//plays file
    }
    /**
     * Method wdError
     *SAME AS THE AUDIO CLIP ABOVE, IT IS ALOT MORE RESOURCE EFFICIENT THAN MEDIAPLAYER, AGAIN WE CREATE AN AUDIOCLIP OBJECT AND USING
     *getClass AND getResource WE CAN LOAD THE AUDIO CLIP INTO THE OBJECT AND PLAY USING THE PLAY METHOD.
     *THIS METHOD IS CALLED ON ANY ERROR IN THE PROGRAM, OVERDRAFT LIMIT EXCEEDED, WITHDRAWAL LIMIT EXCEEDED OR NO FUNDS AVAILABLE.
     */
    public void wdError()
    {
        //BUTTON AUDIO - this method is called in the buttonClicked event handler in the view class
        AudioClip errorBeep = new AudioClip(getClass().getResource("/error.wav").toExternalForm());//Loading file into audio component
        errorBeep.play();//plays file
    }
    public void displayError(){
        Image img = new Image(getClass().getResource("/denied.jpg").toExternalForm());
        ImageView imgView = new ImageView(img);
        
        
    }
    // This is where the Model talks to the View, by calling the View's update method
    // The view will call back to the model to get new information to display on the screen
    /**
     * Method display
     *ADDED IF STATEMENT TO ALLOW ME TO USE JUNIT, WOULD NOT ALLOW ME TO RUN TESTS BEFORE HAND.
     */
    public void display()
    {
        if(view==null){
            return;
        }
        Debug.trace("Model::display");
        view.update();
    }
}
