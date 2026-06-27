package de.thws.kompetenz.session.application.port.in;

import de.thws.kompetenz.session.domain.PrivateSessionReport;

import java.util.List;
import java.util.UUID;

public interface IGetPrivateSessionReportsUseCase {

    List<PrivateSessionReport> getAllReports();

    List<PrivateSessionReport> getReportsForSession(UUID sessionId);
}
