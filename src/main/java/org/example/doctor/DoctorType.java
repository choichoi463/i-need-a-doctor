package org.example.doctor;

/**
 * Each doctor type maps to a Luxmed visit-type search string (matched via
 * Playwright's text locator when picking the visit type) and a short code
 * typed into the fake-attribute filter input, plus the file listing known
 * doctor names for that type.
 */
public enum DoctorType {

    ENDOKRYNOLOG("Endokrynolog", "Konsultacja endokrynologiczna", "endo", "endokrynolog.txt"),
    OKULISTA("Konsultacja okulistyczna z badaniem dna oka", "Konsultacja okulistyczna z badaniem dna oka", "okulistyczna", "okulista.txt"),
    GASTROSKOPIJA_TELEFONICZNA("Konsultacja gastroenterologiczna (gastrologiczna) - telefoniczna", "Konsultacja gastroenterologiczna (gastrologiczna) - telefoniczna", "gastrologiczna", "gastrolog.txt");

    private final String displayName;
    private final String visitTypeSearchParam;
    private final String visitShortCode;
    private final String doctorsFileName;

    DoctorType(String displayName, String visitTypeSearchParam, String visitShortCode, String doctorsFileName) {
        this.displayName = displayName;
        this.visitTypeSearchParam = visitTypeSearchParam;
        this.visitShortCode = visitShortCode;
        this.doctorsFileName = doctorsFileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVisitTypeSearchParam() {
        return visitTypeSearchParam;
    }

    public String getVisitShortCode() {
        return visitShortCode;
    }

    public String getDoctorsFileName() {
        return doctorsFileName;
    }
}
