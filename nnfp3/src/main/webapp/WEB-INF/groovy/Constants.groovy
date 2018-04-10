
public class Constants
{
   //Year from July-to-June
   //public static final Integer YEAR = (new Date() - 180).getAt(Calendar.YEAR)
         
   //Year from March-to-February...55 means 55 days into the next year
   //Note this calculation needs to be the same as the one in nnfp.gtpl
   public static final Integer YEAR = (new Date() - 55).getAt(Calendar.YEAR)

}