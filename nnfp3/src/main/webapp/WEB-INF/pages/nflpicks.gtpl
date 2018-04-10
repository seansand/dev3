<% include '/WEB-INF/includes/header.gtpl' %>


<h2>NFL Picks</h2>


<p>
   <%
      
      request.picks.each()
      {
         String line = it.replaceAll(" ", "&nbsp;")
         
         print("$line<BR>")
      }
          
	%>
</p>


<% include '/WEB-INF/includes/footer.gtpl' %>

