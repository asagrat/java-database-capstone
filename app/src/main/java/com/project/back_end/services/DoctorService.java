package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> opt = doctorRepository.findById(doctorId);
        if (opt.isEmpty()) return new ArrayList<>();

        Doctor doctor = opt.get();
        // Extract start time from each range slot (e.g. "09:00-10:00" → "09:00")
        List<String> allSlots = doctor.getAvailableTimes().stream()
                .map(t -> { int d = t.indexOf('-'); return d > 0 ? t.substring(0, d).trim() : t.trim(); })
                .collect(Collectors.toList());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        List<Appointment> booked = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        List<String> bookedTimes = booked.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toList());

        allSlots.removeAll(bookedTimes);
        return allSlots;
    }

    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        doctors.forEach(d -> d.getAvailableTimes().size()); // force load
        return doctors;
    }

    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getEmail());
            if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(doctor.getEmail());
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Login failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike("%" + name + "%");
        doctors.forEach(d -> d.getAvailableTimes().size());
        result.put("doctors", doctors);
        return result;
    }

    @Transactional
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    @Transactional
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike("%" + name + "%");
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    @Transactional
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        result.put("doctors", doctors);
        return result;
    }

    @Transactional
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    @Transactional
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        result.put("doctors", doctors);
        return result;
    }

    @Transactional
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findAll();
        result.put("doctors", filterDoctorByTime(doctors, amOrPm));
        return result;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        boolean isAmPm = "AM".equalsIgnoreCase(amOrPm) || "PM".equalsIgnoreCase(amOrPm);
        return doctors.stream().filter(doctor -> {
            doctor.getAvailableTimes().size(); // force load
            return doctor.getAvailableTimes().stream().anyMatch(slot -> {
                try {
                    if (isAmPm) {
                        // slots are stored as ranges; parse only the start
                        String start = slot.contains("-") ? slot.split("-")[0].trim() : slot.trim();
                        LocalTime time = LocalTime.parse(start);
                        return "AM".equalsIgnoreCase(amOrPm) ? time.getHour() < 12 : time.getHour() >= 12;
                    } else {
                        // exact slot match, e.g. "09:00-10:00"
                        return slot.equalsIgnoreCase(amOrPm);
                    }
                } catch (Exception e) {
                    return false;
                }
            });
        }).collect(Collectors.toList());
    }
}
