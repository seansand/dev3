//nflpicks.groovy

request.title = "NFL Picks"

List<String> output = new ArrayList<String>()

NflOdds.getOdds(output)



// Added for Dimich
def LS = System.getProperty("line.separator")  
int lineCount = output.size()
List<String> ordered = new ArrayList<String>(lineCount)
def digit = java.util.regex.Pattern.compile("\\d+")
output.each()
{
   line ->
   def matcher = digit.matcher(line)
   if (matcher.find())
   {
      int index = Integer.parseInt(matcher.group(0)) - 1
	  ordered[index] = line
   }
}
ordered = ordered.reverse()
output.add("")
output.add("------")
output.add("")
ordered.each() {output.add(it)}
output.each(){println it}




request.picks = output;

//////////////


forward '/WEB-INF/pages/nflpicks.gtpl'
//////////////
 