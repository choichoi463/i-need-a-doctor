package org.example;

import com.microsoft.playwright.*;
import javafx.application.Application;
import lombok.extern.java.Log;
import org.example.browser.BrowserProvider;
import org.example.browser.BrowserSession;
import org.example.luxmed.LuxmedPage;
import org.example.model.doctor.VisitDto;
import org.example.ui.ConfigApp;
import org.example.utils.listener.Display;
import org.example.utils.listener.Sensor;

import java.time.Duration;

import static org.example.utils.ConfigReader.*;

@Log
public class Luxmed {

    static boolean isRegistrationDone = false;
    static boolean isSessionAlive = false;

    public static void main(String[] args) {
        Application.launch(ConfigApp.class, args);
    }

    public static void startLoop(VisitDto visitData) {
        //keeping one browser alive only!
        Page page = BrowserSession.getPage(); // same browser/context/page every call

        //setting up retry mechanism
        int MAX_RETRY_COUNT = getMaxRetryNumber(); //250h of total time run.
        int RETRY_INTERVAL_MINUTES = getRetryIntervalMinutes();
        for (int i = 0; i <= MAX_RETRY_COUNT; i++) {
            log.info("Starting execution #" + i);
            isSessionAlive = false;
            if (isRegistrationDone) {
                log.info("Loop ended. Registration is true.");

                break;
            }
            if (!isRegistrationDone) {
                runRegistration(visitData, page);
            }
            try {
                Thread.sleep(Duration.ofMinutes(RETRY_INTERVAL_MINUTES));
            } catch (InterruptedException e) {
                log.severe("Thread wait has failed! Please check why. Stopping the app.");
                throw new RuntimeException(e);
            }
        }
    }

    public static Page checkForOpenedPages(BrowserContext context) {
        if (context.pages().isEmpty()) {
            return context.newPage();
        } else {
            return context.pages().get(0);
        }
    }

    /**
     * ui logic fore whole operation sequence based on visit data
     * @param visitData - doctor name, type, clinic, time, etc.
     * @param page - playwright browser page. This should help to keep the same login session opened for long
     */
    public static void runRegistration(VisitDto visitData, Page page) {
        try {

//        try (BrowserProvider browserProvider = new BrowserProvider()) {
//            Browser browser = browserProvider.getBrowser();
//
//            BrowserContext context = browser.newContext();
//            //checks for opened pages
//            Page page  = checkForOpenedPages(context);


            LuxmedPage luxmedPage = new LuxmedPage(page);
            Sensor sensor = new Sensor();
            Display isSessionAlive = new Display(sensor); //TODO may be it is not needed

            luxmedPage.login(sensor);
            luxmedPage.emailVerification(sensor);
            luxmedPage.optionalAnketaQuestionPomin();
            luxmedPage.selectingNewVisit(visitData.getDoctorType());
//            luxmedPage.isThatYourFirstVisitQuestions();
            luxmedPage.partWithYesNoQuestionsAboutVisitType(visitData.getDoctorType(), visitData.isFollowupVisit());
            luxmedPage.chooseDoctorNameAndClinic(visitData.getDoctorName(), visitData.isFollowupVisit());
            isRegistrationDone = luxmedPage.selectAVisitFromTheList(visitData.isFollowupVisit(), visitData.getDoctorName());

            log.info("waiting for the next loop now.");

//            log.info("closing the browser");
//            context.close();
        } catch (Exception e) {
            log.warning("Loop failed.");
            log.warning("Exception message: " + e.getMessage());
            log.warning("Exception trace: " + e.getStackTrace());
        }
    }
}