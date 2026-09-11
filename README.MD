
This is a playwright repo with java 21.

First - copy the config_default.properties into config.properties and populate your creds there.

To run behind the proxy use this system variable, which will skip downloading browsers each time:
` PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`
manually can be downloaded from here
https://playwright-verizon.azureedge.net/builds/chromium/1067/chromium-win64.zip
for codegen place it here
C:\Users\Hleb_Halkin\AppData\Local\ms-playwright\chromium-1208\chrome-win64\chrome.exe

To Codegen:
https://playwright.dev/java/docs/next/codegen-intro
`mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen demo.playwright.dev/todomvc"`
`mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen demo.playwright.dev/todomvc"`

This repo may utilize reading emails through imap. In aprticular - google mail.
https://www.baeldung.com/java-access-gmail-imap
To connect to the Gmail server, we need to create an app password. Let’s navigate to the Google Account settings page and select the security option at the sidebar. Then, let’s enable 2-Factor Authentication (2FA) if not already active.
app password can be created here https://myaccount.google.com/apppasswords

To receive a message in telegram - you should update the token in Telegram.java

To Encrypt variable:
mvn exec:java -D exec.mainClass=org.example.ConfigEncryptorMain -D exec.args="your_variable

Logic steps:
- ui with doctor type -> with doctor name
- ui followup visit boolean (extra step to choose followup while registering and skips doctor names completely (cleared on search result page))
- opens url luxmed portal , login , verification by email code
- Umow -> select a doctor type and name.
- Extra questions for visit type (depends on the doctor type)
- Hardcoded step to apply time filter 12:00-17:00, 17:00++
- Finds results -> Chooses tha last result found (usually 2-3 days, or 20-30 visits rows)-> loop ends -> telegram notification sent
  - For a followup visit clears the names from the list as well. 
  - No results -> loop restart after 5 minutes

TODO list:
- time slots selection
- time slots filtering by text from seach page
- any doctor name (blank name support on ui, and while selecting a name on ui)