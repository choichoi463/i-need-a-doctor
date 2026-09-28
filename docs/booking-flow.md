# Booking flow

This document describes behavior currently implemented in
`src/main/java/org/example/Luxmed.java` and
`src/main/java/org/example/luxmed/LuxmedPage.java`. Portal selectors and
conditional screens can change; verify them against the live portal before
making operational changes.

## Sequence

1. **Choose visit options.** `ConfigApp` loads `DoctorType.values()` into the
   type selector and loads the selected type's name file through
   `DoctorNameReader`. The editable doctor-name field allows a name not listed
   in the file. The form also collects the follow-up flag and builds a
   `VisitDto`.
2. **Start the loop.** `Luxmed.startLoop` obtains a `Page` from
   `BrowserSession`. The browser/context/page are shared by attempts, rather
   than recreated for each retry. The loop runs from zero through
   `loop.retry.max` inclusive, checks the registration flag, invokes one
   attempt, then sleeps for `loop.retry.interval.minutes`.
3. **Log in or reuse the session.** `LuxmedPage.login` navigates to the
   dashboard. If the login textbox is visible, it enters the configured
   username/password and submits the form; otherwise, it treats the session
   as already alive.
4. **Handle email MFA if required.** When login was needed,
   `emailVerification` connects to Gmail IMAP and records the current inbox
   count, continues through the portal prompts, polls for a new email, finds
   the latest message from `noreply@info.luxmed.pl`, extracts a six-digit code
   from `<b>...</b>`, and submits it. The normal poll checks every five seconds
   for up to 40 attempts. If polling fails, the code sleeps for
   `noemail.sleep.interval` minutes and polls again. The mail store is not
   explicitly closed in this flow. If the second poll also fails, the code
   logs that failure but still proceeds to look up a matching message and
   extract a code.
5. **Dismiss optional prompts.** `optionalAnketaQuestionPomin` waits for page
   loading and then looks for the optional “Pomiń” prompt and survey iframe.
6. **Select visit type.** `selectingNewVisit` opens the appointment flow,
   filters the visit-type input using `DoctorType.getVisitShortCode()`, and
   selects the exact configured portal visit text.
7. **Answer visit questions.** For follow-up visits, the flow waits and clicks
   “Wybierz wizytę kontrolną”. Otherwise it dispatches to a questionnaire
   handler based on `DoctorType`. Some type-specific handlers are incomplete
   or only act when a matching screen is visible. The separate first-visit
   question method exists but is not called from `runRegistration`.
8. **Search for a doctor.** For a normal visit, the flow clears any existing
   doctor selection when possible, selects the requested doctor unless its
   name is exactly `any`, and clicks “Szukaj”. Clinic selection currently
   remains disabled because the clinic variable is an empty string. For a
   follow-up, it first searches without a doctor filter, then filters results
   by the requested doctor on the results page.
9. **Inspect results and attempt reservation.** The flow waits 15 seconds,
   optionally filters follow-up results by doctor and waits another 15 seconds,
   then proceeds only if `app-term` results exist. It clicks the “12:00 -
   17:00” and “Po 17:00” UI filters, expands visible day groups, and iterates
   the displayed time entries in reverse order within each day. For each entry
   it hovers the time and clicks the last reserve button on the page. This is
   not a deliberate single-slot selection strategy; do not interpret it as
   selecting only the first available or preferred slot.
10. **Confirm and notify.** After iterating result entries, the flow clicks
    “Potwierdź rezerwację”, sets the static registration flag to `true`, saves a
    success screenshot, and sends a Telegram message. The loop sees the flag
    on its next check and exits.

## Errors, screenshots, and retries

Most public `LuxmedPage` steps catch failures, log them, save a named screenshot
under `playwright/screenshots/`, and rethrow as a runtime exception. The outer
`Luxmed.runRegistration` catches exceptions and logs the attempt failure
without propagating it to `startLoop`, so the loop proceeds to its configured
sleep and retry. The shared page is reused; an error does not reset the browser
or explicitly navigate back to a known starting state.

The retry loop's `isRegistrationDone` flag is static and is not reset when a
new `startLoop` call begins. The loop also sleeps after an attempt before
checking the flag again. These details matter when changing lifecycle or retry
behavior.

## Behavior that is not currently wired

- `VisitDto` has time-of-day and clinic selection fields, but the setup screen
  does not populate them and the result-selection method does not consult them.
- The `DoctorType` selector inputs and visit labels should be checked against
  the portal; in particular, do not assume every configured short code is
  validated.
- `LuxmedPage` contains hardcoded waits and portal-specific selectors rather
  than a complete explicit timeout/wait strategy.
- A successful slot action is treated as a completed registration once the
  confirmation button is clicked; inspect the actual portal outcome and
  confirmation email when validating changes.

## Editing this flow safely

- Keep each UI step in `LuxmedPage`, following the existing named-screenshot
  failure pattern where practical.
- Keep orchestration and retry lifecycle in `Luxmed`.
- Do not run end-to-end booking against a real account merely to test a code
  change unless you are authorized and willing to create a real reservation.
- Update this document when the step sequence, retry behavior, browser
  lifecycle, or slot-selection behavior changes.
