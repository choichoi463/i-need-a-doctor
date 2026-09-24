package org.example.model.doctor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VisitDto {
    DoctorType doctorType;
    String doctorName;
    boolean isFollowupVisit;
    boolean isAnyDoctor;
    boolean isTimeBefore12;
    boolean isTimeBefore18;
    boolean isTimeAfter18;
    ClinicType clinicType;
    boolean isAnyClinic;
}
