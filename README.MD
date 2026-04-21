
This is a playwright repo with java 21.

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

Logic steps:
- `10` open url
- login
- condition - if verification form - email verification
- select a doctor (name, category)
- find results
- register for the upcoming date - last slot from page 1.
- send telegram message
- end loop

Conditions for steps above:
- condition - if fails ui element - goto 10.
- add max wait time for playwright *TODO*
- condition - if session ends (same as ui fails) - goto 10.
- condition - if email is not received for very long - sleep 15 minutes. goto 10.

- observer patter for these events. (TODO may be, only if not restartign the app each time, but to react with some events instead.)
