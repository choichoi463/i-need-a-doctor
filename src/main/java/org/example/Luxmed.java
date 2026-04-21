package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import lombok.extern.java.Log;
import org.example.browser.BrowserProvider;
import org.example.luxmed.LuxmedPage;

import java.time.Duration;

import static org.example.utils.ConfigReader.*;

@Log
public class Luxmed {

    static boolean isRegistrationDone = false;

    public static void main(String[] args) {

        //setting up retry mechanism
        int MAX_RETRY_COUNT = getMaxRetryNumber(); //250h of total time run.
        int RETRY_INTERVAL_MINUTES = getRetryIntervalMinutes();
        for (int i = 0; i <= MAX_RETRY_COUNT; i++) {
            log.info("Starting execution #" + i);
            if (isRegistrationDone) {
                log.info("Loop ended. Registration is true.");
                break;
            }
            if (!isRegistrationDone) {
                endokrinologRegistration();
            }
            try {
                Thread.sleep(Duration.ofMinutes(RETRY_INTERVAL_MINUTES));
            } catch (InterruptedException e) {
                log.severe("Thread wait has failed! Please check why. Stopping the app.");
                throw new RuntimeException(e);
            }
        }
    }


    public static void endokrinologRegistration() {
        try (BrowserProvider browserProvider = new BrowserProvider()) {
            Browser browser = browserProvider.getBrowser();

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            LuxmedPage luxmedPage = new LuxmedPage(page);

            luxmedPage.login();
            luxmedPage.emailVerification();
            luxmedPage.optionalAnketaQuestion();
            luxmedPage.selectingNewVisitEndokrinolog();
            luxmedPage.isThatYourFirstVisitQuestions();
            luxmedPage.partWithYesNoQuestionsForEndo();
            luxmedPage.chooseDoctorNameAndClinic();
            isRegistrationDone = luxmedPage.selectAVisitFromTheList();

            log.info("closing the browser");
            context.close();
        } catch (Exception e) {
            log.warning("Loop failed.");
            log.warning("Exception message: " + e.getMessage());
            log.warning("Exception trace: " + e.getStackTrace());
        }
    }
}