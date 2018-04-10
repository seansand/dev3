//nnfp.groovy

import java.util.regex.*;


class Globals
{
   String mNameLine = ""
   String mLineLine = ""
   LinkedList mMatchupLines = []
   List mNames = []
   List mScores = []
   String[][] mPicks = new String[16][16]  //row, column
   List mOpp = []
   int[][] mValues = new int[16][16]
   int mMatchupCount = 0
   int mUnknownCount = 0
   int mConsecutiveUnknowns = 0
   boolean mExcelFlag = false
   int mWeekNumber = 1
   def mResultsSlurper = null;
   def mCurrentSlurper = null;
}

final Integer YEAR = Constants.YEAR

Globals g = new Globals()
Random r = new Random()

// First get picks and return them as request.picksTxt

//URL url = new URL('http://home.comcast.net/~seansand/picks-temp.txt') ; request.title += " (TEMP)"
URL url = new URL('https://dl.dropbox.com/u/14876946/picks.txt?' + r.nextInt() )

def response = url.get()
assert response.responseCode == 200
assert response.text.contains('NNFP Week')

def picksTxt = response.text
request.picksTxt = response.text

log.info("Week number is " + g.mWeekNumber)
log.info("mNames is " + g.mNames)

g.mWeekNumber = readPicks(picksTxt, g)

Set<String> winners = new HashSet()
Set<String> losers = new HashSet()
Set<String> tiers = new HashSet()

try
{
   request.title = "NNFP latest results"

   int gameCount = validatePicks(g)

   log.info("Week number is " + g.mWeekNumber)

   g.mResultsSlurper = g.mCurrentSlurper = new NnfpSlurper(g.mWeekNumber, YEAR);

   log.info(g.mResultsSlurper.toString())

   Println pr = new Println()
   
  
   getResults(new DontPrintln(), winners, losers, tiers, g)
   displayResults(pr, g)
  
   request.winners = winners;
   request.losers = losers;
   request.tiers = tiers;
   
   request.resultStringList = pr.displayStringList
   
   ///g.mUnknownCount = 17;  /////////////TEMP
   
   request.unknownCount = g.mUnknownCount
           
   
}
catch (Exception e)
{
   request.resultStringList = [""]
   request.resultStringList.add(e.getMessage())
   e.printStackTrace()
}


//////////////

forward '/WEB-INF/pages/nnfpclassic.gtpl'

//////////////



   /**
    * Returns game count for the week.
    */
   int validatePicks(def g)
   {
      int gameCount = 0;
    
      g.mNames.eachWithIndex
      {
         name, j -> 
         int sum = 0;
         def values = []
         
         for (i in 0..15)
         {
            sum += g.mValues[i][j];
            values.add(g.mValues[i][j]);
         }

         values.sort()
         
         for (i in 0..14)
         {
            int value1 = values[i]
            int value2 = values[i+1]

            if (value1 != 0 && value2 != 0)
            {
               if (value1 == value2)
               {
                  throw new IllegalArgumentException(
                     g.mNames[j] + " has duplicate " + value1 + "s")
               }

               if (1 != value2 - value1)
               {
                  throw new IllegalArgumentException(
                     g.mNames[j] + " is missing value " + (value2 - 1))
               }
            }
         }

         switch (sum)
         {
            case 0:   gameCount = 0; break;
            case 78:  gameCount = 12; break;
            case 91:  gameCount = 13; break;
            case 105: gameCount = 14; break;
            case 120: gameCount = 15; break;
            case 136: gameCount = 16; break;
            default:
               throw new IllegalArgumentException(
                  g.mNames[j] + "'s values sum to " + sum + 
                  " (must be a triangular number)")
               break;
         }
      }
      
      return gameCount
   }
   

   
   int readPicks(String picksTxt, def g)
   {
      def weekNumber = 1;

		List<String> lines = []
		
      picksTxt.eachLine() {lines.add(it)}		
	
      for (String line : lines)
      {
		   line = line.trim();
         line = line.replaceAll("\t", "  ")

         log.info(line)
         
         
         if ("".equals(line)) 
         {
            continue;
         }
         if (line.contains("---"))
         {
            mLineLine = line
            continue
         }
         if (line.contains("("))
         {
		      Pattern p = Pattern.compile(".*\\(([0-9]+)\\).*")
			   Matcher m = p.matcher(line)
			   if (m.matches())
            {
			      weekNumber = Integer.parseInt(m.group(1))
               log.info("Week number = $weekNumber")
            }
            continue
         }
         if (line.indexOf("MNT") >= 0)
         {
            break
         }
         
         if (0 == g.mNames.size())
         {
            readNames(line, g)
         }
         else
         {
            readMatchup(line, g)
         }
      }
	  
	  return weekNumber
   }
   
   
      
   void readNames(String line, def g)
   {
      g.mNameLine = line
      line = line.trim() + " "
      while (!" ".equals(line))
      {
         int spacePos = line.indexOf(" ")
         g.mNames.add(line.substring(0, spacePos).trim())
         line = line.substring(spacePos + 1).trim() + " "
         g.mScores.add(0)
      }
   }

   
   void readMatchup(String line, def g)
   {
      g.mMatchupLines.add(line)

      int i = 0
      g.mOpp.add(g.mMatchupCount, null)

      line = line.trim() + " "
      while (!" ".equals(line))
      {
         int spacePos = line.indexOf(" ")
         String teamName = line.substring(0, spacePos).trim()
         g.mPicks[g.mMatchupCount][i] = teamName.toUpperCase()
         if (!teamName.equals(g.mPicks[g.mMatchupCount][0]))
         {
            if (g.mOpp[g.mMatchupCount] == null)
            {
               g.mOpp[g.mMatchupCount] = teamName
            }
            else if (0 != g.mOpp[g.mMatchupCount].compareToIgnoreCase(teamName))
            {
               throw new IllegalArgumentException(
                  "Inconsistent teams in matchup (" + g.mPicks[g.mMatchupCount][0] +
                  "/" + g.mOpp[g.mMatchupCount] + 
                  "/" + teamName + ")")
            }
         }
         line = line.substring(spacePos + 1).trim() + " "
         spacePos = line.indexOf(" ")
         String valueStr = line.substring(0, spacePos).trim()
         g.mValues[g.mMatchupCount][i] = Integer.parseInt(valueStr)
         line = line.substring(spacePos + 1).trim() + " "
         ++i
      }
      
      ++g.mMatchupCount;
   }

   
   /*
    * Ret value is the winner.
    * Returns a list of what to display.
    */
   
   String displayResults(def pr, def g)
   {
      String retVal = ""
   
      def results = new LinkedList[1370]

      // add scores to LinkedList array
      g.mNames.eachWithIndex
      {  
         name, j -> 
         int currScore = g.mScores[j]

         if (results[currScore] == null)
         {
            results[currScore] = new LinkedList()
         }

         results[currScore].add(name + ": " + (double)(currScore / 10.0))  
      }
       
      pr.println()

      // Now go through LinkedList array and display (this sorts them)

      for (j in (0..<1370).step(5)) 
      {
         if (results[j] != null)
            Collections.shuffle(results[j])
         
         while (results[j] != null && results[j].size() > 0)
         {
            String result = results[j].removeFirst()
            pr.println(result)
            retVal = result.substring(0, result.indexOf(":"))
         }
         
          // ONLY DO THIS IF YOU ONLY WANT THE FIRST   if (retVal != "")
          //                                                break;
      } 

      pr.println()

      if (g.mUnknownCount > 1)
      {
         pr.println(g.mUnknownCount + " games not yet determined.")
      }
      else if (g.mUnknownCount == 1)
      {
         pr.println("One game not yet determined.")
      }
      else
      {
         pr.println("Results complete.")
      }

      /*
         
         if (g.mConsecutiveUnknowns > 0)
         {
            pr.println()
            pr.println(g.mNameLine)
            if (!"".equals(g.mLineLine))
            {
               pr.println(g.mLineLine)  
            }
            g.mConsecutiveUnknowns.times
            {
               pr.println(g.mMatchupLines.removeLast())
            } 
         }
      */
         
      return retVal
   }

   
   void score(char result, int row, def g)
   {
      if (result == 'U')
      {
         ++g.mUnknownCount
         ++g.mConsecutiveUnknowns
         return
      }
      
      g.mConsecutiveUnknowns = 0

      if (result == 'T')
      {
         g.mScores.eachWithIndex
         {  
            score, j ->
            int currScore = score
            currScore += 5 * g.mValues[row][j]  //penalize by half
            g.mScores[j] = currScore
         }
          
         return
      }
      
      // for each player 
      g.mNames.eachWithIndex
      {
         name, j -> 
         // if same as first
         if (g.mPicks[row][j].equals(g.mPicks[row][0]) && result == 'Y')
         {
             //good; do nothing
         }
         else if (!g.mPicks[row][j].equals(g.mPicks[row][0]) && result == 'N')
         {
             //good; do nothing
         }
         else  // wrong team won
         {
            int currScore = g.mScores[j]
            currScore += 10 * g.mValues[row][j]  
            g.mScores[j] = currScore
         }
      }
       
   }
   
   
   void getResults(def pr, def g)
   {
      getResults(pr, new ArrayList(), new ArrayList(), new ArrayList(), g)
   }
   
   void getResults(def pr, def winners, def losers, def tiers, def g)
   {
      int i = 0
      while (i < 16 && g.mPicks[i][0] != null)
      {
         if (g.mOpp[i] != null)
         {
            pr.print((i+1) + ":  " + g.mPicks[i][0] + " beats " +
                  g.mOpp[i] + ":  ")
         }
         else
         {
            pr.print((i+1) + ":  " + g.mPicks[i][0] + " wins:  ")
         }

         char c = ' ';

         if (g.mResultsSlurper.isWinner(g.mPicks[i][0]))
         {
            winners.add(g.mPicks[i][0])
            if (g.mOpp[i] != null) 
               losers.add(g.mOpp[i])

            c = 'Y'
            pr.println("YES")
         }
         else if (g.mResultsSlurper.isLoser(g.mPicks[i][0]))
         {
            winners.add(g.mOpp[i] == null ? "-" + g.mPicks[i][0] : g.mOpp[i])
            losers.add(g.mPicks[i][0])
            c = 'N'
            pr.println("NO")
         }
         else if (g.mResultsSlurper.isTier(g.mPicks[i][0]))
         {
            tiers.add(g.mPicks[i][0])
            if (g.mOpp[i] != null) 
               losers.add(g.mOpp[i])
            c = 'T'
            pr.println("TIE")
         }         
         else if (g.mCurrentSlurper.isWinner(g.mPicks[i][0]))
         {
            winners.add(g.mPicks[i][0])
            c = 'Y'
         }
         else if (g.mCurrentSlurper.isLoser(g.mPicks[i][0]))
         {
            winners.add(g.mOpp[i] == null ? "-" + g.mPicks[i][0] : g.mOpp[i])
            c = 'N'
         }
         else if (g.mCurrentSlurper.isTier(g.mPicks[i][0]))
         {
            c = 'T'
         }
         else
         {
            c = 'U'
            pr.println("")
         }
         
			g.mCurrentSlurper.incrementGameCounter()
			         
         score(c, i++, g);
      }
      
   }
   
   
   
class Pair implements Comparable
{
   Pair(def first, def second) {this.first = first; this.second = second}
   public def first
   public def second
   
   public String toString() {return first + " " + second}
   public int compareTo(Object otherObject) throws ClassCastException 
   {
      return this.first - otherObject.first
   }      
}



class Println
{
   public List<String> displayStringList = [""]

   void print() {}
   
   void println() 
   {
      displayStringList.add("")
   }
   
   void println(Object obj) 
   {
      print(obj)
      println()
   }
   
   void print(Object obj) 
   {
      String lastString = displayStringList.last()
      lastString += obj.toString()
      displayStringList.set(displayStringList.size() - 1, lastString)
   }
}



class DontPrintln
{
   public List<String> displayStringList = []

   void println() {}
   void println(Object obj) {}
   void print() {}
   void print(Object obj) {}
   
}
