package javafxeasterdatecalculator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDate;

/*
 * This class creates an application with an input form where a user can enter a number
 * representing a year and then the user can obtain Easter date for that year.
 *
 * Both Western (Catholic) and Eastern (Orthodox) Easter dates are returned for years starting with 1583.
 * In October 1582 the Gregorian calendar was introduced, and Western churches and societies adopted it over the years.
 * Eastern churches kept on using the Julian calendar, hence the difference in Easter days between West and East. 
 * For years before 1583, when Gregorian calendar was not being used, the application returns only one Easter date,
 * using Julian Easter calculation.
 *
 * This application is developed using JavaFX 8.
 */

public class EasterDateCalculator extends Application {

  /* maximum input length */
  final int maxLength = 8;
  
  final String lineSeparator = System.lineSeparator();

  /* variable used for knowing when the application displayed a message after shortening a too long input given by the user */
  boolean lenghtLimitMessageDisplayed = false;

  /* variable used for displaying the result */
  Label result = new Label();

  /*
   * This method is used for validating user input.
   * Each input field can contain at most 8 digits (0-9).
   */
  private boolean validateValue(String inputString){

    Pattern p = Pattern.compile("[0-9]{1,8}");
    Matcher m = p.matcher(inputString);
    boolean b = m.matches();
    return b;

  }

  /*
   * Since JavaFX 8 does not come with a built-in method to limit the number of characters typed in a TextField,
   * this method will be used as a workaround.
   * If the input exceeds the maximum allowed length, it is shortened to match that length.
   */
  private void limitSize(TextField tf, int maximumLength){

    if ( tf.getText().length() > maximumLength ) {

      String resizedText=tf.getText(0, maximumLength);
      /*
       * Since "limitSize" method will be called inside a change listener, modifying the input field by "limitSize" method will result in triggering 
       * again the same listener, which will re-call the "limitSize" method, thus ending up with another "limitSize" method starting to execute
       * before the first one ended, which will lead to undesired behavior, such as incorrect caret positioning or exceptions thrown in the background. 
       * To prevent this, we use "Platform.runLater" method.
       */
      Platform.runLater(() -> {

        int position = tf.getCaretPosition();
        tf.setText(resizedText);
        tf.positionCaret(position);

        /*
         * display a message that informs there is a length limit, 
         * so that the user would know why the input was shortened
         */
        showMessage("Input field is limited to "+maximumLength+" characters.");

        /* mark that the input has been automatically shortened and that a message about this has been displayed */
        lenghtLimitMessageDisplayed = true;

      });

    }

    else{

      /* it means that the input does not exceed the length limit */
      if (lenghtLimitMessageDisplayed == true){

        /*
         * it means that the user's input has previously been shortened, but now there is no case for this,
         * so the message about the previous shortening should no longer be displayed
         */
        showMessage("");
        lenghtLimitMessageDisplayed = false;

      }

    }

  }

  /* 
   * This method returns Gregorian date for Western (Catholic) Easter.
   * Gregorian calendar is the one being currently used in most of the world.  
   */
  LocalDate getWesternEasterOnGregorianCalendar(int year){

    /* we use Anonymous Gregorian algorithm (also known as "Meeus/Jones/Butcher" algorithm, because of the book where it was published) */

    int a,b,c,d,e,f,g,h,i,k,l,m,day,month;

    a = year % 19;
    b = year / 100;
    c = year % 100;
    d = b / 4;
    e = b % 4;
    f = (b + 8) / 25;
    g = (b - f + 1) / 3;
    h = (19*a + b - d - g + 15) % 30;
    i = c / 4;
    k = c % 4;
    l = (32 + 2*e + 2*i - h - k) % 7;
    m = (a + 11*h + 22*l) / 451;

    month = (h + l - 7*m + 114) / 31;
    day = ((h + l - 7*m + 114) % 31) + 1;

    return LocalDate.of(year, month, day);

  }

  /*
   * This method returns Julian date for Eastern (Orthodox) Easter.
   * To obtain the date according to present time calendar, this method's result needs to be converted to Gregorian date. 
   */
  JulianDate getJulianEasterOnJulianCalendar(int year){

    /* we use Meeus's Julian algorithm */

    int a,b,c,d,e,day,month;

    a = year % 4;
    b = year % 7;
    c = year % 19;

    d = (19*c + 15) % 30;
    e = (2*a + 4*b - d + 34) % 7;

    month = (d + e + 114) / 31;
    day = ((d + e + 114) % 31) + 1;

    /* we return an instance of a custom class created to hold Julian dates */ 
    return new JulianDate(year, month, day);

  }

  /*
   * This method is used to convert a date to a format that would eliminate user confusion between days and months.
   * It returns a date where the month is given by its name, instead of its number. This date will be displayed to the user.
   */
  public String getCustomFormattedDate(LocalDate date){
    return date.getDayOfMonth()+" "+date.getMonth()+" "+date.getYear();
  }

  public static void main(String[] args){
    Application.launch(args);
  }

  @Override
  public void start(Stage stage){

    String textFont = "Arial";

    result.setFont(Font.font(textFont, FontWeight.BOLD, 12));

    Label inputMessage = new Label ("Enter the year:");
    inputMessage.setFont(Font.font(textFont, 12));

    /* creating the input TextField for the year */
    TextField yearField = new TextField();
    yearField.setFont(Font.font(textFont, FontWeight.BOLD, 22));
    yearField.setMaxWidth(155);

    /* adding a listener, to be called whenever the text for the year changes */
    yearField.textProperty().addListener((observable,oldValue,newValue)->{

      /* the input is not allowed to exceed a certain size */
      limitSize(yearField, maxLength);

    });

    /* creating a Submit button, with G as its mnemonic */
    Button submitButton = new Button("_Get Easter date");

    /* setting the Submit button as default button, to be called if the user presses Enter key */
    submitButton.setDefaultButton(true);

    /* adding EventHandler to the button */
    submitButton.setOnAction(new EventHandler<ActionEvent>(){

      @Override
      public void handle(ActionEvent e){

        String message="";
        String yearText = yearField.getText();
        int givenYear = 0;
        JulianDate julianEasterJulianDate;

        if(validateValue(yearText) == true){

          givenYear = Integer.parseInt(yearText);

          if (givenYear<26)
            message = "It is estimated that Jesus was crucified between AD 26 and AD 37. Before that, no Easter existed."
                     +lineSeparator+"This application returns results for years starting with AD 26.";
          else{

            if (givenYear<1583){ 
              /* Gregorian calendar did not exist, so only Julian Easter is calculated */
              julianEasterJulianDate = getJulianEasterOnJulianCalendar(givenYear);
              message = "Easter date was "+julianEasterJulianDate.getCustomFormattedDate()+" (Julian date).";
            }

            else {

              /* we calculate both Western and Eastern Easter dates */

              String verbForWesternEasterResultTense = "is";
              String verbForEasternEasterResultTense = "is";

              LocalDate today = LocalDate.now();
              LocalDate westernEasterDate = getWesternEasterOnGregorianCalendar(givenYear);
              julianEasterJulianDate = getJulianEasterOnJulianCalendar(givenYear);
              LocalDate julianEasterGregorianDate = julianEasterJulianDate.julianDateToGregorianDate();

              if (westernEasterDate.compareTo(today)<0)
                verbForWesternEasterResultTense = "was";

              if (julianEasterGregorianDate.compareTo(today)<0)
                verbForEasternEasterResultTense = "was";

              message="Western Easter "+verbForWesternEasterResultTense+ " on "+getCustomFormattedDate(westernEasterDate)+" (Gregorian date).";
              if (westernEasterDate.compareTo(today)==0) message+=" Today.";

              message+=lineSeparator+"Eastern Easter "+verbForEasternEasterResultTense+" on "+julianEasterJulianDate.getCustomFormattedDate()+" (Julian date). That is "+getCustomFormattedDate(julianEasterGregorianDate)+" (Gregorian date).";
              if (julianEasterGregorianDate.compareTo(today)==0) message+=" Today.";

              /* If both Easter dates are the same (according to presently used Gregorian calendar) display an appropriate message */
              if (westernEasterDate.compareTo(julianEasterGregorianDate)==0){
                if (westernEasterDate.compareTo(today)<0)
                  message+=lineSeparator+"Both Easters were celebrated on the same day.";
                else
                  message+=lineSeparator+"Both Easters are celebrated on the same day.";
              }

            }

          }

        }

        else{
          message = "Invalid input. Must contain digits representing a positive integral number.";
        }

        showMessage(message);

        /* A result message has been displayed, so we reset the variable used with input length message */
        if (lenghtLimitMessageDisplayed == true){
          lenghtLimitMessageDisplayed = false;
        }

      }

    });

    /* creating a Clear button, with C as its mnemonic */
    Button clearButton = new Button("_Clear");

    /* setting the Clear button as cancel button, to be called if the user presses Escape key */
    clearButton.setCancelButton(true);

    /* adding EventHandler to the button */
    clearButton.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent e){
        yearField.clear();
        showMessage("");
      }
    });

    /* creating an HBox */
    HBox buttonBox = new HBox();

    /* adding children to the HBox */
    buttonBox.getChildren().addAll(submitButton, clearButton);

    /* setting the horizontal spacing between children to 5px */
    buttonBox.setSpacing(5);

    /* creating a VBox */
    VBox root = new VBox();

    /* adding the children to the VBox */
    root.getChildren().addAll(inputMessage, yearField, buttonBox, result);

    /* setting the vertical spacing between children to 5px */
    root.setSpacing(5);

    /* setting the minimum Size of the VBox */
    root.setMinSize(700, 300);

    /* setting the style for the VBox */
    root.setStyle("-fx-padding: 10;"
                + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;"
                + "-fx-border-color: #1E90FF;");

    /* creating the Scene */
    Scene scene = new Scene(root);

    /* adding the scene to the Stage */
    stage.setScene(scene);

    /* setting the title of the Stage */
    stage.setTitle("Easter Date Calculator");

    /* showing the Stage */
    stage.show();

  }

  private void showMessage(String message){
    result.setText(message);
  }

}
