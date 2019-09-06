
get "/", forward: "/WEB-INF/pages/index.gtpl"
get "/datetime", forward: "/datetime.groovy"
get "/www/@filename?", forward: "/www.groovy"
get "/w/@filename?", forward: "/www.groovy"
get "/dx/@filename?", forward: "/newweb.groovy"
get "/nnfp", forward: "/nnfp.groovy"
get "/pebblecards", forward: "/pebblecards.groovy"
get "/nflpicks", forward: "/nflpicks.groovy"
get "/spread", forward: "/spread.groovy"
get "/spreads", forward: "/spread.groovy"
get "/spreads.groovy", forward: "/spread.groovy"

get "/favicon.ico", redirect: "/images/srsico.png"
