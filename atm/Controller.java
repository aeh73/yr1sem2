
// The ATM controller is quite simple - the process method is passed
// the label on the button that was pressed, and it calls different
// methods in the model depending what was pressed.
public class Controller 
{
    public Model model;
    public View  view;

    // we don't really need a constructor method, but include one to print a 
    // debugging message if required
    public Controller()
    {
        Debug.trace("Controller::<constructor>");
    }

    // This is how the View talks to the Controller
    // AND how the Controller talks to the Model
    // This method is called by the View to respond to some user interface event
    // The controller's job is to decide what to do. In this case it uses a switch 
    // statement to select the right method in the Model
    /**
     * Method process
     * ADDED ADDITIONAL BUTTONS AND ADDITIONAL METHODS INCLUDING M/S AND PROCESSMINISTATEMENT, W/D WITH NEW METHOD FROM MODEL, YES AND NO
     * BUTTONS TO HELP WITH NAVIGATION AND A P/S BUTTON TO PRINT YOUR MINISTATEMENT TO A TEXT FILE.
     * 
     */
    public void process( String action ) throws java.io.IOException
    {
        Debug.trace("Controller::process: action = " + action);
        switch (action) {
        case "1" : case "2" : case "3" : case "4" : case "5" :
        case "6" : case "7" : case "8" : case "9" : case "0" : 
            model.processNumber(action);
            break;
        case "M/S":
            model.processMiniStatement();//calls on processministatement in model to provide transaction history
            break;
        case "CLEAR":
            model.processClear();
            break;
        case "Ent":
            model.processEnter();
            break;
        case "W/D":
            //model.setState(model.BAL_WITHDRAW);
            model.processBalOpt();//calls on processbalopt method in model to give option to view balance
            break; 
        case "Dep":
            model.processDeposit();
            break;
        case "Bal":
            model.processBalance();
            break; 
        case "Fini":
            model.processFinish();
            break;
        case "Yes":
            model.processWithBal();//calls on processwithbal method in model to view balance 
            break;
        case "No":
            model.processWithdraw();//calls on processwithdraw method in model to withdraw immediately
            break;
        case "P/S":
            model.printStatement();//calls on printstatement method in model to print a text file version of the mini statement
            break;
        case "L/S":
            model.loadStatement();//calls on loadstatement method in model to retreive data from file if any
            break;  
        default:
            model.processUnknownKey(action);
            break;
        }    
    }

}
