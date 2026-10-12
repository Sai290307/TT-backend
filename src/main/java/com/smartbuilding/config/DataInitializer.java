package com.smartbuilding.config;

import com.smartbuilding.model.Announcement;
import com.smartbuilding.model.AnnouncementType;
import com.smartbuilding.model.Building;
import com.smartbuilding.model.Issue;
import com.smartbuilding.model.IssuePriority;
import com.smartbuilding.model.IssueStatus;
import com.smartbuilding.model.Role;
import com.smartbuilding.model.Unit;
import com.smartbuilding.model.User;
import com.smartbuilding.repository.AnnouncementRepository;
import com.smartbuilding.repository.BuildingRepository;
import com.smartbuilding.repository.IssueRepository;
import com.smartbuilding.repository.UnitRepository;
import com.smartbuilding.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// Creates demo data on first startup, so the app is usable right away.
// Each block only runs when its table is still empty.
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final AnnouncementRepository announcementRepository;
    private final IssueRepository issueRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BuildingRepository buildingRepository,
                           UnitRepository unitRepository,
                           AnnouncementRepository announcementRepository,
                           IssueRepository issueRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.buildingRepository = buildingRepository;
        this.unitRepository = unitRepository;
        this.announcementRepository = announcementRepository;
        this.issueRepository = issueRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createUserIfMissing("admin", "admin@smartbuilding.com", "admin123", "Admin", "User", Role.ADMIN);
        createUserIfMissing("manager", "manager@smartbuilding.com", "manager123", "Manager", "User", Role.MANAGER);
        createUserIfMissing("resident", "resident@smartbuilding.com", "resident123", "John", "Doe", Role.USER);
        createUserIfMissing("technician", "technician@smartbuilding.com", "technician123", "Mike", "Smith", Role.USER);

        // One demo building.
        Building building;
        if (buildingRepository.count() == 0) {
            building = new Building();
            building.setName("Green Valley Apartments");
            building.setAddress("123 Main Street, Springfield");
            building.setTotalFloors(4);
            building.setTotalUnits(8);
            building = buildingRepository.save(building);
        } else {
            building = buildingRepository.findAll().get(0);
        }

        // A few demo units. The resident lives in A-101.
        if (unitRepository.count() == 0) {
            User resident = userRepository.findByUsername("resident").orElse(null);
            List<Unit> units = Arrays.asList(
                    createUnit("A-101", 1, "2BHK", 950.0, building.getId(), 3000.0, "OCCUPIED",
                            resident != null ? resident.getId() : null),
                    createUnit("A-102", 1, "2BHK", 950.0, building.getId(), 3000.0, "VACANT", null),
                    createUnit("A-201", 2, "3BHK", 1200.0, building.getId(), 4500.0, "OCCUPIED",
                            resident != null ? resident.getId() : null),
                    createUnit("B-301", 3, "1BHK", 650.0, building.getId(), 2000.0, "VACANT", null)
            );
            unitRepository.saveAll(units);
        }

        // A few demo notices.
        if (announcementRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                List<Announcement> announcements = Arrays.asList(
                        createAnnouncement("Welcome to Green Valley!",
                                "Welcome to our community. Please read the society rules on the notice board.",
                                building.getId(), admin.getId(), AnnouncementType.GENERAL, 2),
                        createAnnouncement("Water maintenance on Sunday",
                                "Water supply will be off on Sunday 10AM-2PM for tank cleaning.",
                                building.getId(), admin.getId(), AnnouncementType.MAINTENANCE, 4),
                        createAnnouncement("Pay maintenance by the 5th",
                                "Monthly maintenance is due by the 5th. Late fee applies after the 10th.",
                                building.getId(), admin.getId(), AnnouncementType.PAYMENT_REMINDER, 3)
                );
                announcementRepository.saveAll(announcements);
            }
        }

        // One demo complaint, already assigned to the technician.
        if (issueRepository.count() == 0) {
            User resident = userRepository.findByUsername("resident").orElse(null);
            User technician = userRepository.findByUsername("technician").orElse(null);
            if (resident != null) {
                Issue issue = new Issue();
                issue.setTitle("Leaking tap in bathroom");
                issue.setDescription("The bathroom tap in A-101 keeps dripping all day.");
                issue.setStatus(technician != null ? IssueStatus.IN_PROGRESS : IssueStatus.OPEN);
                issue.setPriority(IssuePriority.MEDIUM);
                issue.setReportedById(resident.getId());
                if (technician != null) {
                    issue.setAssignedToId(technician.getId());
                }
                issue.setUnitNumber("A-101");
                issueRepository.save(issue);
            }
        }
    }

    private void createUserIfMissing(String username, String email, String password,
                                     String firstName, String lastName, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }

    private Unit createUnit(String unitNumber, int floor, String type, double area,
                            Long buildingId, double maintenance, String status, Long residentId) {
        Unit unit = new Unit();
        unit.setUnitNumber(unitNumber);
        unit.setFloorNumber(floor);
        unit.setUnitType(type);
        unit.setAreaSqft(area);
        unit.setBuildingId(buildingId);
        unit.setMonthlyMaintenance(maintenance);
        unit.setStatus(status);
        unit.setResidentId(residentId);
        return unit;
    }

    private Announcement createAnnouncement(String title, String content, Long buildingId,
                                            Long authorId, AnnouncementType type, int priority) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setBuildingId(buildingId);
        announcement.setAuthorId(authorId);
        announcement.setType(type);
        announcement.setPriority(priority);
        announcement.setIsPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());
        return announcement;
    }
}