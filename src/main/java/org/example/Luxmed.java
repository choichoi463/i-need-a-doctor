package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import javafx.application.Application;
import lombok.extern.java.Log;
import org.example.browser.BrowserProvider;
import org.example.doctor.DoctorType;
import org.example.luxmed.LuxmedPage;
import org.example.ui.ConfigApp;

import java.time.Duration;

import static org.example.utils.ConfigReader.*;

@Log
public class Luxmed {

    static boolean isRegistrationDone = false;

    public static void main(String[] args) {
        Application.launch(ConfigApp.class, args);
    }

    public static void startLoop(DoctorType doctorType, String doctorName) {
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
                runRegistration(doctorType, doctorName);
            }
            try {
                Thread.sleep(Duration.ofMinutes(RETRY_INTERVAL_MINUTES));
            } catch (InterruptedException e) {
                log.severe("Thread wait has failed! Please check why. Stopping the app.");
                throw new RuntimeException(e);
            }
        }
    }


    public static void runRegistration(DoctorType doctorType, String doctorName) {
        try (BrowserProvider browserProvider = new BrowserProvider()) {
            Browser browser = browserProvider.getBrowser();

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            LuxmedPage luxmedPage = new LuxmedPage(page);

            luxmedPage.login();
            luxmedPage.emailVerification();
            luxmedPage.optionalAnketaQuestion();
            luxmedPage.selectingNewVisit(doctorType);
            luxmedPage.isThatYourFirstVisitQuestions();
            luxmedPage.partWithYesNoQuestions(doctorType);
            luxmedPage.chooseDoctorNameAndClinic(doctorName);
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