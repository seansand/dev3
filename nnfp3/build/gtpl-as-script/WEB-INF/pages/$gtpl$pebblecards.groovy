package web_inf.pages;out.print("""{
""");
      print('"content":"' + request.content + '",');
      print('"refresh_frequency":' + request.frequency + ',');
      print('"vibrate":' + request.vibrate);
;
out.print("""
}
""");
