# BloodLife - Blood Donation Management System (Work in Progress 🚧)

**BloodLife** is a multi-user desktop application designed to streamline the blood donation process through a **layered architecture**. The system provides a bridge between donors, medical professionals, and administrators to ensure efficient blood supply management. 

---

## 🎯 Project Scope
The application manages complex workflows including donor eligibility, real-time inventory tracking, and emergency alerting, all while ensuring data persistence through a robust **PostgreSQL** database.

## ✨ Key Features

### 1. Multi-User Account Management
* **Roles:** Separate interfaces and permissions for Donors, Medical Staff (Doctors), and Administrators.

### 2. Digital Pre-Eligibility Screening
* **Validation:** A mandatory digital questionnaire that automatically validates if a donor meets safety criteria (weight, time since last donation, recent surgeries) before allowing an appointment.

### 3. Smart Appointment Scheduling
* **Slot Management:** Donors can browse available centers and book time slots. The system manages capacity limits (e.g., max 5 donors/hour) to prevent overcrowding. 

### 4. Donation Process Control (Doctor Module)
* **Real-time Recording:** Doctors confirm attendance, record blood type (whole blood, plasma, or platelets), and the quantity collected. 
* **Traceability:** Every unit is assigned a unique tracking code linked to the donor.

### 5. Advanced Inventory Management
* **Live Stock Tracking:** Real-time visualization of blood units sorted by type (A, B, AB, 0) and Rh factor (+/-).
* **Status Monitoring:** Features to mark units as "Expired" or "Reserved" for specific emergency cases. 

### 6. Medical History & Results
* **Donor Dashboard:** Access to complete donation history and the ability to download blood analysis results as PDF files. 

### 7. Emergency Alert System
* **Critical Stock Alerts:** Administrators can issue urgent requests for specific blood types.
* **Smart Notifications:** Automated app or email notifications sent specifically to donors with the required blood type.

### 8. Analytics & Reporting
* **Visual Statistics:** Generation of charts showing monthly donor trends, center activity, and stock distribution to aid decision-making.

---

## 🤖 Meet "Bloodie" - Your AI Assistant
To improve user engagement and accessibility, the application features **Bloodie**, an integrated **AI Chatbot**. 
* **Purpose:** Bloodie assists users with real-time information about the donation process, eligibility rules, and center locations.
* **Goal:** To provide immediate guidance and encourage more people to become regular donors through interactive support.

---

## 🛠️ Technical Stack
* **Language:** Java (Object-Oriented Programming principles). 
* **Database:** PostgreSQL (SQL CRUD operations and secure data persistence).
* **Architecture:** Layered Architecture for clear separation of concerns (Business Logic, Data Access, and UI). 
