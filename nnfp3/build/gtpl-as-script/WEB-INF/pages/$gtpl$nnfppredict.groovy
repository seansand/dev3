package web_inf.pages;out.print("""""");
/* include#begin \WEB-INF\includes\header.gtpl */
out.print("""<html>
    <head>
        <title>  
         """);
            print(request?.title == null ? "NNFP latest results" : request.title)
         ;
out.print("""
        
        </title>
        
        <meta name=viewport content=\"width=device-width, initial-scale=1\">
        <META HTTP-EQUIV=\"Expires\" CONTENT=\"Tue, 01 Jan 1980 1:00:00 GMT\">
        <META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\"> 

        <link rel=\"shortcut icon\" href=\"/images/gaelyk-small-favicon.png\" type=\"image/png\">
        <link rel=\"icon\" href=\"/images/gaelyk-small-favicon.png\" type=\"image/png\">
        
        <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/main.css\"/>
    

    
    </head>
    
    
    
    <body>

        <div>
""");
/* include#end   \WEB-INF\includes\header.gtpl */
out.print("""


<h2>NNFP latest results</h2>


<!-- get picks and display them here -->

<p>
   """);
      
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
   
   ;
out.print("""
   
   

   
   
</p>



""");
/* include#begin \WEB-INF\includes\footer.gtpl */
out.print("""        <P></P>
		  <P></P>
        <h3> </h3>
        <TABLE BORDER=0 WIDTH=350><TR><TD WIDTH=155>
        <div>Powered by<BR>
            <A HREF=\"http://gaelyk.appspot.com\"><img src=\"/images/gaelyk.png\" HEIGHT=24 BORDER=0></A>
        </div></TD><TD WIDTH=245><A HREF=\"http://seansand.appspot.com/nnfp.groovy\">New NNFP with live scoring</A></TD></TABLE>
        
    </body>
</html>""");
/* include#end   \WEB-INF\includes\footer.gtpl */
out.print("""

""");
