## Design Approach

### Overview
This project uses a modular and maintainable design to ensure scalability, ease of testing, and clear separation of concerns.
Utilized the Cloudinary API ti upload/view/delete images

### Key Principles
- **Separation of Concerns:**  
  Different layers/components handle distinct responsibilities, such as controllers managing HTTP requests, services handling business logic, and repositories managing data access.

- **Security:**  
  Passwords are securely stored using `BCryptPasswordEncoder` to hash passwords before saving them, ensuring strong protection against attacks.
  OAuth2.0 and JWT tokens using googleapis to secure the API endpoints

- **Dependency Injection:**  
  The Spring Framework's DI mechanism is leveraged to manage component dependencies, promoting loose coupling and easier testing.

- **RESTful APIs:**  
  Endpoints follow REST principles, enabling easy integration with front-end applications or other services.
  Endpoints and their functionalities are documented using Swagger UI. They can be accessed using the endpoint /swagger-ui.html


### Components
- **Controller Layer:**  
  Handles incoming requests and sends appropriate responses.
  
  Controller -> 
  User Controller -> register, login and get user functionalities with success or failure responses
  Image Controller -> Upload, Delete, View Images with success or failure responses
  

- **Service Layer:**  
  Contains the core business logic, including password encoding and validation.
  Service ->
  User Service -> Business logic corresponding to the User Controller class methods
  Image Service -> Business logic corresponding to the Image Controller class methods
  Cloudinary Service -> Business logic corresponding to the Cloudinary API to integrate with Image Service class methods

- **Repository Layer:**  
  Interfaces with the database to perform CRUD operations.
  ImageRepository -> Jpa Repository to Store Image data.
  UserRepository -> Jpa Repository to store User data.

- **Configuration:**  
  Defines beans such as `BCryptPasswordEncoder` for password encryption.
  Security COnfiguration is implemented in SecurityConfig class. OAuth2 is implemented to secure the endpoints

### Example Workflow
1. A user sends a password via a REST endpoint.
2. The controller delegates to the service layer.
3. The service layer hashes the password using `BCryptPasswordEncoder`.
4. The hashed password is stored or used for validation.
5. On login, the service validates the raw password against the stored hash.

### Tools & Technologies
- Java 17+
- Spring Boot 3.4.6
- Spring Security (for password encoding)
- Maven build tool

Cloudinary API details
Register to https://cloudinary.com/
Login with the credentials used for creation of the account
Go to settings-> API keys 
Generate a new API key by registering your application name

Project Structure
ImageUploadApplication/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── synchrony/
│   │   │           └── cloudinary/
│   │   │               ├── controller/        # REST controllers (API endpoints)
│   │   │               ├── service/           # Business logic
│   │   │               ├── repository/        # Data access (JPA repositories)
│   │   │               ├── entity/             # Entity classes / domain models
│   │   │               ├── config/            # Configuration classes (e.g., security, beans)
│   │   │               └── ImageUploadApplication.java  # Main Spring Boot application
│   │   └── resources/
│   │       ├── application.properties          # App configuration
│   │       ├── static/                          # Static files (JS, CSS, images)
│   │     
│   └── test/
│       └── java/
│           └── com/
│               └── synchrony/
│                   └── cloudinary/
│                       ├── controller/          # Controller tests
│                       ├── service/             # Service tests
│                       └── repository/          # Repository tests
├── mvnw, mvnw.cmd                               # Maven wrapper scripts (if Maven project)
├── pom.xml or build.gradle                      # Build files
└── README.md                                    # Project documentation

Retrieve the API key, API secret and name and add the details in application.properties to be utilized in the application.

