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
<div class=\"hero-unit\">
<h1>Current Date</h1>

<p>
    """);
        log.info "outputing the datetime attribute"
    ;
out.print("""
    The current date and time: ${ request.getAttribute('datetime') }
</p>
</div>
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
