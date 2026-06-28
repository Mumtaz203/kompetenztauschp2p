package de.thws.kompetenz.session.application.port.out;

import java.util.UUID;

public interface ReportedUserFlagPort {

    void updateReportCount(UUID userId, long reportCount);

    void flagUser(UUID userId);
}
