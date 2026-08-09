// doctorDashboard.js
import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

const tableBody = document.getElementById("patientTableBody");
let selectedDate = new Date().toISOString().split("T")[0];
const token = localStorage.getItem("token");
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
  renderContent();
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();

  document.getElementById("searchBar").addEventListener("input", () => {
    const value = document.getElementById("searchBar").value.trim();
    patientName = value.length > 0 ? value : "null";
    loadAppointments();
  });

  document.getElementById("todayBtn").addEventListener("click", () => {
    selectedDate = new Date().toISOString().split("T")[0];
    document.getElementById("datePicker").value = selectedDate;
    loadAppointments();
  });

  document.getElementById("datePicker").addEventListener("change", () => {
    selectedDate = document.getElementById("datePicker").value;
    loadAppointments();
  });
});

async function loadAppointments() {
  try {
    const appointments = await getAllAppointments(selectedDate, patientName || "null", token);
    tableBody.innerHTML = "";

    if (!appointments || appointments.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="5" class="noPatientRecord">No Appointments found for today.</td></tr>';
      return;
    }

    appointments.forEach(appt => {
      const patient = {
        id: appt.patientId,
        name: appt.patientName,
        phone: appt.patientPhone,
        email: appt.patientEmail
      };
      const row = createPatientRow(patient, appt.id, appt.doctorId);
      tableBody.appendChild(row);
    });
  } catch (error) {
    tableBody.innerHTML = '<tr><td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td></tr>';
    console.error("Error loading appointments:", error);
  }
}
