<html>
    <head>
        <title>  
         <%
            print(request?.title == null ? "NNFP latest results" : request.title)
         %>
        
        </title>
        
        <META HTTP-EQUIV="Expires" CONTENT="Tue, 01 Jan 1980 1:00:00 GMT">
        <META HTTP-EQUIV="Pragma" CONTENT="no-cache"> 

        <link rel="shortcut icon" href="/images/gaelyk-small-favicon.png" type="image/png">
        <link rel="icon" href="/images/gaelyk-small-favicon.png" type="image/png">

        <!-- srs change these -->        
        <link rel="stylesheet" type="text/css" href="/css/main.css"/>
        <script type="text/javascript" src="/js/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="/js/utility.js"></script>
        <script type="text/javascript" src="/js/json2.js"></script>
        <script type="text/javascript" src="/js/example.js"></script>
    
    </head>
    
    <body onload="onDocumentLoad();"> 

        <div>
