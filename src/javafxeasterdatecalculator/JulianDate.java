package javafxeasterdatecalculator;

import java.time.LocalDate;
import java.time.Month;

/* 
 * This class represents a date according to Julian calendar.
 * 
 * Unlike Gregorian calendar, Julian calendar has some extra leap years.
 * 
 * This class was created as a substitute for LocalDate type, which uses the Gregorian calendar and cannot always hold Julian dates,
 * since some valid Julian dates (such as 29-February-1700) would be rejected by LocalDate.
 * 
 * This class holds dates the same way as predefined LocalDate Java class does, except that it also allows a few February 29 dates that LocalDate does not permit.
 * 
 * To validate Julian dates (except the extra February 29 dates) we get help from LocalDate type.
 * Except for the extra February 29 dates, if a date is not a valid LocalDate, it is also not a valid Julian date.
 * 
 * Using LocalDate to validate our JulianDate values means our JulianDate will be very similar to LocalDate.
 * That means it uses astronomical year numbering and the year is limited to [-999999999,999999999] interval.
 * 
 * Historically, we jumped from year 1 BC to year 1 AD without passing through zero.
 * However, astronomical year numbering style does have a year numbered zero. That means that year 1 BC is numbered as 0, year 2 BC is numbered as -1 and so on.
 * 
 * Astronomical year numbering helps with making the same calculations for BC and AD years.
 * For instance, it is counter-intuitive that year 1001 BC is a leap year, but if you think that the same year is numbered as -1000 it starts to make more sense.
 * Also, this JulianDate class defines a method that converts a Julian date to a Gregorian date. The conversion algorithm requires astronomical year numbering.  
 */
class JulianDate{

  private int year, month, day;

  /* The constructor for JulianDate receives the arguments in the same order as they are used for Java predefined LocalDate class, to avoid order confusion */
  public JulianDate(int year, int month, int day){

    /*
     * We use LocalDate (which is a Gregorian date type) as helper class, to validate our Julian date.
     * Otherwise, explicit logic should have been written to validate that the days, months and years given as constructor arguments form a valid date.
     * Since some dates of February 29 only exist in the Julian calendar, they had to be dealt with separately.
     * 
     * LocalDate constructor automatically validates the received arguments and throws an exception if they do not form a valid date.
     * We create a (Gregorian) LocalDate with the same arguments as our JulianDate (except for February 29 dates that exist only in Julian calendar).
     * If LocalDate creation fails, it means that our JulianDate is also invalid.
     * The only reason we create LocalDate is to validate our JulianDate. 
     */
    try{

      if ((year%100 == 0)&(year%400!=0)&(month==2)&(day==29)) {
        /*
         * It means that this is a leap year in Julian calendar, but not in Gregorian calendar and the given Julian date is February 29.
         * Creating a (Gregorian) LocalDate for February 29 will cause an exception. We do not want that, since this is actually a valid Julian date.
         * So we don't create a LocalDate with these values. We already know that February 29 is valid for this Julian year. 
         * But we don't know if the year respects the interval of values for a LocalDate type and this is the reason 
         * why we are still creating a LocalDate here: to validate the year.
         */
        LocalDate.of(year, month, 28); /* in this case, using 29 for the LocalDate day number will cause a runtime exception */
      }
      else {
        /* we try to validate the date with the originally given values */
        LocalDate.of(year, month, day);
      }
    }
    catch (Exception e){
      throw new RuntimeException("Invalid Julian date (y/m/d): "+year+"/"+month+"/"+day);
    }

    this.year = year;
    this.month = month;
    this.day = day;

  }

  /* We don't write setter methods, since that would open the possibility of obtaining invalid dates. */

  public int getYear(){
    return this.year;
  }

  public int getMonth(){
    return this.month;
  }

  public int getDay(){
    return this.day;
  }

  /* This method returns the name of the month for the current date instance */
  public String getMonthName(){
    Month[] months = Month.values();
    return months[this.month-1].toString();
  }

  /*
   * This method is used to display a date in a format that would eliminate user confusion between days and months.
   * It returns a date (as a String) where the month is given by its name, instead of its number.
   */
  public String getCustomFormattedDate(){
    return this.day+" "+getMonthName()+" "+this.year;
  }

  /*
   * This method calculates a preliminary offset between this JulianDate and its corresponding Gregorian date.
   * A negative number means that the Julian calendar is ahead of the Gregorian calendar.
   */
  public int getSecularDifference(){
    return (int)Math.floor(this.year/100d) - (int)Math.floor(this.year/400d) - 2;
  }

  /* 
   * This method is used to convert the date held by the current JulianDate instance to a Gregorian date.
   * It works for both BC and AD dates.
   * 
   * If BC date is being used, please remember that the year used by the method has to be in astronomical year format.
   * Example:
   *   If you want to convert 29 February 1001 BC to its corresponding Gregorian date, you need a JulianDate created with (-1000,2,29) as arguments.
   *   You will obtain a Gregorian date of 19 February -1000, which means 19/February/1001 BC.
   *   
   * For AD dates, you use the year as it is.
   */
  public LocalDate julianDateToGregorianDate(){

    int offset = getSecularDifference();
    int newDay = this.day; /* the day may suffer a correction and we do not want to corrupt 'day' instance field */

    /* if year is leap in the Julian calendar, but not in the Gregorian calendar, corrections have to be made */
    if ((this.year%100 == 0)&(this.year%400!=0)) {

      /* 
       * For Julian dates before February 29 (inclusive) we make an adjustment of one day, because we have a leap Julian year (where 29 February exists)
       * that is not Gregorian leap year (February will only have 28 days in the calendar being converted to).
       * The adjustment is done by decreasing the offset that will be applied, or by decreasing the day and later applying the initially computed offset.
       */
      if ((this.month==1)||((this.month==2)&&(this.day<=28))) {
        /* 
         * We decrement the offset; we cannot work by decrementing the day, since that will lead to exception if initial day is one and becomes zero,
         * because a date of type LocalDate (that means Gregorian) will later be created with that day, and a day of zero will lead to an exception 
         * being thrown during Gregorian date creation. 
         */
        offset--;
      }

      if ((this.month==2)&&(this.day==29)){
        /*
         * For February 29 we subtract one from the date's day, since we later create a Gregorian date with that day 
         * and a Gregorian date of February 29 would be invalid.
         * So, instead of creating a Gregorian date of 29 February (which would lead to exception) and trying to apply an adjusted offset to it,
         * we create a date of February 28 and later apply the unadjusted offset to it.
         */
        newDay--;
      }

    }

    /* It is now safe to create a Java Gregorian date without causing a runtime exception */
    LocalDate ld = LocalDate.of(this.year, this.month, newDay);

    /* 
     * We adjust the date with the necessary offset, to finally obtain a correct Gregorian date that corresponds to the Julian date stored in this JulianDate instance.
     * Please remember that this operation could lead to exceeding the boundaries that LocalDate type has, which will cause runtime exceptions. 
     */
    ld = ld.plusDays(offset); /* 'plusDays' method adds a number of days and automatically changes the month and year fields if necessary; it also works with negative arguments */

    return ld;

  }

}
