package com.example.mailScheduler.service;

import com.example.mailScheduler.controller.EmailController;
import com.example.mailScheduler.model.*;
import com.example.mailScheduler.repository.*;
import jakarta.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ScheduledEmailRepository emailRepository;

    @Autowired
    private EmailSummaryRepository emailSummaryRepository;

    @Autowired
    private SentEmailsRepository sentEmailsRepository;

    @Autowired
    private FailedEmailsRepository failedEmailsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentNameRepository currentNameRepository;

    private static final String EMAIL_TEMPLATE = """
        <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Campus Placement Invitation - IIT Jodhpur</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            margin: 40px;
                            color: #333;
                        }
                        h1, h2 {
                            color: #2a4d69;
                        }
                        ul {
                            margin-top: 0;
                        }
                        .contact {
                            margin-top: 20px;
                        }
                        .signature {
                            margin-top: 40px;
                        }
                    </style>
                </head>
                <body>
            
                    <p>Dear {Salutation},</p>
            
                    <p>Greetings from <strong>IIT Jodhpur</strong>!</p>
            
                    <p>On behalf of the Office of Training and Placement Cell at the Indian Institute of Technology Jodhpur, I, <b>{Name}</b>, Internship Coordinator, take this opportunity to invite <b>{organization}</b> to participate in our campus placement and internship season for the {year} batches, respectively.</p>
            
                    <p>Since its inception in 2008, IIT Jodhpur has achieved several milestones and consistently strived for excellence across all domains. The institute hosts a large pool of talented students across various academic programs and recently secured the <b>{rank_number}th rank</b> in <b>NIRF {nirf_year}</b>.</p>
            
                    <p>IIT Jodhpur distinguishes itself with its <strong>Industry 4.0 curriculum</strong>, interdisciplinary project-based learning, and mandatory training in <strong>Machine Learning</strong> and <strong>Data Structures</strong> for all branches. Our students actively participate in various tech and non-tech clubs, ensuring holistic and industry-ready development.</p>
            
                    <h2>Why Collaborate with IIT Jodhpur?</h2>
                    <ul>
                        <li><strong>Qualified Talent Pool:</strong> Students undergo rigorous academic and practical training.</li>
                        <li><strong>Diverse Skill Sets:</strong> Programs offered include B.Tech, BS, M.Tech, M.Sc, MSR, Ph.D., M.Des, and dual degrees.</li>
                        <li><strong>Innovative Learning:</strong> Curriculum focuses on AI, IoT, Computational Sciences, and more.</li>
                        <li><strong>Interdisciplinary Projects:</strong> Students are well-prepared for real-world industrial challenges.</li>
                        <li><strong>Active Clubs:</strong> Clubs like Product, Quant, DevLup Labs, RAID, Robotics Society, and E-Cell encourage holistic development.</li>
                    </ul>
            
                    <h2>Key Achievements</h2>
                    <ul>
                        <li><strong>ISRO’s Inter-IIT Tech Meet 13.0:</strong> Bronze medal with top-10 ranks in lunar mapping, autonomous robots, and UAV fault recovery.</li>
                        <li><strong>ISRO-IRoC 2024:</strong> Ranked top 20 out of 2000+ teams with a custom rocker-bogie rover.</li>
                        <li><strong>Amazon ML Challenge 2024:</strong> M.Tech AI students secured 1st place, outperforming top institutes.</li>
                        <li><strong>Google Summer of Code:</strong> 5 students selected, showcasing global coding excellence.</li>
                    </ul>
            
                    <h2>Available Student Strength</h2>
                    <h3>Placements</h3>
                    <table border="1" cellpadding="8" cellspacing="0">
                        <tr>
                            <th>Program</th>
                            <th>Available Batch Strength</th>
                        </tr>
                        <tr><td>B.Tech / B.S</td><td>480</td></tr>
                        <tr><td>M.Tech</td><td>350</td></tr>
                        <tr><td>M.Sc</td><td>80</td></tr>
                        <tr><td>M.Sc-M.Tech</td><td>16</td></tr>
                        <tr><td>Tech MBA</td><td>83</td></tr>
                        <tr><td>M.Des</td><td>27</td></tr>
                        <tr><td>MSR</td><td>7</td></tr>
                    </table>
            
                    <h3>Internships</h3>
                    <table border="1" cellpadding="8" cellspacing="0">
                        <tr>
                            <th>Program</th>
                            <th>Available Batch Strength</th>
                        </tr>
                        <tr><td>B.Tech / B.S</td><td>507</td></tr>
                        <tr><td>Tech MBA</td><td>83</td></tr>
                    </table>
            
                    <p>
                    For more details, please find the\s
                    <a href="https://drive.google.com/file/d/1oxIjevmw4kIX51-c5l6HlrgAW1JieWiu/view" target="_blank">brochure</a>\s
                    attached. We invite you to consider our students for both <strong>technical</strong> and <strong>non-technical</strong> roles.\s
                    Kindly download, fill out, and return the\s
                    <a href="https://docs.google.com/document/d/1AS9WqmU4Bpky6Q4ox6TstXM4zS5WaP4V/edit?tab=t.0" target="_blank">Job (JAF)</a> /\s
                    <a href="https://docs.google.com/document/d/19kKzsWJ8n-lLlAUmjMvuQb6TWd95H-j-/edit?tab=t.0" target="_blank">Internship (IAF)</a>\s
                    Announcement Form to initiate the process.
                </p>
            
            
                    <p>We look forward to building a long-term relationship with your esteemed organization.</p>
            
                    <div class="signature">
                        <p>Warm Regards,</p>
                        <b>{Name}</b><br>
                        {Designation}<br>
                        Office of Training and Placement | IIT Jodhpur<br>
                        Contact : <b>+91 {phone_number}</b>
                                       </p>
            
                        <div class="contact">
                            <p><strong>Alternate Contact:</strong><br>
                            <strong>Puneet Garg</strong><br>
                            Training and Placement Officer<br>
                            Career Development Cell | IIT Jodhpur<br>
                            Contact : +91 9815964823 , 0291-2801155</p>
                        </div>
                    </div>
            
                </body>
                </html>
            
    """;


    public ResponseEntity<?> scheduleEmail(ScheduledEmail email) {

        // Validate recipient company name
        if (email.getCompany() == null || email.getCompany().isEmpty()) {
            // Store the failed email in the database with an error message
            email.setStatus("FAILED");
            email.setErrorMessage("Recipient company name is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Company name is required."));
        }

        // Validate user name
        if (email.getName() == null || email.getName().isEmpty()) {
            // Store the failed email in the database with an error message
            email.setStatus("FAILED");
            email.setErrorMessage("Your name is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Your name is required."));
        }

        // Validate rank
        if (email.getRank() == null) {
            // Store the failed email in the database with an error message
            email.setStatus("FAILED");
            email.setErrorMessage("NIRF Rank is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: NIRF Rank is required."));
        }

        // Validate Salutation
        if (email.getSalutation() == null || email.getSalutation().isEmpty()) {
            // Store the failed email in the database with an error message
            email.setStatus("FAILED");
            email.setErrorMessage("Salutation is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Salutation is required."));
        }

        // Validate NIRF Year
        if (email.getnirfYear() == null) {
            email.setStatus("FAILED");
            email.setErrorMessage("NIRF Year is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: NIRF Year is required."));
        }

        // Validate Designation
        if (email.getDesignation() == null || email.getDesignation().isEmpty()) {
            // Store the failed email in the database with an error message
            email.setStatus("FAILED");
            email.setErrorMessage("Designation is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Designation is required."));
        }

        // Validate Phone Number
        if (email.getPhone_Number() == null) {
            // Store the failed email in the database with an error message
            email.setStatus("FAILED");
            email.setErrorMessage("Phone Number is required.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Phone Number is required."));
        }

        // Validate year format (e.g., "2025-26")
        String yearRegex = "^(\\d{4})-(\\d{2})$";
        Pattern pattern = Pattern.compile(yearRegex);
        Matcher matcher = pattern.matcher(email.getYear());

        if (!matcher.matches()) {
            email.setStatus("FAILED");
            email.setErrorMessage("Invalid year format. Please use 'YYYY-YY' format.");
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: Invalid year format. Please use 'YYYY-YY' format."));
        }

        // Validate scheduled time: ensure it's in the future
        if (email.getScheduledTime() == null || email.getScheduledTime().isBefore(LocalDateTime.now())) {
            email.setStatus("FAILED");
            email.setErrorMessage("Schedule time must be in the future and cannot be null.");
            saveFailedEmail(email);
            // Return a JSON error response with the specific error message
            return ResponseEntity.badRequest().body(new ErrorResponse(email.getErrorMessage()));
        }

        // Validate all recipient email addresses
        String[] recipientList = email.getRecipient().split(",");
        List<String> invalidEmails = new ArrayList<>();
        for (String recipient : recipientList) {
            if (!isValidEmail(recipient.trim())) {
                invalidEmails.add(recipient.trim());
            }
        }

        if (!invalidEmails.isEmpty()) {
            email.setStatus("FAILED");
            email.setErrorMessage("Invalid email addresses: " + String.join(", ", invalidEmails));
            saveFailedEmail(email);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid email addresses: " + String.join(", ", invalidEmails)));
        }

        // If validation passes, set the status to "PENDING"
        email.setStatus("PENDING");
        CurrentName currentName = currentNameRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("CurrentName row not found"));
        email.setUsername(currentName.getUsername());

        ScheduledEmail savedEmail = emailRepository.save(email);

        try {
            // Schedule the email task for all recipients at the same time
            scheduleEmailTask(savedEmail, recipientList);
        } catch (Exception e) {
            savedEmail.setStatus("FAILED");
            savedEmail.setErrorMessage("Error scheduling email: " + e.getMessage());
            saveFailedEmail(email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error scheduling email: " + e.getMessage()));
        }

        // Ensure summary is updated
        updateEmailSummary();
        return ResponseEntity.ok(savedEmail); // Return the scheduled email response
    }

    //Modification in this is remaining
    public ResponseEntity<?> scheduleEmailsInBulk(MultipartFile file) {
        List<ScheduledEmail> scheduledEmails;
        List<String> errors = new ArrayList<>();

        try {
            scheduledEmails = parseExcelFile(file);

            for (ScheduledEmail email : scheduledEmails) {
                // Call the existing scheduleEmail method for each email
                ResponseEntity<?> response = scheduleEmail(email);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    errors.add("Failed to schedule email for recipient: " + email.getRecipient());
                }
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Some emails failed to schedule: " + String.join(", ", errors)));
            }

            return ResponseEntity.ok("Bulk emails scheduled successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Error processing the bulk email file: " + e.getMessage()));
        }
    }

    private List<ScheduledEmail> parseExcelFile(MultipartFile file) throws Exception {
        List<ScheduledEmail> emails = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        // Skip header row
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            ScheduledEmail email = new ScheduledEmail();
            email.setRecipient(row.getCell(0).getStringCellValue());
            email.setSalutation(row.getCell(1).getStringCellValue());
            email.setScheduledTime(row.getCell(2).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            email.setCompany(row.getCell(3).getStringCellValue());
            email.setRank((long) row.getCell(4).getNumericCellValue());
            email.setYear(row.getCell(5).getStringCellValue());
            email.setNirfYear((long) row.getCell(6).getNumericCellValue());
            String username = currentNameRepository.findUsernameById(1);
            User user = userRepository.findByUsername(username);
            email.setDesignation(user.getDesignation());
            email.setPhone_Number(user.getPhone_Number());
            email.setName(user.getName());
            emails.add(email);
        }

        return emails;
    }

    private void scheduleEmailTask(ScheduledEmail email, String[] recipients) {
        Runnable task = () -> sendEmail(email, recipients);
        Date scheduleDate = Date.from(email.getScheduledTime().atZone(ZoneId.systemDefault()).toInstant());
        taskScheduler.schedule(task, scheduleDate);
    }

    private void sendEmail(ScheduledEmail email, String[] recipients) {
        try {
            // Prepare the email content
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Prepare the email content
            String htmlContent = EMAIL_TEMPLATE.replace("{organization}", email.getCompany());
            String rank_number = Long.toString(email.getRank());
            String nirf_year = Long.toString(email.getnirfYear());  // Use getNIRF_Year() for correct value
            String phone = Long.toString(email.getPhone_Number());

            // Split the year into start year and end year
            String[] yearParts = email.getYear().split("-");
            String startYear = yearParts[0];  // "2025"
            String endYear = Integer.toString(Integer.parseInt(startYear) + 1);  // "2026"

            // Replace placeholders in the email content
            htmlContent = htmlContent.replace("{Salutation}", email.getSalutation());
            htmlContent = htmlContent.replace("{Name}", email.getName());
            htmlContent = htmlContent.replace("{rank_number}", rank_number);
            htmlContent = htmlContent.replace("{nirf_year}", nirf_year);
            htmlContent = htmlContent.replace("{Designation}", email.getDesignation());
            htmlContent = htmlContent.replace("{phone_number}", phone);
            htmlContent = htmlContent.replace("{year}", "<b>" + startYear + "</b> and <b>" + endYear + "</b>");

            // Send email to all recipients at once
            helper.setTo(recipients);
            String fixedSubject = "Placement and Internship Invite " + email.getYear() + " | IIT Jodhpur | " + email.getCompany();
            helper.setSubject(fixedSubject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);

            email.setStatus("SENT");
            email.setErrorMessage(null);

        } catch (Exception e) {
            email.setStatus("FAILED");
            email.setErrorMessage("Failed to send email: " + e.getMessage());
            saveFailedEmail(email);
            e.printStackTrace();
        }
        finally {
            sendTable(email); // Will store the email in sent table
            emailRepository.delete(email);
            updateEmailSummary();
        }
    }

    public void sendTable(ScheduledEmail email)
    {
        SentEmails sentEmails= new SentEmails();
        sentEmails.setRecipient(email.getRecipient());
        sentEmails.setScheduledTime(email.getScheduledTime());
        sentEmails.setStatus(email.getStatus());
        sentEmails.setCompany(email.getCompany());
        sentEmails.setDesignation(email.getDesignation());
        sentEmails.setnirfYear(email.getnirfYear());
        sentEmails.setName(email.getName());
        sentEmails.setPhone_Number(email.getPhone_Number());
        sentEmails.setSalutation(email.getSalutation());
        sentEmails.setYear(email.getYear());
        sentEmails.setRank(email.getRank());
        CurrentName currentName = currentNameRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("CurrentName row not found"));
        sentEmails.setUsername(currentName.getUsername());
        sentEmailsRepository.save(sentEmails);
        updateEmailSummary();
    }

    public void processMissedEmails() {
        List<ScheduledEmail> pendingEmails = emailRepository.findAll();

        // List to keep track of all errors encountered during processing
        List<String> errorMessages = new ArrayList<>();

        for (ScheduledEmail email : pendingEmails) {
            String[] recipients = email.getRecipient().split(",");

            try {
                if (email.getScheduledTime().isBefore(LocalDateTime.now())) {
                    // If the scheduled time is in the past, send the email to all recipients immediately
                    sendEmail(email, recipients);
                } else {
                    // If the scheduled time is in the future, re-schedule the email task for all recipients
                    scheduleEmailTask(email, recipients);
                }
            } catch (Exception e) {
                // Collect error message without failing immediately
                String errorMessage = "Error during processing email for recipients " + String.join(", ", recipients) + ": " + e.getMessage();
                errorMessages.add(errorMessage);
                email.setStatus("FAILED");
                email.setErrorMessage(errorMessage);
                saveFailedEmail(email);
            }
        }

        // Update email summary after processing all emails
        updateEmailSummary();

        // Log or return the error messages (optional)
        if (!errorMessages.isEmpty()) {
            // You can log the errors or handle them as needed
            String allErrors = String.join(", ", errorMessages);
            System.out.println("Errors during processing missed emails: " + allErrors);
        }
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email != null && email.matches(emailRegex);
    }

    public void updateEmailSummary() {
        // Fetch all sent emails
        List<SentEmails> sentEmails = sentEmailsRepository.findAll();
        long totalSent = 0;

        // Count the total number of recipients for all sent emails
        for (SentEmails email : sentEmails) {
            String[] recipientList = email.getRecipient().split(",");
            totalSent += recipientList.length; // Add the number of recipients for this email
        }

        // Fetch all failed emails
        List<FailedEmails> failedEmails = failedEmailsRepository.findAll();
        long totalFailed = 0;

        // Count the total number of recipients for all failed emails
        for (FailedEmails email : failedEmails) {
            String[] recipientList = email.getRecipient().split(",");
            totalFailed += recipientList.length; // Add the number of recipients for this email
        }

        // Fetch all scheduled emails
        List<ScheduledEmail> scheduledEmails = emailRepository.findAll();
        long totalScheduled = 0;

        // Count the total number of recipients for all scheduled emails
        for (ScheduledEmail email : scheduledEmails) {
            String[] recipientList = email.getRecipient().split(",");
            totalScheduled += recipientList.length; // Add the number of recipients for this email
        }

        // Update the summary table
        EmailSummary summary = emailSummaryRepository.findFirstByOrderById()
                .orElse(new EmailSummary());
        summary.setTotalSent(totalSent);
        summary.setTotalFailed(totalFailed);
        summary.setTotalScheduled(totalScheduled);

        // Update the 'lastUpdated' field to the current time
        summary.setLastUpdated(LocalDateTime.now());

        // Save the updated summary
        emailSummaryRepository.save(summary);
    }


    // Method to update the email summary after processing new email records


    public void saveFailedEmail(ScheduledEmail email)
    {
        FailedEmails failedEmails= new FailedEmails();
        CurrentName currentName = new CurrentName();
        EmailController emailController = new EmailController();
        failedEmails.setRecipient(email.getRecipient());
        failedEmails.setScheduledTime(email.getScheduledTime());
        failedEmails.setStatus(email.getStatus());
        failedEmails.setCompany(email.getCompany());
        failedEmails.setDesignation(email.getDesignation());
        failedEmails.setnirfYear(email.getnirfYear());
        failedEmails.setName(email.getName());
        failedEmails.setPhone_Number(email.getPhone_Number());
        failedEmails.setSalutation(email.getSalutation());
        failedEmails.setYear(email.getYear());
        failedEmails.setRank(email.getRank());
        failedEmails.setErrorMessage(email.getErrorMessage());
        CurrentName currentname = currentNameRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("CurrentName row not found"));
        failedEmails.setUsername(currentname.getUsername());
        failedEmailsRepository.save(failedEmails);


        emailRepository.delete(email);

        // Update email summary after saving a failed email
        updateEmailSummary();
    }
}