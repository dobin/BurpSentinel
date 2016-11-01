# Burp Sentinel

Eases discovery of common security holes in web applications.

* Intro / tutorial: https://github.com/dobin/BurpSentinel/wiki/BurpSentinel---HowTo-and-introduction
* Blog: http://dobin.github.io/

With BurpSentinel it is possible for the penetration tester to quickly and easily
send a lot of malicious requests to parameters of a HTTP request. Not only that,
but it also shows a lot of information of the HTTP responses, corresponding to the
attack requests. Its easy to find low-hanging fruits and hidden vulnerabilities
like this, and allows the tester to focus on more important stuff!


## Features

* Attack payloads already inside
* Identification of reflected XSS, and stored XSS
* Identification of SQL injections (non-blind)
* Indicators and visual aid for the user to identify blind/fullblind SQL injections
* Diff original and modified requests easily


## Other 

What it cannot do:
* Find DOM Injections
* Exploit vulnerabilities


Alternatives:
* Ironwasp (www.ironwasp.org, .NET)
* Wfuzz (www.edge-security.com/wfuzz.php, Python)