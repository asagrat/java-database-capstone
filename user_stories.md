## Admin User Stories


Title:
As an Admin, I want to create and manage doctor accounts, so that new staff members can access the system to provide care.

Acceptance Criteria:

1. Admin can input doctor details: Full Name, Email, Phone, Specialization, and License Number.
2. The system validates that the email address is unique and not already registered.
3. Upon successful creation, a system email is sent to the doctor with a temporary password or activation link.
4. Admin can view a list of all doctors with options to Disable/Enable accounts.
5. The system enforces role-based access, ensuring only Admins can modify doctor credentials.

Priority: High
Story Points: 5

Notes:
Use Spring Security to hash passwords.
Validation errors should display clearly in the UI.
Edge case: Handle duplicate license numbers if required by policy.


Title:
As an Admin, I want to manage medical services and pricing, so that patients can see available treatments and associated costs.

Acceptance Criteria:

1. Admin can Create, Read, Update, and Delete (CRUD) medical services (e.g., General Checkup, Dental Cleaning).
2. Each service must have a Name, Description, Duration (in minutes), and Price.
3. Admin can toggle the service status to "Active" or "Inactive".
4. Inactive services are hidden from the patient booking interface.
5. Changes to service details are reflected immediately in the database and API responses.

Priority: Medium
Story Points: 5

Notes:
Service ID should be auto-generated.
Consider adding an optional image URL field for the service catalog.


Title:
As an Admin, I want to view a dashboard with clinic statistics, so that I can monitor daily operations and resource usage.

Acceptance Criteria:

1. Dashboard displays "Total Appointments Today" and "Total Patients Registered".
2. Dashboard shows "Available Doctors" vs "Doctors on Leave".
3. Admin can filter statistics by date range (Today, This Week, This Month).
4. Data is retrieved efficiently using optimized database queries.
5. The dashboard loads within 2 seconds for standard data volumes.

Priority: Medium
Story Points: 8
Notes:
Use database aggregations (e.g., JPA countBy... or native queries) for performance.
Handle null values gracefully (e.g., display 0 if no data exists).


Title:
As an Admin, I want to cancel or reschedule appointments globally, so that I can resolve scheduling conflicts and manage clinic capacity.

Acceptance Criteria:

1. Admin can search appointments by Patient Name, Doctor Name, or Date.
2. Admin can cancel an appointment with a mandatory "Reason" field.
3. Upon cancellation, the patient receives an automated email notification.
4. The doctor's schedule is updated to reflect the cancelled slot.
5. The system logs the admin action for audit purposes (Who cancelled and when).

Priority: High
Story Points: 5
Notes:
Implement an Audit Log entity to record admin actions.
Ensure cancellation rules (e.g., cannot cancel past appointments) are enforced.


Title:
As an Admin, I want to configure clinic settings, so that the application reflects accurate operational information.

Acceptance Criteria:

1. Admin can update Clinic Name, Address, and Emergency Contact Number.
2. Admin can set standard Clinic Operating Hours (e.g., Mon-Fri 9 AM - 5 PM).
3. Admin can configure the maximum advance booking window (e.g., patients can book up to 30 days in advance).
4. Changes are saved and immediately affect the patient booking restrictions.
5. A "Reset to Defaults" option is available for testing purposes.

Priority: Low
Story Points: 3

Notes:
Store settings in a configuration table or properties file.
Operating hours should determine available time slots for doctors.





## Patient User Stories


Title:
As a Patient, I want to register and log in securely, so that I can access my health records and manage appointments.

Acceptance Criteria:

1. Patient registration form includes: Name, Email, Password, Phone, and Date of Birth.
2. The system validates email format and password strength (e.g., min 8 chars, special character).
3. Upon registration, the user is redirected to the login page.
4. Login requires valid credentials and supports session management or JWT token generation.
5. Patients cannot register with an email that is already associated with an existing account.

Priority: High
Story Points: 8

Notes:
Passwords must be encrypted (e.g., BCrypt) before storage.
Implement account lockout after multiple failed login attempts.


Title:
As a Patient, I want to book an appointment with a doctor, so that I can consult with a healthcare professional at a convenient time.

Acceptance Criteria:

1. Patient can select a Doctor based on Specialization.
2. Patient can view available time slots filtered by the doctor's schedule and existing appointments.
3. Patient selects a slot and confirms the appointment details.
4. The system prevents double-booking by locking the slot during selection.
5. Upon confirmation, the appointment is saved, and a confirmation email/SMS is triggered.

Priority: High
Story Points: 8

Notes:
Handle concurrency carefully; use database constraints or locking mechanisms for slot reservation.
Consider implementing a queue system if slots are shared.


Title:
As a Patient, I want to view my appointment history, so that I can track past visits and upcoming consultations.

Acceptance Criteria:

1. Patient can view a list of upcoming appointments sorted by date.
2. Patient can view a list of past appointments with status (Completed, Cancelled, No-Show).
3. Each appointment entry displays Doctor Name, Date, Time, and Status.
4. Patient can view detailed notes for past appointments if the doctor has added them.
5. The interface clearly distinguishes between "Upcoming" and "Past" tabs.

Priority: Medium
Story Points: 5

Notes:
API should return paginated results for performance.
Ensure patients can only see their own appointments (Authorization check).


Title:
As a Patient, I want to view my medical records and prescriptions, so that I can keep track of my health history and treatments.

Acceptance Criteria:

1. Patient can access a "My Records" section.
2. Records display Diagnosis, Medications, Dosage, and Doctor's Notes for completed appointments.
3. Patients can download prescriptions as a PDF (optional feature, mark as edge case).
4. Sensitive data is only visible to the patient and authorized doctors.
5. The view is read-only for patients; they cannot edit their records.

Priority: High
Story Points: 5

Notes:
Ensure HIPAA/GDPR compliance regarding data privacy.
PDF generation might require a library like Apache PDFBox.


Title:
As a Patient, I want to cancel or reschedule my appointment, so that I can manage my schedule effectively without administrative help.

Acceptance Criteria:

1. Patient can cancel an upcoming appointment up to 24 hours before the scheduled time.
2. If cancelling within 24 hours, a warning message is displayed (and cancellation may be blocked per policy).
3. Patient can reschedule, which redirects them to the booking flow for the same doctor.
4. Upon cancellation, the doctor's slot becomes available for other patients immediately.
5. The system sends a notification to the doctor upon cancellation.

Priority: Medium
Story Points: 3

Notes:
Business rule: Cancellation window is configurable by Admin.
Rescheduling should check for slot availability dynamically.






## Doctor User Stories


Title:
As a Doctor, I want to set my availability schedule, so that patients can book appointments during my working hours.

Acceptance Criteria:

1. Doctor can view a weekly calendar to set working hours.
2. Doctor can select specific days and set start/end times (e.g., Mon 9-12, 2-5).
3. Doctor can block specific dates for leave or unavailable periods.
4. The system calculates available slots based on appointment duration and breaks.
5. Changes to availability are saved and immediately update the booking interface for patients.

Priority: High
Story Points: 8

Notes:
Use a recurrence pattern or explicit date overrides for simplicity.
Ensure time zone handling is consistent (store in UTC, display in local).


Title:
As a Doctor, I want to view my daily appointment list, so that I can prepare for consultations and manage my workflow.

Acceptance Criteria:

1. Doctor can view a list of appointments for the current date by default.
2. Doctor can filter by patient name or status.
3. Each appointment card shows Patient Name, Age, Reason for Visit, and Status.
4. Doctor can quickly navigate to the next/previous day's appointments.
5. The list updates in real-time or refreshes when the doctor checks the page.

Priority: High
Story Points: 5

Notes:
Implement lazy loading if the list grows large.
Highlight urgent or flagged patients if such a feature exists.


Title:
As a Doctor, I want to add diagnosis and prescriptions to a patient record, so that patients receive formal treatment plans.

Acceptance Criteria:

1. Doctor can open a form for a specific completed appointment.
2. Doctor can input Diagnosis text, Prescriptions, and Medical Advice.
3. Prescription form allows adding multiple medications with Name, Dosage, and Frequency.
4. Saving the record updates the patient's medical history immediately.
5. Once saved, the record is locked and visible to the patient.

Priority: High
Story Points: 8

Notes:
Validate prescription fields to prevent empty entries.
Consider adding a "Signature" field for digital authorization.


Title:
As a Doctor, I want to update the status of appointments, so that the clinic and patients have accurate information about visit outcomes.

Acceptance Criteria:

1. Doctor can change appointment status to: "Completed", "Cancelled", or "No-Show".
2. Changing status to "Completed" triggers the record creation workflow.
3. Changing status to "Cancelled" or "No-Show" prompts for a reason.
4. The status change is logged with a timestamp.
5. The action cannot be performed on appointments from previous days without admin override.

Priority: Medium
Story Points: 3

Notes:
"No-Show" might impact patient rating or scheduling privileges in the future.
Ensure status transitions follow a logical flow (e.g., Scheduled -> Completed).


Title:
As a Doctor, I want to view detailed patient profiles, so that I can access medical history during consultations.

Acceptance Criteria:

1. Doctor can search for a patient by Name or ID.
2. Profile displays Patient Demographics, Allergies, and Chronic Conditions.
3. Doctor can view a timeline of previous appointments and diagnoses.
4. Doctor can see contact information for emergency purposes.
5. Access to patient profiles is audited to ensure privacy compliance.

Priority: Medium
Story Points: 5

Notes:
Search should be fuzzy (supports partial names).
Sensitive flags (e.g., HIV status, Mental Health) might require higher permissions; check with business rules.