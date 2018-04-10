<% include '/WEB-INF/includes/header-beta.gtpl' %>
<!-- get picks and display them here -->

<p id="picks">
   <%
      Integer year = (new Date() - 55).getAt(Calendar.YEAR)  
      def p = java.util.regex.Pattern.compile("\\((\\d+)\\)");
      def m = p.matcher(request.picksTxt);
      if (m.find())
      {
         print('<h2 id="nnfpbetaheader" class="betaheader"><nobr><i>NNFP latest results</I>&nbsp;&nbsp;<SMALL><SMALL><SPAN title=' + year + '>WEEK ' + m.group(1) + '</SPAN></SMALL></SMALL>');
         print('</nobr></h2>');
      }
   
      request.picksTxt.eachLine()
      {
         String line = it.replaceAll(" ", "&nbsp;")
         
         //line = line.replaceAll("\\(|\\)", "");        
         if (!line.contains("NNFP") && line.trim().size() > 0)
            print("""<span class="matchup">$line</span><BR>\n""")
         
      }
	%>
</p>

<span id="jsResultsGoHere"></span>
<span id="jsProjectionsGoHere"></span>
<span id="jsNflProjectionsGoHere"></span>

<!-- remove footer

<h4 class="betafooter"> </h4>
<span class="tinyfont"><FONT COLOR = "#808080">Prefer no JavaScript? Or projections based on NFL final scores only? </FONT>
<A HREF="http://seansand.appspot.com/nnfpclassic.groovy">The previous version is here.</A></SPAN> 

-->

<%
   import groovy.json.JsonBuilder
   def oddsBuilder = new JsonBuilder(request.oddsMap)
   def oppsBuilder = new JsonBuilder(request.oppsMap)
   def gridBuilder = new JsonBuilder(request.grid)
   def mflBuilder = new JsonBuilder(request.mfl)
   print('<div class="nondisplay" id="grid">' + gridBuilder.toString() + "</div>\n\n")
   print('<div class="nondisplay" id="odds">' + oddsBuilder.toString() + "</div>\n\n")
   print('<div class="nondisplay" id="opps">' + oppsBuilder.toString() + "</div>\n\n")
   print('<div class="nondisplay" id="mfl">' +  mflBuilder.toString() + "</div>\n")  
   print('<div class="nondisplay" id="mflxml">' + request.mflXml + "</div>\n")   //remove?
   
%>

</div></body></html>