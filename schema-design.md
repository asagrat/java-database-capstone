# Smart Clinic Management System - Schema Design

## MySQL Database Design

Operational and strongly related data is stored in MySQL.

Design assumptions:
- Engine: InnoDB
- Character set: utf8mb4
- All timestamps are stored in UTC
- Hard deletes are avoided for core users; use `is_active` for soft-disable behavior

### Table: admins
```sql
CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: patients
```sql
CREATE TABLE patients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(20) NOT NULL UNIQUE,
  date_of_birth DATE NOT NULL,
  address VARCHAR(255) NULL,
  allergies TEXT NULL,
  chronic_conditions TEXT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: doctors
```sql
CREATE TABLE doctors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(20) NOT NULL UNIQUE,
  specialization VARCHAR(100) NOT NULL,
  license_number VARCHAR(50) NOT NULL UNIQUE,
  is_on_leave TINYINT(1) NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: doctor_availability
```sql
CREATE TABLE doctor_availability (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  day_of_week TINYINT NOT NULL, -- 1=Mon ... 7=Sun
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  slot_duration_minutes SMALLINT NOT NULL DEFAULT 30,
  is_available TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_availability_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
    ON DELETE CASCADE,

  CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
  CONSTRAINT chk_availability_time CHECK (end_time > start_time),
  CONSTRAINT chk_slot_duration CHECK (slot_duration_minutes IN (15, 20, 30, 45, 60)),

  UNIQUE KEY uq_doctor_schedule_window (doctor_id, day_of_week, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: appointments
```sql
CREATE TABLE appointments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  appointment_time DATETIME NOT NULL,
  duration_minutes SMALLINT NOT NULL DEFAULT 30,
  status TINYINT NOT NULL DEFAULT 0, -- 0=Scheduled,1=Completed,2=Cancelled,3=NoShow
  cancellation_reason VARCHAR(255) NULL,
  created_by_role ENUM('PATIENT','DOCTOR','ADMIN') NOT NULL DEFAULT 'PATIENT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_appointments_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
    ON DELETE RESTRICT,

  CONSTRAINT fk_appointments_patient
    FOREIGN KEY (patient_id) REFERENCES patients(id)
    ON DELETE RESTRICT,

  CONSTRAINT chk_appointment_status CHECK (status IN (0,1,2,3)),
  CONSTRAINT chk_appointment_duration CHECK (duration_minutes IN (15, 20, 30, 45, 60)),

  UNIQUE KEY uq_doctor_appointment_start (doctor_id, appointment_time),
  INDEX idx_appointments_patient_time (patient_id, appointment_time),
  INDEX idx_appointments_doctor_time (doctor_id, appointment_time),
  INDEX idx_appointments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: medical_services
```sql
CREATE TABLE medical_services (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL UNIQUE,
  description TEXT NULL,
  duration_minutes SMALLINT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT chk_service_duration CHECK (duration_minutes > 0),
  CONSTRAINT chk_service_price CHECK (price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: appointment_audit_logs
```sql
CREATE TABLE appointment_audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  appointment_id BIGINT NOT NULL,
  action_type ENUM('CREATE','RESCHEDULE','CANCEL','STATUS_CHANGE') NOT NULL,
  action_by_role ENUM('PATIENT','DOCTOR','ADMIN') NOT NULL,
  action_by_id BIGINT NOT NULL,
  old_status TINYINT NULL,
  new_status TINYINT NULL,
  reason VARCHAR(255) NULL,
  action_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_audit_appointment
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
    ON DELETE CASCADE,

  INDEX idx_audit_appointment_time (appointment_id, action_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Design notes:
- Patient/Doctor delete behavior is `RESTRICT` in `appointments` to preserve medical history.
- Double-booking prevention is enforced by `UNIQUE (doctor_id, appointment_time)`.
- Complex overlap checks (for variable durations) should be validated in service-layer code and, if needed, transaction-level locking.
- Email and phone formats can be validated in backend code (Bean Validation) before DB insert/update.

## MongoDB Collection Design

Flexible clinical notes and prescription details are stored in MongoDB.

### Collection: prescriptions

```json
{
  "_id": "ObjectId('66af0d9f2b8f7f17f2d013a1')",
  "appointmentId": 1025,
  "patientId": 210,
  "doctorId": 43,
  "patientNameSnapshot": "Rita Gomez",
  "doctorNameSnapshot": "Dr. Alex Chen",
  "diagnosis": "Acute sinusitis",
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "3 times/day",
      "durationDays": 7,
      "instructions": "After meals"
    },
    {
      "name": "Cetirizine",
      "dosage": "10mg",
      "frequency": "1 time/day",
      "durationDays": 5,
      "instructions": "At bedtime"
    }
  ],
  "doctorNotes": "Hydrate well. Follow up if fever persists after 48 hours.",
  "tags": ["sinus", "infection", "follow-up"],
  "attachments": [
    {
      "type": "lab-report",
      "fileName": "cbc-2026-08-05.pdf",
      "url": "https://files.smartclinic.example/reports/cbc-2026-08-05.pdf"
    }
  ],
  "metadata": {
    "source": "doctor-dashboard",
    "version": 1,
    "lastEditedBy": "doctor:43"
  },
  "createdAt": "2026-08-05T10:15:23Z",
  "updatedAt": "2026-08-05T10:16:10Z"
}
```

Design notes:
- Keep relational truth in MySQL using IDs (`appointmentId`, `patientId`, `doctorId`).
- Keep human-readable snapshots (`patientNameSnapshot`, `doctorNameSnapshot`) for historical readability even if profile names later change.
- This structure supports schema evolution (new optional fields can be added without migration-heavy DDL changes).
