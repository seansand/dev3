
//forward '/WEB-INF/pages/www.gtpl'   //not used


String dropboxPublic = "https://dl.dropboxusercontent.com/u/14876946/www/"


String filename = "links.html"

if (params.filename)
{
    filename = params.filename
}

URL url = (dropboxPublic + filename).toURL()
print url.text




/* OLD

if (!params.keySet())
{
   print("<HTML><BODY><H4>Specify filename as a URL parameter key.</H4>")   
}
else
{
   String fileName = params.keySet().first()
   URL url = (dropboxPublic + fileName).toURL()
   print url.text
}

*/




