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
       
      request.resultStringList?.each()
      {
         String line = it.replaceAll(" ", "&nbsp;")
         
         if (line.contains("determined"))
         {
            print("$line&nbsp; ")
            print('''<button name="button1" value="Refresh" onClick="window.location.reload()">Refresh this page</button>''')
            println("<BR>")            
         }
         else
         {
            print("$line<BR>")
         }
      } 
       
      
      if (request.unknownCount > 0)
      {
         print("Approximate odds of winning this week (leveraging point spreads):<BR><BR>")
         request.predictStringList?.each()
         {
            String line = it.replaceAll(" ", "&nbsp;")
            print("$line&nbsp;&nbsp;")
            
            if (line.trim().length() > 0)
            {
               String name = line.replaceAll("[^A-Z]", "")
               print("<input type=\"button\" value=\"Key matchups\" onClick=\"alertKeyGames$name();\">")
               print("&nbsp;")
               print("<input type=\"button\" value=\"Details\" onClick=\"alertDetails$name();\">")
               
            }
            
            print("<BR>")
            
         } 	
         
       
         print('<script language="javascript" type="text/javascript">')

         def iter = request.detailsList.iterator()
         
         request.predictStringList?.each()
         {
            if (it.trim().length() > 0)
            {
               List displays = iter.next()
               
               String name = it.replaceAll("[^A-Z]", "")
               println()
               println("function alertDetails$name()")
               print("{alert(")

               displays.each() 
               {  
                   display ->
                   print("\"$display\" + \"\\n\" + ")
               }
               
               println("\"\");}")
            }
         }

         iter = request.keyGamesList.iterator()
         
         request.predictStringList?.each()
         {
            if (it.trim().length() > 0)
            {
               List displays = iter.next()
               
               String name = it.replaceAll("[^A-Z]", "")
               println()
               println("function alertKeyGames$name()")
               print("{alert(")

               displays.each() 
               {  
                   display ->
                   print("\"$display\" + \"\\n\" + ")
               }
               
               println("\"\");}")
            }
         }

         
         print("</script>")
       
       
       
       
      }    
   
   %>
   
   

   
   
</p>



<% include '/WEB-INF/includes/footer.gtpl' %>

