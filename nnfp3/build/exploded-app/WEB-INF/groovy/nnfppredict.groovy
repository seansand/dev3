//nnfppredict.groovy

import java.util.regex.*;


class MyGlobals
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

MyGlobals g = new MyGlobals()

request.title = "NNFP latest results"

// First get picks and return them as request.picksTxt

Random r = new Random()

//URL url = new URL('http://home.comcast.net/~seansand/picks-temp.txt') ; request.title += " (TEMP)"
URL url = new URL('https://dl.dropbox.com/u/14876946/picks.txt?' + r.nextInt() )


def response = url.get()
assert response.responseCode == 200
assert response.text.contains('NNFP Week')

def picksTxt = response.text
request.picksTxt = response.text


g.mWeekNumber = readPicks(picksTxt, g)

Set<String> winners = new HashSet()
Set<String> losers = new HashSet()
Set<String> tiers = new HashSet()



try
{
   int gameCount = validatePicks(g)

   g.mResultsSlurper = g.mCurrentSlurper = new NnfpSlurper(g.mWeekNumber, YEAR);

   Printline pr = new Printline()
   DontPrintline dontpr = new DontPrintline()  
     
   getResults(dontpr, winners, losers, tiers, g)
   displayResults(pr, g, false)
  
   request.winners = winners;
   request.losers = losers;
   request.tiers = tiers;
   
   request.resultStringList = pr.displayStringList
 
           //g.mUnknownCount = 17   //for testing
 
   request.unknownCount = g.mUnknownCount
 
  
   if (g.mUnknownCount > 0)
   {
     
      // NOW DO PREDICTION STUFF
      
      pr = new Printline()
     
      g.mCurrentSlurper = new NnfpOddsReader()
                  
      int trialCount = 500
      
      try
      {
         trialCount = Integer.parseInt(params.get("reps"))  
         trialCount = Math.max(trialCount, 50)
         trialCount = Math.min(trialCount, 5000) 
         
      }
      catch (Exception e) {}  // do nothing, leave 500 as default
      
      
      Map<String, Integer> winnerMap = new HashMap<String, Integer>()
      
      Map<String, Integer> allWinnersMap = new HashMap<String, HashMap<String, Integer>>()
      
      List nflWinners = []
      String weekWinner;
      
      for (int i in (0..<trialCount))
      {
         nflWinners.clear()
         getResults(dontpr, nflWinners, [], [], g)
         
         weekWinner = displayResults(dontpr, g, true)
         increment(weekWinner, winnerMap)

         Map<String, Integer> teamWinsMap = allWinnersMap.get(weekWinner)  
         if (null == teamWinsMap)
         {
            teamWinsMap = new HashMap<String, Integer>()
            allWinnersMap.put(weekWinner, teamWinsMap)
         }
         
         nflWinners.each()
         {
            increment(it, teamWinsMap)
         }        
         
         reinitScores(g)
      }

      Set keys = winnerMap.keySet()

      List keyList = []
      keys.each()
      {
         keyList.add(new MyPair(winnerMap.get(it), it))
      }

      keyList = keyList.sort().reverse()      
      

      // Approximate odds of winning this week (leveraging point spreads)   
      
      keyList.each()
      {
         Integer wins = it.first
         String name = it.second
         
         pr.println("$name wins $wins of $trialCount (${percentage(wins, trialCount)})")   
      }

      request.predictStringList = pr.displayStringList
   
 
      Map oppsMap = g.mCurrentSlurper.getOpps()
   
      allWinnersMap = adjustAllWinnersMap(allWinnersMap, oppsMap, winners + losers + tiers)
      
      List displayDetailsList = []
      List displayKeyGamesList = []
      
      getButtonLists(keyList, allWinnersMap, oppsMap, trialCount, displayDetailsList, displayKeyGamesList)
      
      request.detailsList = displayDetailsList

      request.keyGamesList = displayKeyGamesList.collect() {it.second}   // forces a sort   
      
      
      
   }
     
   
}
catch (Exception e)
{
   request.resultStringList = [""]
   request.resultStringList.add(e.getMessage())
   e.printStackTrace()
}


//////////////

forward '/WEB-INF/pages/nnfppredict.gtpl'

//////////////


   void getButtonLists(List<MyPair> players, 
                       Map<String, Map<String, Integer>> playersMap,
                       Map oppsMap,
                       int trialCount,
                       List<List<String>> detailsList,
                       def keyGamesList)
   {
      detailsList.clear()
      keyGamesList.clear()      

      Map meanMap = new HashMap<String, Double>()
      
      players.each()
      {
         String playerName = it.second
         
         def map = playersMap.get(playerName)
         Set keySet = map.keySet()
         keySet.each()
         {
            addMap(it, meanMap, map.get(it))
         }
      }
            
      Set meanMapKeys = meanMap.keySet()
      for (String teamName : meanMapKeys)
      {
         meanMap.put(teamName, meanMap.get(teamName) / trialCount)
         //meanMap.put(teamName, ((Double) getTeamFromMap(meanMap, teamName)) / trialCount)
      }
            

      // New code, look for clinches by populating mustWinMap
     
      Map<String, Integer> mustWinMap = new HashMap<String, Integer>()
     
      if (players.size() > 1)     //Only need to do this if more than one player is left
      {
         players.each()
         {
            String playerName = it.second
            String winCount = it.first
            def map = playersMap.get(playerName)  
         
            List<MyPair> teamList = new ArrayList<MyPair>()
            Set keySet = map.keySet()
            keySet.each()
            {
               teamList.add(new MyPair(map.get(it), it))
            }

            for (MyPair p : teamList)
            {
               Integer winCountInt = Integer.parseInt(winCount)
               String teamName = p.second
               
               String opp = getTeamFromMap(oppsMap, teamName) 
               
               if (p.first == winCountInt)
               {
                  increment(teamName, mustWinMap)    //teamName must win for this player
               }
            
            }
         }
      }
      
      
      
      //Done populating mustWinMap, now do regular processing.
      String spaces = "                                                   "      
            
      //log.info(spaces + players.size().toString())      
      //log.info(spaces + mustWinMap.toString())      
      
      
      players.each()
      {
         //log.info(spaces + it.second)
      
         List stringDetailsList = []
         List<MyPair> stringKeyGamesList = new ArrayList<MyPair>()
        
         String playerName = it.second
         String winCount = it.first
         
         def map = playersMap.get(playerName)  
         
         List<MyPair> teamList = new ArrayList<MyPair>()
         Set keySet = map.keySet()
         keySet.each()
         {
            teamList.add(new MyPair(map.get(it), it))
         }

         teamList = teamList.sort().reverse()

         stringDetailsList.add('\u2014' + " Details of $playerName's $winCount wins " + '\u2014')
         stringKeyGamesList.add(new MyPair(1000D, '\u2014' + " Key matchups for $playerName " + '\u2014'))
         
         
         
         for (MyPair p : teamList)
         {
            Integer winCountInt = Integer.parseInt(winCount)

            String teamName = p.second
            String opp = getTeamFromMap(oppsMap, teamName) 
            //String ss = '\u00a0' + '\u00a0'
            
            if (p.first / winCountInt >= 0.50)
            {

               stringDetailsList.add("" + 
                  teamName + " over " + opp + ":  " + p.first + " (" + percentage(p.first, winCountInt) + ")  x" + '\u0304' + "=${percentage(getTeamFromMap(meanMap, teamName) )}")
  
            
            }
            
            Double percentageOverMean = p.first / winCountInt - getTeamFromMap(meanMap, teamName)
            
            //String logString = teamName + " over " + opp + " " + p.first + " " + winCountInt
            //log.info(spaces + logString)
           
            
            //if (p.first != winCountInt &&   //this and not needed
            
            boolean clinches = (getTeamFromMap(mustWinMap, opp) == players.size() - 1)                   
            boolean mustWin = (p.first == winCountInt)
            
            if (clinches && mustWin)
            {
               stringKeyGamesList.add(new MyPair(102D, "" +
                  teamName + " over " + opp + " (must-win, clinches)"))
            }
            else if (clinches)
            {
               stringKeyGamesList.add(new MyPair(101D, "" +
                  teamName + " over " + opp + " (clinches week)"))
            }
            else if (mustWin)
            {
               stringKeyGamesList.add(new MyPair(100D, "" +
                  teamName + " over " + opp + " (must-win)"))
            }
            else if (percentageOverMean >= 0.10)
            {
               stringKeyGamesList.add(new MyPair(percentageOverMean, "" +
                  teamName + " over " + opp + " (+${Math.round(100 * percentageOverMean)})"))
            }
         }
         
         if (stringKeyGamesList.size() == 1)
         {
            stringKeyGamesList.add(new MyPair(0D, "(none)"))
         }  
            
         stringKeyGamesList = stringKeyGamesList.sort().reverse()
     
         detailsList.add(stringDetailsList)
         keyGamesList.add(stringKeyGamesList)         
     
      }
      
      
      return 
   }




   def adjustAllWinnersMap(Map<String, Map<String, Integer>> map, Map<String, String> opps, Set donePlaying)
   {
      Map retMap = new HashMap<String, Map<String, Integer>>()
   
      Set players = map.keySet()
   
      players.each()
      {
         Map playerMap = map.get(it)
         
         Map newTeamMap = new HashMap<String, Integer>() 
         retMap.put(it, newTeamMap) 
      
         Set teams = playerMap.keySet()
         teams.each()
         {
            teamName ->
         
            if (donePlaying.contains(teamName))
            {
               // don't add
            }
            else if (teamName.startsWith("-"))
            {
               String opp = opps.get(teamName.substring(1))
               newTeamMap.put(opp, playerMap.get(teamName))
            }
            else
            {
               newTeamMap.put(teamName, playerMap.get(teamName))
            }
         }
      }
      
      return retMap
   }

   String percentage(int a, int b)
   {
      a *= 100
      int quotient = a.intdiv(b)
      return "$quotient%"
   }

   String percentage(Double d)
   {
      int a = Math.round(d * 100)
      return "$a%"
   }

   String percentage2dec(int a, int b)
   {
      double quotient = a/b
      quotient *= 10000
      quotient = Math.round(quotient)
      quotient /= 100
      return "$quotient%"
   }
   
   void addMap(String str, Map<String, Double> map, Double addThis)
   {
      Double currVal = map.get(str)
      map.put(str, currVal == null ? addThis : addThis + currVal)
   }
   
   void increment(String str, Map<String, Integer> map)
   {
      if (map.get(str) == null)
      {
         map.put(str, 1)
      }
      else
      {
         map.put(str, map.get(str) + 1)
      }
   }
   
   void reinitScores(def g)
   {
      int scoresSize = g.mScores.size()
      g.mScores = []
      scoresSize.times() {g.mScores.add(0)}
   }




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
   
   String displayResults(def pr, def g, boolean onlyWantWinner)
   {
      String retVal = ""
   
      //def results = new LinkedList[1370]   //this is too small 11/2011
      def results = new LinkedList[4000]

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
         
         if (onlyWantWinner && retVal != "")
            break;
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
   
   public static Object getTeamFromMap(Map map, String teamName)
   {
       return map.get(teamName)
   }
   
   //replace this with oppsMap.get(teamName)
   public static Object getTeamFromMap_deprecated(Map map, String teamName)
   {
       Object retVal = map.get(teamName)
       
       if (retVal != null)
          return retVal;
          
       if (map.get(NnfpSlurper.convertBack(teamName)) != null)
          return map.get(NnfpSlurper.convertBack(teamName));

       if (map.get(NnfpSlurper.convert(teamName)) != null)
          return map.get(NnfpSlurper.convert(teamName));

       else throw new IllegalStateException("Error in getTeamFromMap: $teamName not found.")
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
            losers.add(g.mOpp[i] == null ? "-" + g.mPicks[i][0] : g.mOpp[i]) 

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
            tiers.add(g.mOpp[i] == null ? "-" + g.mPicks[i][0] : g.mOpp[i])     

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
   
   
   
class MyPair implements Comparable
{
   MyPair(def first, def second) {this.first = first; this.second = second}
   public def first
   public def second
   
   public String toString() {return first + " " + second}
   public int compareTo(Object otherObject) throws ClassCastException 
   {
      if (this.first > otherObject.first) return 1
         else if (this.first < otherObject.first) return -1
         else return 0;
   }      
}



class Printline
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



class DontPrintline
{
   public List<String> displayStringList = []

   void println() {}
   void println(Object obj) {}
   void print() {}
   void print(Object obj) {}
   
}
