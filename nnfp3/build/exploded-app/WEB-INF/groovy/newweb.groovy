
//forward '/WEB-INF/pages/www.gtpl'   //not used


String webMappingUrl = "https://www.dropbox.com/s/1aszza3e0r6a61i/webmapping.txt?dl=1"

URL mappingUrl = new URL(webMappingUrl)

String webMappingText = mappingUrl.getText()

Boolean mappingSection = false
Boolean typesSection = false

Map map = [:]
Map types = ["" : "application/octet-stream"]

webMappingText.eachLine()
{
   line ->
   
   if (line.trim().startsWith("---"))
   {
      mappingSection = true
      typesSection = false
   }

   else if (line.trim().startsWith("+++"))
   {
      mappingSection = false
      typesSection = true
   }
   
   else 
   {
      if (line.trim() != "" && mappingSection) 
      {
         def tokens = line.split(/\s+/)
         map.put(tokens[0], tokens[1])
      }

      if (line.trim() != "" && typesSection) 
      {
         def tokens = line.split(/\s+/)
         types.put(tokens[0], tokens[1])
      }
   }
}

if (params.filename)
{
   String redirectUrl = map.get(params.filename) 
   if (!redirectUrl) 
   {
      print ""
      return
   }
   
   // normalize URL; remove ?dl=X if present
   if (redirectUrl.endsWith("?dl=1") || redirectUrl.endsWith("?dl=0"))
   {
      redirectUrl = redirectUrl.substring(0, redirectUrl.length() - 5)
   }
   
   // sout: Class ServletBinding.ServletOutput
   // http://docs.groovy-lang.org/latest/html/api/groovy/servlet/ServletBinding.html
   // com.google.appengine.api.blobstore.dev.ServeBlobFilter.ResponseWrapper

   URL url = new URL(redirectUrl + "?dl=1")
   String extension = getFileExtension(redirectUrl)
   String contentType = types.get(extension.toLowerCase())
   response.setContentType(contentType ?: "application/octet-stream")
   sout << url.getBytes()     // order matters...must be last for binaries   

}

String getFileExtension(String name) 
{
    try 
    {
        return name.substring(name.lastIndexOf(".") + 1);
    } 
    catch (Exception e) 
    {
        return "";
    }
}   






