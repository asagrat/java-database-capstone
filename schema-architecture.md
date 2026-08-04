Architecture summary

This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.




Numbered flow of data and control

1. User accesses AdminDashboard or Appointment pages.
2. The action is routed to the appropriate Thymeleaf or REST controller.
3. The controller calls the service layer which acts as the heart of the backend system: applies business rules and validations, coordinates workflows across multiple entities
4. The service layer communicates with the Repository Layer to perform data access operations with MySQL and MongoDB repositories
5. Database access where each repository interfaces directly with the underlying database engine: MySQL and MongoDB
6. Model binding layer is where once data is retrieved from the database, it is mapped into Java model classes that the application can work with
7. FInal layer is where bound models are used in the response layer: MVC or REST flows