
This is a playwright repo with java 21.

To run behind the proxy use this system variable, which will skip downloading browsers each time:
` PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`

To Codegen:
https://playwright.dev/java/docs/next/codegen-intro
`mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen demo.playwright.dev/todomvc"`
