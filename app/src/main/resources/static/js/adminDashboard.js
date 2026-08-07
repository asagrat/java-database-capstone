// adminDashboard.js
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { openModal } from './components/modals.js';
import { createDoctorCard } from './components/doctorCard.js';

document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
  document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
  document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
});

async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Failed to load doctors:", error);
  }
}

async function filterDoctorsOnChange() {
  const name = document.getElementById("searchBar").value.trim() || null;
  const time = document.getElementById("filterTime").value || null;
  const specialty = document.getElementById("filterSpecialty").value || null;

  try {
    const response = await filterDoctors(name, time, specialty);
    const doctors = response.doctors || [];
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    if (doctors.length > 0) {
      doctors.forEach(doctor => contentDiv.appendChild(createDoctorCard(doctor)));
    } else {
      contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
    }
  } catch (error) {
    alert("Error filtering doctors.");
    console.error(error);
  }
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";
  doctors.forEach(doctor => contentDiv.appendChild(createDoctorCard(doctor)));
}

window.adminAddDoctor = async function () {
  const name = document.getElementById("doctorName").value;
  const email = document.getElementById("doctorEmail").value;
  const phone = document.getElementById("doctorPhone").value;
  const password = document.getElementById("doctorPassword").value;
  const specialty = document.getElementById("specialization").value;

  const checkboxes = document.querySelectorAll('input[name="availability"]:checked');
  const availableTimes = Array.from(checkboxes).map(cb => cb.value);

  const token = localStorage.getItem("token");
  if (\!token) {
    alert("No authentication token found. Please log in again.");
    return;
  }

  const doctor = { name, email, phone, password, specialty, availableTimes };

  const result = await saveDoctor(doctor, token);
  if (result.success) {
    alert("Doctor added successfully.");
    document.getElementById("modal").style.display = "none";
    location.reload();
  } else {
    alert(result.message || "Failed to add doctor.");
  }
};
