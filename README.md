# EECS 4413 - Auction E-Commerce System
## Deliverable 1: Software Design Document

### Project Overview
This is the Software Design Document for an auction e-commerce system that implements forward auctions. The system allows sellers to list items for auction and buyers to bid on items in real-time.

### Team Information
- **Course:** EECS 4413 - Building e-Commerce Systems
- **Deliverable:** 1 of 3
- **Submission Date:** February 2026

### Deliverable Contents

#### Documentation
- **Software_Design_Document.md** - Complete SDD with all required sections:
  - Introduction (Purpose, Overview, References)
  - Major Design Decisions (Architecture patterns, modularization criteria)
  - Sequence Diagrams (4 use cases: UC1.1, UC1.2, UC2, UC3)
  - Activity Diagrams (4 use cases: UC4, UC5, UC6, UC7)
  - Architecture (Component diagrams, service descriptions, interface specifications)
  - Activities Plan (Product backlog, sprint backlog, GANTT diagram)
  - Group Meeting Logs
  - Test Driven Development (16 test cases)
  - Acknowledgment (AI use documentation)

### Architecture Summary

**System Type:** Microservices Architecture

**Key Services:**
1. **IAM Service** - User authentication and authorization
2. **Catalogue Service** - Item management and search
3. **Auction Service** - Bidding process and auction lifecycle
4. **Payment Service** - Payment processing and receipts
5. **Notification Service** - Email and real-time notifications
6. **Gateway Service** - API Gateway and request routing

**Technology Stack:**
- **Backend:** Spring Boot, Java
- **Frontend:** React.js, HTML5, CSS3
- **Database:** SQLite (separate DB per service)
- **Communication:** REST APIs, WebSocket, RabbitMQ/Kafka
- **Deployment:** Docker containers (Milestone 3)

### Use Cases Covered

1. **UC1.1:** User Sign-Up
2. **UC1.2:** User Sign-In
3. **UC2:** Browse Catalogue of Auctioned Items (Search, Display)
4. **UC3:** Bidding Process (Real-time bidding with validation)
5. **UC4:** Auction Ended (Winner determination, Pay Now)
6. **UC5:** Payment Process (Credit card payment)
7. **UC6:** Receipt and Shipment Details
8. **UC7:** Auction Item Upload (Seller functionality)

### Key Features

- **Real-time Bidding:** WebSocket-based real-time updates for all bidders
- **Concurrent Bid Handling:** Race condition prevention with database locking
- **Secure Authentication:** JWT-based authentication with password hashing
- **Flexible Shipping:** Standard and expedited shipping options
- **Comprehensive Testing:** 16 test cases covering all major functionality
- **Scalable Architecture:** Independent microservices that can scale separately

### Project Timeline

- **Deliverable 1 (Weeks 1-4):** Architecture & Design ✅
- **Deliverable 2 (Weeks 5-8):** Back-End Implementation
- **Deliverable 3 (Weeks 9-12):** Full System Integration & Deployment

### Design Principles

- **High Cohesion:** Each service has a focused, single responsibility
- **Low Coupling:** Services communicate through well-defined interfaces
- **Microservices Pattern:** Independent deployment and scaling
- **Database per Service:** Each service owns its data
- **API Gateway Pattern:** Single entry point for all client requests
- **Circuit Breaker Pattern:** Fault tolerance and graceful degradation

### Testing Strategy

- **Test-Driven Development (TDD):** Tests written before implementation
- **Unit Tests:** Individual service method testing
- **Integration Tests:** Service-to-service interaction testing
- **End-to-End Tests:** Complete user workflow testing
- **Performance Tests:** Concurrent bidding and load testing

### Next Steps

**Deliverable 2 Tasks:**
1. Implement all backend services (IAM, Catalogue, Auction, Payment)
2. Create database schemas in SQLite
3. Develop REST APIs for all service operations
4. Implement service-to-service communication
5. Create minimal UI for testing individual use cases
6. Write and execute unit tests for all services
7. Conduct integration testing

### References

- EECS 4413 Project Description Document v1.0
- EECS 4413 Deliverable 1 Instructions
- Martin Fowler - "Patterns of Enterprise Application Architecture"
- Chris Richardson - "Microservices Patterns"
- Spring Boot Documentation
- React.js Documentation

### Contact

For questions about this project, please refer to the course eClass page or contact your TA.

---

**Document Version:** 1.0  
**Last Updated:** February 4, 2026
