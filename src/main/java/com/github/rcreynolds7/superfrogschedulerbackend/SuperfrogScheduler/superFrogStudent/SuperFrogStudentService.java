package com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.superFrogStudent;

import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.appearanceRequest.AppearanceRequest;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.appearanceRequest.AppearanceRequestRepository;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.appearanceRequest.AppearanceRequestService;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.performanceReport.PerformanceReport;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.system.enums.AppearanceRequestStatus;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SuperFrogStudentService {
    private final SuperFrogStudentRepository superFrogStudentRepository;
    private final AppearanceRequestRepository appearanceRequestRepository;

    private final AppearanceRequestService appearanceRequestService;

    @Autowired
    public SuperFrogStudentService(SuperFrogStudentRepository superFrogStudentRepository, AppearanceRequestRepository appearanceRequestRepository, AppearanceRequestService appearanceRequestService) {
        this.superFrogStudentRepository = superFrogStudentRepository;
        this.appearanceRequestRepository = appearanceRequestRepository;
        this.appearanceRequestService = appearanceRequestService;
    }

    public SuperFrogStudent findById(Integer superFrogStudentId) {
        return this.superFrogStudentRepository.findById(superFrogStudentId).
                orElseThrow(() -> new ObjectNotFoundException("superfrogstudent", superFrogStudentId));
    }

    public List<SuperFrogStudent> findAll() {
        return this.superFrogStudentRepository.findAll();
    }

    public SuperFrogStudent update(Integer superFrogStudentId, SuperFrogStudent update) {
        return this.superFrogStudentRepository.findById(superFrogStudentId)
                .map(oldSuperFrogStudent -> {
                    oldSuperFrogStudent.setFirstName(update.getFirstName());
                    oldSuperFrogStudent.setLastName(update.getLastName());
                    oldSuperFrogStudent.setEmail(update.getEmail());
                    oldSuperFrogStudent.setPhone(update.getPhone());
                    oldSuperFrogStudent.setAddress(update.getAddress());
                    oldSuperFrogStudent.setActive(update.getActive());
                    oldSuperFrogStudent.setInternational(update.getInternational());
                    oldSuperFrogStudent.setPaymentPreference(update.getPaymentPreference());

                    return this.superFrogStudentRepository.save(oldSuperFrogStudent);
                })
                .orElseThrow(() -> new ObjectNotFoundException("superfrogstudent", superFrogStudentId));
    }

    public List<SuperFrogStudent> searchStudents(String firstName, String lastName, String email, String phone) {
        Specification<SuperFrogStudent> spec = Specification.where(null);
        if (StringUtils.hasText(firstName)) {
            spec = spec.and(SuperFrogStudentSpecifications.withFirstName(firstName));
        }
        if (StringUtils.hasText(lastName)) {
            spec = spec.and(SuperFrogStudentSpecifications.withLastName(lastName));
        }
        if (StringUtils.hasText(email)) {
            spec = spec.and(SuperFrogStudentSpecifications.withEmail(email));
        }
        if (StringUtils.hasText(phone)) {
            spec = spec.and(SuperFrogStudentSpecifications.withPhone(phone));
        }
        return superFrogStudentRepository.findAll(spec);
    }

    public SuperFrogStudentDetails getDetails(Integer superFrogStudentId) {
        SuperFrogStudent student = findById(superFrogStudentId);

        List<AppearanceRequest> signedUpAppearances = appearanceRequestRepository.findByAssignedSuperFrogStudentAndAppearanceRequestStatusIn(student, List.of(
                AppearanceRequestStatus.PENDING,
                AppearanceRequestStatus.APPROVED,
                AppearanceRequestStatus.ASSIGNED
        ));

        List<AppearanceRequest> completedAppearances = appearanceRequestRepository.findByAssignedSuperFrogStudentAndAppearanceRequestStatusIn(student, List.of(AppearanceRequestStatus.COMPLETED));

        return new SuperFrogStudentDetails(
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                signedUpAppearances,
                completedAppearances
        );
    }

    public PerformanceReport generatePerformanceReport(Integer superFrogStudentId, LocalDateTime startDate, LocalDateTime endDate) {
        SuperFrogStudent student = this.findById(superFrogStudentId);
        List<AppearanceRequest> appearances = appearanceRequestService.findCompletedBySuperFrogStudentIdAndDateRange(student, startDate, endDate);

        long completedAppearances = appearances.stream().filter(a -> a.getAppearanceRequestStatus() == AppearanceRequestStatus.COMPLETED).count();
        long cancelledAppearances = appearances.stream().filter(a -> a.getAppearanceRequestStatus() == AppearanceRequestStatus.CANCELED_BY_THE_SPIRIT_DIRECTOR || a.getAppearanceRequestStatus() == AppearanceRequestStatus.CANCELED_DUE_TO_NO_PAYMENT).count();

        PerformanceReport performanceReport = new PerformanceReport();
        performanceReport.setSuperFrogStudentId(student.getId());
        performanceReport.setSuperFrogStudentFirstName(student.getFirstName());
        performanceReport.setSuperFrogStudentLastName(student.getLastName());
        performanceReport.setStartDate(startDate);
        performanceReport.setEndDate(endDate);
        performanceReport.setCompletedAppearances((int) completedAppearances);
        performanceReport.setCancelledAppearances((int) cancelledAppearances);

        return performanceReport;
    }
}
