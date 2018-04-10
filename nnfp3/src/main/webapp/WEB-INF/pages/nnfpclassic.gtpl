<% include '/WEB-INF/includes/header.gtpl' %>


<h2>NNFP latest results</h2>


<!-- get picks and display them here -->

<p>
   <%
      
      request.picksTxt.eachLine()
      {
         String line = it.replaceAll(" ", "&nbsp;")
         line = line.replaceAll("\\(|\\)", "");        
        
         if (!line.contains("SEAN"))     // SEA within SEAN
         {      
            request.winners?.each()
            {
               line = line.replaceAll(it, "<span style='color:#008800'>$it</span>")
            }

            request.losers?.each()
            {
               line = line.replaceAll(it, "<span style='color:#dd0000'>$it</span>")
            }

            request.tiers?.each()
            {
               line = line.replaceAll(it, "<span style='color:#0000bb'>$it</span>")
            }
         }          
          
         print("$line<BR>")
      }
       
      request.resultStringList.each()
      {
         String line = it.replaceAll(" ", "&nbsp;")
        
         if (line.contains("determined"))
         {
            print("$line&nbsp; ")
            print('''<button name="button1" value="Blert" onClick="window.location.reload()">Refresh this page</button>''')
            println("<BR>")            
         }
         else
         {
            print("$line<BR>")
         }
      } 

      if (request.unknownCount > 0)
      {
         print('''<button name="button1" onClick="self.location='nnfppredict.groovy?reps=300'">Calculate win percentages</button>''') 
         
         print("&nbsp; (takes several seconds)")
         println("<BR>&nbsp;</P>")
      }      
          
	%>
</p>





<% include '/WEB-INF/includes/footer.gtpl' %>

