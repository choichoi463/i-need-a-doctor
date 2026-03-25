
This is a playwright repo with java 21.

To run behind the proxy use this system variable, which will skip downloading browsers each time:
` PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`

To Codegen:
https://playwright.dev/java/docs/next/codegen-intro
`mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen demo.playwright.dev/todomvc"`

This repo may utilize reading emails through imap. In aprticular - google mail.
https://www.baeldung.com/java-access-gmail-imap
To connect to the Gmail server, we need to create an app password. Let’s navigate to the Google Account settings page and select the security option at the sidebar. Then, let’s enable 2-Factor Authentication (2FA) if not already active.
app password can be created here https://myaccount.google.com/apppasswords