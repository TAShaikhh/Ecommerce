# Sequence Diagram: UC1.1 - User Sign-Up

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant Gateway
    participant IAM as IAM Service
    participant UserDB as User Database

    User->>Frontend: Click Sign-Up
    Frontend->>User: Display Sign-Up Form
    
    User->>Frontend: Enter Details<br/>(username, password,<br/>firstName, lastName,<br/>address)
    
    Frontend->>Gateway: POST /api/auth/signup<br/>(SignUpRequest)
    
    Gateway->>Gateway: validateSignUpData()
    
    Gateway->>IAM: createUser(UserDTO)
    
    IAM->>UserDB: checkUserExists(username)
    UserDB-->>IAM: false
    
    IAM->>IAM: hashPassword(password)
    
    IAM->>UserDB: INSERT INTO users<br/>VALUES(...)
    UserDB-->>IAM: userId
    
    IAM->>IAM: generateJWT(userId)
    
    IAM-->>Gateway: UserResponse<br/>(userId, token)
    
    Gateway-->>Frontend: 200 OK<br/>(SignUpResponse)
    
    Frontend->>Frontend: Store JWT Token<br/>in localStorage
    
    Frontend->>User: Display Success Message<br/>& Redirect to Dashboard
```

## Description
This sequence diagram illustrates the user registration process where the user provides registration information including username, password, name, and shipping address. The Gateway service validates the input, forwards the request to the IAM Service, which checks for duplicate usernames, hashes the password, and stores the user information in the User database. Upon successful registration, a JWT token is generated and returned to the user.
