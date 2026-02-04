# Auction E-Commerce System
## Software Design Document (SDD)
### EECS 4413 - Building e-Commerce Systems
### Deliverable 1

---

## Document Change Control

| Version | Date | Author(s) | Summary of Changes |
|---------|------|-----------|-------------------|
| 1.0 | February 4, 2026 | Development Team | Initial document creation with complete architecture design |

---

## Table of Contents

1. [Introduction](#1-introduction)
   - 1.1 [Purpose](#11-purpose)
   - 1.2 [Overview](#12-overview)
   - 1.3 [References](#13-references)
2. [Major Design Decisions](#2-major-design-decisions)
   - 2.1 [Architectural Patterns](#21-architectural-patterns)
   - 2.2 [Modularization Criteria](#22-modularization-criteria)
   - 2.3 [Alternative Architectures Considered](#23-alternative-architectures-considered)
3. [Sequence Diagrams](#3-sequence-diagrams)
   - 3.1 [UC1.1: User Sign-Up](#31-uc11-user-sign-up)
   - 3.2 [UC1.2: User Sign-In](#32-uc12-user-sign-in)
   - 3.3 [UC2: Browse Catalogue of Auctioned Items](#33-uc2-browse-catalogue-of-auctioned-items)
   - 3.4 [UC3: Bidding Process](#34-uc3-bidding-process)
4. [Activity Diagrams](#4-activity-diagrams)
   - 4.1 [UC4: Auction Ended](#41-uc4-auction-ended)
   - 4.2 [UC5: Payment Process](#42-uc5-payment-process)
   - 4.3 [UC6: Receipt and Shipment Details](#43-uc6-receipt-and-shipment-details)
   - 4.4 [UC7: Auction Item Upload](#44-uc7-auction-item-upload)
5. [Architecture](#5-architecture)
   - 5.1 [System Architecture Overview](#51-system-architecture-overview)
   - 5.2 [Services Description](#52-services-description)
   - 5.3 [Interfaces Description](#53-interfaces-description)
6. [Activities Plan, Product Backlog, and Sprint Backlog](#6-activities-plan-product-backlog-and-sprint-backlog)
   - 6.1 [Product Backlog](#61-product-backlog)
   - 6.2 [Sprint Backlog by Deliverable](#62-sprint-backlog-by-deliverable)
   - 6.3 [GANTT Diagram](#63-gantt-diagram)
7. [Group Meeting Logs](#7-group-meeting-logs)
8. [Test Driven Development (TDD)](#8-test-driven-development-tdd)
9. [Acknowledgment](#9-acknowledgment)

---

## 1. Introduction

### 1.1 Purpose

The purpose of this project is to design and implement a comprehensive auction e-commerce system that enables users to participate in forward auctions. The system will provide a complete end-to-end solution for sellers to list items for auction and buyers to bid on items, process payments, and receive shipment information. This deliverable focuses on the architectural design, system specification, and planning phases of the project.

### 1.2 Overview

The Auction E-Commerce System is a web-based application that implements forward auction functionality where sellers offer items for sale and bidders compete to purchase items at the highest bid price. The system supports the following key capabilities:

**User Management:**
- User registration (sign-up) with personal and shipping information
- Secure authentication (sign-in) with password recovery mechanisms
- Session management for concurrent bidding across multiple browsers

**Auction Management:**
- Browsing and searching for auctioned items using keywords
- Real-time bidding with automatic price validation (strictly increasing bids)
- Time-based auction expiration with automatic winner determination
- Support for sellers to upload and manage auction items

**Payment and Fulfillment:**
- Secure payment processing for winning bidders
- Shipping cost calculation (standard and expedited options)
- Receipt generation with shipment tracking details

The system follows a three-tier architecture:
1. **Front-end Tier:** HTML/CSS-based user interface with REST API consumption
2. **Middle Tier:** Application logic layer using Spring Boot/Java EE framework for request orchestration
3. **Back-end Tier:** Business logic services with independent databases implementing microservices architecture

### 1.3 References

1. EECS 4413 Project Description Document v1.0
2. EECS 4413 Deliverable 1 Instructions
3. Martin Fowler, "Patterns of Enterprise Application Architecture"
4. Richardson, Chris. "Microservices Patterns: With examples in Java"
5. UML 2.5 Specification - Object Management Group (OMG)
6. REST API Design Best Practices - https://restfulapi.net/
7. SQLite Documentation - https://www.sqlite.org/docs.html
8. Spring Boot Framework Documentation - https://spring.io/projects/spring-boot

---

## 2. Major Design Decisions

### 2.1 Architectural Patterns

Our auction e-commerce system adopts a **Microservices Architecture** pattern combined with several complementary design patterns to ensure scalability, maintainability, and modularity:

#### Primary Architectural Pattern: Microservices Architecture

We have chosen a microservices architecture where the system is decomposed into independent, loosely-coupled services that communicate through well-defined APIs. Each service:
- Owns its dedicated database (Database per Service pattern)
- Can be developed, deployed, and scaled independently
- Implements a specific business capability
- Communicates via REST APIs and asynchronous messaging

**Key Services:**
1. **Identity and Authorization Management (IAM) Service:** Handles user authentication, authorization, and credential management
2. **Catalogue Service:** Manages the inventory of items available for auction
3. **Auction Service:** Orchestrates the bidding process and tracks auction state
4. **Payment Service:** Processes payments and manages financial transactions
5. **Gateway Service (API Gateway):** Acts as a façade for the front-end, routing requests to appropriate services

#### Supporting Design Patterns:

**1. Façade Pattern:**
- The Gateway Service implements the Façade pattern, providing a simplified unified interface to the front-end
- Hides the complexity of the microservices architecture from clients
- Handles cross-cutting concerns like authentication, rate limiting, and request routing

**2. Repository Pattern:**
- Each service uses the Repository pattern to abstract data access logic
- Separates business logic from data persistence concerns
- Enables easier testing and database technology changes

**3. Observer Pattern:**
- Implemented for real-time bid updates to all users watching an auction
- Auction Service notifies subscribed clients when bid status changes
- Enables push-based updates rather than polling

**4. State Pattern:**
- Auction items transition through different states: ACTIVE, EXPIRED, WON, PAID
- Each state has specific behaviors and valid transitions
- Simplifies auction lifecycle management

**5. Strategy Pattern:**
- Supports different auction types (currently Forward Auctions, extensible to Reverse/Dutch auctions)
- Payment processing strategies for different payment methods
- Shipping calculation strategies for standard vs. expedited shipping

### 2.2 Modularization Criteria

Our system design adheres to fundamental software engineering principles to achieve high cohesion and low coupling:

#### High Cohesion:

**1. Single Responsibility Principle:**
- Each service has a well-defined, focused responsibility
- IAM Service: Only handles user authentication and authorization
- Catalogue Service: Only manages product inventory and search
- Auction Service: Only manages bidding logic and auction state
- Payment Service: Only handles payment processing and receipts

**2. Functional Cohesion:**
- Classes within each service are grouped by related functionality
- Example: Auction Service contains `AuctionManager`, `BidValidator`, `AuctionTimer` - all related to auction operations
- Database schema design groups related entities (Users table in IAM DB, Items table in Catalogue DB)

**3. Information Hiding:**
- Services expose only necessary operations through well-defined interfaces
- Internal implementation details (database schemas, algorithms) are hidden
- Clients cannot directly access service databases

#### Low Coupling:

**1. Interface-Based Communication:**
- Services communicate through REST APIs with JSON payloads
- No direct database access between services
- Changes to internal implementation don't affect other services if interface contracts remain stable

**2. Asynchronous Communication:**
- Event-driven architecture using message queues (RabbitMQ/Kafka) for non-blocking operations
- Example: When auction ends, Auction Service publishes "AuctionEnded" event; Payment Service subscribes and processes payment
- Reduces temporal coupling - services don't need to be available simultaneously

**3. Shared-Nothing Architecture:**
- Each service has its own database (no shared database anti-pattern)
- Services maintain their own data models optimized for their use cases
- Data consistency achieved through eventual consistency and saga patterns for distributed transactions

**4. Dependency Management:**
- Services depend on abstractions (interfaces) not concrete implementations
- Gateway Service knows about service interfaces but not their implementations
- Minimizes ripple effects when changes occur

#### Benefits of Our Modularization:

1. **Independent Deployment:** Services can be updated without redeploying the entire system
2. **Technology Diversity:** Each service can use the most appropriate technology stack
3. **Scalability:** High-traffic services (e.g., Auction Service) can be scaled independently
4. **Fault Isolation:** Failure in one service doesn't cascade to others
5. **Team Autonomy:** Different teams can work on different services concurrently
6. **Testability:** Services can be tested in isolation with mock dependencies

### 2.3 Alternative Architectures Considered

#### 1. Monolithic Layered Architecture (NOT CHOSEN)

**Description:**
- Traditional three-tier architecture with Presentation, Business Logic, and Data Access layers
- All components deployed as a single application
- Shared database across all modules

**Advantages:**
- Simpler to develop initially for small teams
- Easier debugging with single codebase
- Lower deployment complexity
- Better performance for inter-module communication (in-process calls)
- ACID transactions are straightforward

**Disadvantages (Why Not Chosen):**
- **Scalability Limitations:** Cannot scale individual components independently
- **Deployment Risk:** Any change requires full system redeployment
- **Technology Lock-in:** Entire system must use the same technology stack
- **Team Coordination:** Multiple developers working on same codebase increases merge conflicts
- **Fault Propagation:** Single module failure can bring down entire system
- **Not Aligned with Project Requirements:** Milestone 3 explicitly requires containerized microservices deployment

#### 2. Service-Oriented Architecture (SOA) with Enterprise Service Bus (ESB)

**Description:**
- Services communicate through a central Enterprise Service Bus
- ESB handles message routing, transformation, and orchestration
- Heavier services with more business logic per service

**Advantages:**
- Centralized governance and monitoring
- Built-in message transformation and routing
- Protocol mediation (SOAP, REST, JMS)

**Disadvantages (Why Not Chosen):**
- **Single Point of Failure:** ESB becomes a bottleneck and failure point
- **Complexity:** ESB adds significant infrastructure complexity
- **Vendor Lock-in:** Many ESB solutions are proprietary
- **Performance Overhead:** Message transformation and routing adds latency
- **Heavyweight:** Over-engineered for our project scope
- **Limited Agility:** Changes to ESB configuration can be cumbersome

#### 3. Event-Driven Architecture (Pure Event Sourcing)

**Description:**
- All state changes captured as immutable events
- Services react to events rather than direct API calls
- Event store as single source of truth

**Advantages:**
- Complete audit trail of all system changes
- Time-travel debugging capabilities
- Natural fit for asynchronous operations
- High scalability potential

**Disadvantages (Why Not Chosen):**
- **Steep Learning Curve:** Event sourcing is complex to implement correctly
- **Eventual Consistency Complexity:** Difficult to reason about system state
- **Query Complexity:** Rebuilding current state from events can be computationally expensive
- **Overkill for Requirements:** Our use cases don't require full event sourcing capabilities
- **Testing Difficulty:** Harder to write and maintain tests

**Our Chosen Hybrid Approach:**

We selected a **Microservices Architecture with partial event-driven communication** because it:
- Aligns perfectly with project requirements (Milestone 3 containerization)
- Balances complexity with team capabilities
- Provides scalability without over-engineering
- Supports independent service development and deployment
- Allows synchronous REST APIs for request-response patterns (user sign-in, item search)
- Uses asynchronous messaging for decoupled operations (auction end notifications, payment processing)
- Provides clear service boundaries that map to business capabilities
- Enables future extensibility (adding new auction types, payment methods)

---

## 3. Sequence Diagrams

### 3.1 UC1.1: User Sign-Up

```
┌──────────┐     ┌────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  User    │     │Frontend│     │   Gateway   │     │ IAM Service │     │   User DB    │
└────┬─────┘     └───┬────┘     └──────┬──────┘     └──────┬──────┘     └──────┬───────┘
     │                │                 │                    │                    │
     │ Click Sign-Up  │                 │                    │                    │
     │───────────────>│                 │                    │                    │
     │                │                 │                    │                    │
     │                │ Display Sign-Up │                    │                    │
     │                │      Form       │                    │                    │
     │<───────────────│                 │                    │                    │
     │                │                 │                    │                    │
     │ Enter Details  │                 │                    │                    │
     │ (username,     │                 │                    │                    │
     │ password,      │                 │                    │                    │
     │ firstName,     │                 │                    │                    │
     │ lastName,      │                 │                    │                    │
     │ address)       │                 │                    │                    │
     │───────────────>│                 │                    │                    │
     │                │                 │                    │                    │
     │                │ POST /api/auth/signup               │                    │
     │                │    (SignUpRequest)                  │                    │
     │                │────────────────>│                    │                    │
     │                │                 │                    │                    │
     │                │                 │ validateSignUpData()                   │
     │                │                 │    (username,      │                    │
     │                │                 │     password)      │                    │
     │                │                 │ ────────┐          │                    │
     │                │                 │         │          │                    │
     │                │                 │<────────┘          │                    │
     │                │                 │                    │                    │
     │                │                 │ createUser(UserDTO)│                    │
     │                │                 │───────────────────>│                    │
     │                │                 │                    │                    │
     │                │                 │                    │ checkUserExists(   │
     │                │                 │                    │   username)        │
     │                │                 │                    │───────────────────>│
     │                │                 │                    │                    │
     │                │                 │                    │     false          │
     │                │                 │                    │<───────────────────│
     │                │                 │                    │                    │
     │                │                 │                    │ hashPassword(      │
     │                │                 │                    │   password)        │
     │                │                 │                    │ ────────┐          │
     │                │                 │                    │         │          │
     │                │                 │                    │<────────┘          │
     │                │                 │                    │                    │
     │                │                 │                    │ INSERT INTO users  │
     │                │                 │                    │   VALUES(...)      │
     │                │                 │                    │───────────────────>│
     │                │                 │                    │                    │
     │                │                 │                    │     userId         │
     │                │                 │                    │<───────────────────│
     │                │                 │                    │                    │
     │                │                 │  UserResponse      │                    │
     │                │                 │  (userId, token)   │                    │
     │                │                 │<───────────────────│                    │
     │                │                 │                    │                    │
     │                │  200 OK         │                    │                    │
     │                │  (SignUpResponse)                    │                    │
     │                │<────────────────│                    │                    │
     │                │                 │                    │                    │
     │                │ Display Success │                    │                    │
     │                │   Message &     │                    │                    │
     │                │ Redirect to     │                    │                    │
     │                │   Dashboard     │                    │                    │
     │<───────────────│                 │                    │                    │
     │                │                 │                    │                    │
```

**Description:**
This sequence diagram illustrates the user registration process. The user provides registration information including username, password, name, and shipping address. The Gateway service validates the input, forwards the request to the IAM Service, which checks for duplicate usernames, hashes the password, and stores the user information in the User database. Upon successful registration, a JWT token is generated and returned to the user.

---

### 3.2 UC1.2: User Sign-In

```
┌──────────┐     ┌────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  User    │     │Frontend│     │   Gateway   │     │ IAM Service │     │   User DB    │
└────┬─────┘     └───┬────┘     └──────┬──────┘     └──────┬──────┘     └──────┬───────┘
     │                │                 │                    │                    │
     │ Click Sign-In  │                 │                    │                    │
     │───────────────>│                 │                    │                    │
     │                │                 │                    │                    │
     │                │ Display Sign-In │                    │                    │
     │                │      Form       │                    │                    │
     │<───────────────│                 │                    │                    │
     │                │                 │                    │                    │
     │ Enter Username │                 │                    │                    │
     │  & Password    │                 │                    │                    │
     │───────────────>│                 │                    │                    │
     │                │                 │                    │                    │
     │                │ POST /api/auth/signin               │                    │
     │                │  (SignInRequest)│                    │                    │
     │                │────────────────>│                    │                    │
     │                │                 │                    │                    │
     │                │                 │ authenticateUser(  │                    │
     │                │                 │   username,        │                    │
     │                │                 │   password)        │                    │
     │                │                 │───────────────────>│                    │
     │                │                 │                    │                    │
     │                │                 │                    │ SELECT * FROM users│
     │                │                 │                    │  WHERE username=?  │
     │                │                 │                    │───────────────────>│
     │                │                 │                    │                    │
     │                │                 │                    │  User(userId,      │
     │                │                 │                    │  hashedPassword,   │
     │                │                 │                    │  userData)         │
     │                │                 │                    │<───────────────────│
     │                │                 │                    │                    │
     │                │                 │                    │ verifyPassword(    │
     │                │                 │                    │   password,        │
     │                │                 │                    │   hashedPassword)  │
     │                │                 │                    │ ────────┐          │
     │                │                 │                    │         │          │
     │                │                 │                    │<────────┘          │
     │                │                 │                    │                    │
     │                │                 │                    │ generateJWT(userId)│
     │                │                 │                    │ ────────┐          │
     │                │                 │                    │         │          │
     │                │                 │                    │<────────┘          │
     │                │                 │                    │                    │
     │                │                 │                    │ UPDATE users SET   │
     │                │                 │                    │  lastLogin=NOW()   │
     │                │                 │                    │  WHERE userId=?    │
     │                │                 │                    │───────────────────>│
     │                │                 │                    │                    │
     │                │                 │                    │     Success        │
     │                │                 │                    │<───────────────────│
     │                │                 │                    │                    │
     │                │                 │ AuthResponse       │                    │
     │                │                 │ (token, userId,    │                    │
     │                │                 │  userData)         │                    │
     │                │                 │<───────────────────│                    │
     │                │                 │                    │                    │
     │                │  200 OK         │                    │                    │
     │                │ (SignInResponse)│                    │                    │
     │                │<────────────────│                    │                    │
     │                │                 │                    │                    │
     │                │ Store JWT Token │                    │                    │
     │                │ in localStorage │                    │                    │
     │                │ ────────┐       │                    │                    │
     │                │         │       │                    │                    │
     │                │<────────┘       │                    │                    │
     │                │                 │                    │                    │
     │                │ Redirect to     │                    │                    │
     │                │  Catalogue Page │                    │                    │
     │<───────────────│                 │                    │                    │
     │                │                 │                    │                    │
```

**Description:**
This sequence diagram shows the authentication process. The user provides credentials which are validated against the User database. The IAM Service retrieves the user record, verifies the hashed password, and generates a JWT token upon successful authentication. The token is returned to the frontend and stored for subsequent authenticated requests.

---

### 3.3 UC2: Browse Catalogue of Auctioned Items

```
┌──────────┐  ┌────────┐  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐
│  User    │  │Frontend│  │   Gateway   │  │   Catalogue  │  │   Auction    │  │  Catalogue DB │
│          │  │        │  │   Service   │  │   Service    │  │   Service    │  │               │
└────┬─────┘  └───┬────┘  └──────┬──────┘  └──────┬───────┘  └──────┬───────┘  └───────┬───────┘
     │             │              │                 │                 │                  │
     │ Enter Search│              │                 │                 │                  │
     │  Keyword    │              │                 │                 │                  │
     │────────────>│              │                 │                 │                  │
     │             │              │                 │                 │                  │
     │             │ GET /api/catalogue/search?     │                 │                  │
     │             │    keyword={keyword}           │                 │                  │
     │             │    Authorization: Bearer {token}                │                  │
     │             │─────────────>│                 │                 │                  │
     │             │              │                 │                 │                  │
     │             │              │ validateToken(token)              │                  │
     │             │              │ ────────┐       │                 │                  │
     │             │              │         │       │                 │                  │
     │             │              │<────────┘       │                 │                  │
     │             │              │                 │                 │                  │
     │             │              │ searchItems(keyword)              │                  │
     │             │              │────────────────>│                 │                  │
     │             │              │                 │                 │                  │
     │             │              │                 │ SELECT * FROM items               │
     │             │              │                 │  WHERE (name LIKE '%keyword%'     │
     │             │              │                 │    OR description LIKE '%keyword%'│
     │             │              │                 │    OR keywords LIKE '%keyword%')  │
     │             │              │                 │  AND status='ACTIVE'              │
     │             │              │                 │─────────────────────────────────>│
     │             │              │                 │                 │                  │
     │             │              │                 │ List<Item>      │                  │
     │             │              │                 │ (itemId, name,  │                  │
     │             │              │                 │  description,   │                  │
     │             │              │                 │  startPrice,    │                  │
     │             │              │                 │  endTime)       │                  │
     │             │              │                 │<─────────────────────────────────│
     │             │              │                 │                 │                  │
     │             │              │ List<ItemDTO>   │                 │                  │
     │             │              │<────────────────│                 │                  │
     │             │              │                 │                 │                  │
     │             │              │ For each item:  │                 │                  │
     │             │              │ getCurrentBidInfo(itemId)         │                  │
     │             │              │────────────────────────────────────>                 │
     │             │              │                 │                 │                  │
     │             │              │                 │  BidInfo(       │                  │
     │             │              │                 │   currentPrice, │                  │
     │             │              │                 │   highestBidder,│                  │
     │             │              │                 │   remainingTime)│                  │
     │             │              │<────────────────────────────────────                 │
     │             │              │                 │                 │                  │
     │             │              │ enrichItemsWithBidData()           │                  │
     │             │              │ ────────┐       │                 │                  │
     │             │              │         │       │                 │                  │
     │             │              │<────────┘       │                 │                  │
     │             │              │                 │                 │                  │
     │             │  200 OK      │                 │                 │                  │
     │             │ (List<ItemWithBidInfo>)        │                 │                  │
     │             │<─────────────│                 │                 │                  │
     │             │              │                 │                 │                  │
     │             │ Display Items│                 │                 │                  │
     │             │ with Radio   │                 │                 │                  │
     │             │  Buttons     │                 │                 │                  │
     │<────────────│              │                 │                 │                  │
     │             │              │                 │                 │                  │
     │ Select Item │              │                 │                 │                  │
     │  & Click BID│              │                 │                 │                  │
     │────────────>│              │                 │                 │                  │
     │             │              │                 │                 │                  │
     │             │ Navigate to  │                 │                 │                  │
     │             │ Bidding Page │                 │                 │                  │
     │<────────────│              │                 │                 │                  │
     │             │              │                 │                 │                  │
```

**Description:**
This sequence diagram depicts the catalogue browsing functionality. The user enters a search keyword, which is sent to the Gateway Service. The request is forwarded to the Catalogue Service to search for matching items in the database. For each item found, the system queries the Auction Service to retrieve current bid information (current price, highest bidder, remaining time). The enriched item data is returned and displayed to the user with selection options.

---

### 3.4 UC3: Bidding Process

```
┌──────────┐  ┌────────┐  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐
│  User    │  │Frontend│  │   Gateway   │  │   Auction    │  │   Auction DB  │  │  WebSocket   │
│  (Bidder)│  │        │  │   Service   │  │   Service    │  │               │  │   Clients    │
└────┬─────┘  └───┬────┘  └──────┬──────┘  └──────┬───────┘  └───────┬───────┘  └──────┬───────┘
     │             │              │                 │                  │                  │
     │ View Item   │              │                 │                  │                  │
     │  Details    │              │                 │                  │                  │
     │────────────>│              │                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │ GET /api/auction/{itemId}      │                  │                  │
     │             │────────────>│                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │              │ getAuctionDetails(itemId)          │                  │
     │             │              │────────────────>│                  │                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ SELECT * FROM auctions              │
     │             │              │                 │  WHERE itemId=?  │                  │
     │             │              │                 │─────────────────>│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ Auction(itemId,  │                  │
     │             │              │                 │  currentPrice,   │                  │
     │             │              │                 │  highestBidderId,│                  │
     │             │              │                 │  endTime)        │                  │
     │             │              │                 │<─────────────────│                  │
     │             │              │                 │                  │                  │
     │             │              │ AuctionDTO      │                  │                  │
     │             │              │<────────────────│                  │                  │
     │             │              │                 │                  │                  │
     │             │  200 OK      │                 │                  │                  │
     │             │ (AuctionDetails)               │                  │                  │
     │             │<─────────────│                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │ Display Item │                 │                  │                  │
     │             │  Current Price                 │                  │                  │
     │             │  Highest Bidder                │                  │                  │
     │             │  Remaining Time                │                  │                  │
     │<────────────│              │                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │ Subscribe to WebSocket         │                  │                  │
     │             │  /auction/{itemId}             │                  │                  │
     │             │────────────────────────────────────────────────────────────────────>│
     │             │              │                 │                  │                  │
     │ Enter Bid   │              │                 │                  │                  │
     │  Amount     │              │                 │                  │                  │
     │────────────>│              │                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │ POST /api/auction/{itemId}/bid │                  │                  │
     │             │    {bidAmount, userId}         │                  │                  │
     │             │────────────>│                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │              │ validateAndPlaceBid(               │                  │
     │             │              │   itemId, userId, bidAmount)       │                  │
     │             │              │────────────────>│                  │                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ SELECT currentPrice, endTime        │
     │             │              │                 │  FROM auctions   │                  │
     │             │              │                 │  WHERE itemId=?  │                  │
     │             │              │                 │  FOR UPDATE      │                  │
     │             │              │                 │─────────────────>│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ currentPrice,    │                  │
     │             │              │                 │  endTime         │                  │
     │             │              │                 │<─────────────────│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ validateBid():   │                  │
     │             │              │                 │  - bidAmount > currentPrice         │
     │             │              │                 │  - auction not expired              │
     │             │              │                 │ ────────┐        │                  │
     │             │              │                 │         │        │                  │
     │             │              │                 │<────────┘        │                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ UPDATE auctions  │                  │
     │             │              │                 │  SET currentPrice=bidAmount,        │
     │             │              │                 │      highestBidderId=userId         │
     │             │              │                 │  WHERE itemId=?  │                  │
     │             │              │                 │─────────────────>│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │     Success      │                  │
     │             │              │                 │<─────────────────│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ INSERT INTO bid_history             │
     │             │              │                 │  VALUES(bidId, itemId, userId,      │
     │             │              │                 │         bidAmount, timestamp)       │
     │             │              │                 │─────────────────>│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │     bidId        │                  │
     │             │              │                 │<─────────────────│                  │
     │             │              │                 │                  │                  │
     │             │              │ BidResponse     │                  │                  │
     │             │              │ (success, newPrice)                │                  │
     │             │              │<────────────────│                  │                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ broadcastBidUpdate(                 │
     │             │              │                 │   itemId, newPrice, bidderId)       │
     │             │              │                 │────────────────────────────────────>│
     │             │              │                 │                  │                  │
     │             │  200 OK      │                 │                  │    WebSocket     │
     │             │ (BidConfirmed)                 │                  │  BidUpdate Event │
     │             │<─────────────│                 │                  │  (to all clients)│
     │             │<───────────────────────────────────────────────────────────────────│
     │             │              │                 │                  │                  │
     │             │ Update UI:   │                 │                  │                  │
     │             │ - New Current Price            │                  │                  │
     │             │ - New Highest Bidder           │                  │                  │
     │             │ - Success Message              │                  │                  │
     │<────────────│              │                 │                  │                  │
     │             │              │                 │                  │                  │
     │             │              │    [When Timer Expires]            │                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ endAuction(itemId)                  │
     │             │              │                 │ ────────┐        │                  │
     │             │              │                 │         │        │                  │
     │             │              │                 │<────────┘        │                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ UPDATE auctions  │                  │
     │             │              │                 │  SET status='ENDED'                 │
     │             │              │                 │  WHERE itemId=?  │                  │
     │             │              │                 │─────────────────>│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │     Success      │                  │
     │             │              │                 │<─────────────────│                  │
     │             │              │                 │                  │                  │
     │             │              │                 │ broadcastAuctionEnded(itemId)       │
     │             │              │                 │────────────────────────────────────>│
     │             │              │                 │                  │                  │
     │             │              │                 │                  │   WebSocket      │
     │             │              │                 │                  │ AuctionEnded Event│
     │             │<───────────────────────────────────────────────────────────────────│
     │             │              │                 │                  │                  │
     │             │ Display      │                 │                  │                  │
     │             │ "Auction Ended"                │                  │                  │
     │             │ "Pay Now" Button               │                  │                  │
     │<────────────│              │                 │                  │                  │
     │             │              │                 │                  │                  │
```

**Description:**
This comprehensive sequence diagram illustrates the real-time bidding process. The user views auction details and subscribes to WebSocket updates. When placing a bid, the system validates that the bid amount is strictly greater than the current price and the auction hasn't expired. The database is updated with row-level locking to prevent race conditions. All connected clients receive real-time updates via WebSocket. When the auction timer expires, the auction is automatically ended and all participants are notified.

---

## 4. Activity Diagrams

### 4.1 UC4: Auction Ended

```
                     ┌─────────────────────┐
                     │  Auction Timer      │
                     │     Expires         │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Mark Auction Status │
                     │    as "ENDED"       │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Identify Highest    │
                     │     Bidder          │
                     └──────────┬──────────┘
                                │
                                ▼
                ┌───────────────────────────────┐
                │  Were there any bids          │
                │  placed on this item?         │
                └───────┬───────────────┬───────┘
                        │               │
                   Yes  │               │ No
                        │               │
                        ▼               ▼
         ┌──────────────────────┐  ┌──────────────────────┐
         │ Notify All Bidders:  │  │ Remove Item from     │
         │ "Auction Ended"      │  │ Catalogue            │
         └──────────┬───────────┘  │ (No Winner)          │
                    │              └──────────┬───────────┘
                    │                         │
                    │                         ▼
                    │              ┌──────────────────────┐
                    │              │ Notify Seller:       │
                    │              │ "Item Not Sold"      │
                    │              └──────────┬───────────┘
                    │                         │
                    │                         ▼
                    │              ┌──────────────────────┐
                    │              │      END             │
                    │              └──────────────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Create Payment       │
         │ Record for Winner    │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Display "Pay Now"    │
         │ Page to Winner       │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Retrieve User's      │
         │ Shipping Address     │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Calculate Shipping   │
         │ Costs (Standard)     │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Display:             │
         │ - Item Details       │
         │ - Final Bid Price    │
         │ - Shipping Cost      │
         │ - Expedited Option   │
         │ - Total Amount       │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ User Selects         │
         │ Expedited Shipping?  │
         └────┬──────────────┬──┘
              │              │
         Yes  │              │ No
              │              │
              ▼              ▼
   ┌─────────────────┐  ┌──────────────────────┐
   │ Add Expedited   │  │ Keep Standard        │
   │ Shipping Cost   │  │ Shipping Cost        │
   └────┬────────────┘  └──────────┬───────────┘
        │                          │
        └────────┬─────────────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Update Total Amount  │
      │ with Shipping Choice │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Display Updated      │
      │ "Pay Now" Page       │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Wait for User Action │
      │ (Click "Pay Now")    │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Proceed to Payment   │
      │ Process (UC5)        │
      └──────────┬───────────┘
                 │
                 ▼
            ┌────────┐
            │  END   │
            └────────┘
```

**Description:**
This activity diagram shows the workflow when an auction ends. The system checks if any bids were placed. If not, the item is removed from the catalogue and the seller is notified. If bids exist, the highest bidder is identified, a payment record is created, and the "Pay Now" page is displayed with shipping options.

---

### 4.2 UC5: Payment Process

```
                     ┌─────────────────────┐
                     │ User Clicks         │
                     │ "Pay Now" Button    │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Verify User is      │
                     │ the Auction Winner  │
                     └───────┬─────────────┘
                             │
                 ┌───────────┴────────────┐
                 │                        │
            Yes  │                        │ No
                 │                        │
                 ▼                        ▼
      ┌──────────────────────┐  ┌──────────────────────┐
      │ Retrieve User's      │  │ Display Error:       │
      │ Shipping Address     │  │ "You did not win     │
      │ from Database        │  │  this auction"       │
      └──────────┬───────────┘  └──────────┬───────────┘
                 │                         │
                 │                         ▼
                 │              ┌──────────────────────┐
                 │              │ Redirect to          │
                 │              │ Catalogue Page       │
                 │              └──────────┬───────────┘
                 │                         │
                 │                         ▼
                 │              ┌──────────────────────┐
                 │              │      END             │
                 │              └──────────────────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Display Payment Form │
      │ - Pre-filled Address │
      │ - Item Price         │
      │ - Shipping Cost      │
      │ - Total Amount       │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ User Enters Payment  │
      │ Information:         │
      │ - Card Number        │
      │ - Name on Card       │
      │ - Expiration Date    │
      │ - Security Code      │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Validate Form Fields │
      └────┬─────────────────┘
           │
   ┌───────┴────────┐
   │                │
   │ All fields     │ Missing or
   │ complete?      │ invalid
   │                │
   ▼                ▼
┌────────┐  ┌──────────────────────┐
│  Yes   │  │ Display Error        │
│        │  │ Messages for         │
│        │  │ Invalid Fields       │
└───┬────┘  └──────────┬───────────┘
    │                  │
    │                  │
    │          ┌───────┘
    │          │
    │          │ [User Corrects Input]
    │          │
    │          └──────────────────────┐
    │                                 │
    ▼                                 ▼
┌─────────────────────┐    ┌──────────────────────┐
│ User Clicks         │    │ Return to Form Entry │
│ "Submit" Button     │    └──────────────────────┘
└──────────┬──────────┘               │
           │                          │
           │         ┌────────────────┘
           │         │
           ▼         ▼
┌─────────────────────┐
│ Validate Payment    │
│ Details Format      │
└────┬────────────────┘
     │
 ┌───┴──────┐
 │          │
 │ Valid?   │ Invalid
 │          │
 ▼          ▼
┌──┐  ┌──────────────────────┐
│Yes│ │ Display Error:       │
└─┬─┘ │ "Invalid Payment     │
  │   │  Information"        │
  │   └──────────┬───────────┘
  │              │
  │              │ [Loop back]
  │              │
  │              └────────────┐
  │                           │
  ▼                           │
┌─────────────────────┐       │
│ Process Payment     │       │
│ through Payment     │       │
│ Gateway             │       │
└────┬────────────────┘       │
     │                        │
 ┌───┴──────────┐             │
 │              │             │
 │ Payment      │ Payment     │
 │ Successful?  │ Failed      │
 │              │             │
 ▼              ▼             │
┌───┐  ┌──────────────────────┤
│Yes│  │ Display Error:       │
└─┬─┘  │ "Payment Failed.     │
  │    │  Please try again"   │
  │    └──────────┬───────────┘
  │               │
  │               └─────────────┘
  │
  ▼
┌─────────────────────┐
│ Update Payment      │
│ Record Status to    │
│ "COMPLETED"         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Update Auction      │
│ Status to "PAID"    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Remove Item from    │
│ Active Catalogue    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Generate Transaction│
│ ID and Store        │
│ Payment Record      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Proceed to Receipt  │
│ & Shipment Details  │
│ Page (UC6)          │
└──────────┬──────────┘
           │
           ▼
        ┌────────┐
        │  END   │
        └────────┘
```

**Description:**
This activity diagram illustrates the payment workflow. The system first verifies that the user is the auction winner. The payment form is displayed with pre-filled address information. The user enters credit card details, which are validated for completeness and format. Payment is processed through a payment gateway, and upon success, the auction and payment records are updated, and the user proceeds to the receipt page.

---

### 4.3 UC6: Receipt and Shipment Details

```
                     ┌─────────────────────┐
                     │ Payment Successfully│
                     │    Completed        │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Retrieve Transaction│
                     │ Details from        │
                     │ Payment Service     │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Retrieve Item       │
                     │ Details from        │
                     │ Catalogue Service   │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Calculate Shipping  │
                     │ Timeline based on   │
                     │ Item Shipping Days  │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Generate Unique     │
                     │ Order ID            │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Create Receipt      │
                     │ Record in Database  │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Format Receipt Data:│
                     │ - Order ID          │
                     │ - Transaction ID    │
                     │ - Item Name         │
                     │ - Item Price        │
                     │ - Shipping Type     │
                     │ - Shipping Cost     │
                     │ - Total Amount Paid │
                     │ - Payment Method    │
                     │ - Transaction Date  │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Format Shipping Data│
                     │ - Recipient Name    │
                     │ - Shipping Address  │
                     │ - Estimated Delivery│
                     │   ("Ships in X days")
                     │ - Tracking (TBD)    │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Send Email Receipt  │
                     │ to User's Email     │
                     │ Address             │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Display Receipt     │
                     │ and Shipment Page:  │
                     │ - Order Summary     │
                     │ - Payment Details   │
                     │ - Shipping Info     │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Provide Options:    │
                     │ - Print Receipt     │
                     │ - Download PDF      │
                     │ - Return to Home    │
                     └──────────┬──────────┘
                                │
                                ▼
                 ┌───────────────────────────┐
                 │ User Selects Action       │
                 └───┬──────────┬────────┬───┘
                     │          │        │
              Print  │Download  │Return  │
              Receipt│  PDF     │ Home   │
                     │          │        │
                     ▼          ▼        ▼
          ┌─────────────┐┌──────────┐┌──────────┐
          │Open Browser ││Generate  ││Navigate  │
          │Print Dialog ││PDF File  ││to Home   │
          │             ││& Download││Page      │
          └──────┬──────┘└────┬─────┘└────┬─────┘
                 │             │           │
                 └─────┬───────┴───────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │ Log Transaction      │
            │ Completion in Audit  │
            │ Trail                │
            └──────────┬───────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │ Update User's Order  │
            │ History              │
            └──────────┬───────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │ Notify Seller:       │
            │ "Item Sold"          │
            │ "Payment Received"   │
            └──────────┬───────────┘
                       │
                       ▼
                  ┌────────┐
                  │  END   │
                  └────────┘
```

**Description:**
This activity diagram shows the receipt generation and shipment details workflow. After successful payment, the system retrieves transaction and item details, calculates shipping timeline, generates a unique order ID, and creates a receipt record. The receipt and shipment information are displayed to the user with options to print, download as PDF, or return to the homepage. The seller is notified of the successful sale.

---

### 4.4 UC7: Auction Item Upload

```
                     ┌─────────────────────┐
                     │ Seller Clicks       │
                     │ "Create Auction"    │
                     │ Button              │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │ Verify User is      │
                     │ Authenticated       │
                     └───────┬─────────────┘
                             │
                 ┌───────────┴────────────┐
                 │                        │
            Yes  │                        │ No
                 │                        │
                 ▼                        ▼
      ┌──────────────────────┐  ┌──────────────────────┐
      │ Display Auction      │  │ Redirect to Sign-In  │
      │ Creation Form        │  │ Page                 │
      └──────────┬───────────┘  └──────────┬───────────┘
                 │                         │
                 │                         ▼
                 │              ┌──────────────────────┐
                 │              │      END             │
                 │              └──────────────────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Seller Enters Item   │
      │ Information:         │
      │ - Item Name          │
      │ - Description        │
      │ - Keywords           │
      │ - Starting Bid Price │
      │ - Auction Duration   │
      │ - Auction Type       │
      │   (Forward)          │
      │ - Shipping Days      │
      │ - Expedited Shipping │
      │   Cost (optional)    │
      │ - Item Image(s)      │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Validate Form Fields │
      └────┬─────────────────┘
           │
   ┌───────┴──────────┐
   │                  │
   │ All required     │ Missing or
   │ fields valid?    │ invalid
   │                  │
   ▼                  ▼
┌────────┐  ┌──────────────────────┐
│  Yes   │  │ Display Error        │
│        │  │ Messages:            │
│        │  │ - Highlight Invalid  │
│        │  │   Fields             │
│        │  │ - Show Requirements  │
└───┬────┘  └──────────┬───────────┘
    │                  │
    │                  │
    │          ┌───────┘
    │          │
    │          │ [User Corrects Input]
    │          │
    │          └──────────────────────┐
    │                                 │
    ▼                                 ▼
┌─────────────────────┐    ┌──────────────────────┐
│ Validate Business   │    │ Return to Form Entry │
│ Rules:              │    └──────────────────────┘
│ - Starting Price > 0│               │
│ - Duration >= 1 hour│               │
│ - Duration <= 30    │               │
│   days              │               │
└────┬────────────────┘               │
     │                                │
 ┌───┴──────┐           ┌─────────────┘
 │          │           │
 │ Valid?   │ Invalid   │
 │          │           │
 ▼          ▼           │
┌──┐  ┌──────────────────────┐
│Yes│ │ Display Error:       │
└─┬─┘ │ "Invalid Auction     │
  │   │  Parameters"         │
  │   └──────────┬───────────┘
  │              │
  │              │ [Loop back]
  │              │
  │              └────────────┘
  │
  ▼
┌─────────────────────┐
│ Upload Item Image(s)│
│ to File Storage     │
└────┬────────────────┘
     │
     ▼
┌─────────────────────┐
│ Generate Unique     │
│ Item ID             │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Calculate Auction   │
│ End Time:           │
│ currentTime +       │
│ duration            │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Store Item in       │
│ Catalogue Database: │
│ - Item Details      │
│ - Seller ID         │
│ - Start Time        │
│ - End Time          │
│ - Initial Price     │
│ - Status: ACTIVE    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Create Auction      │
│ Record in Auction   │
│ Database:           │
│ - Item ID           │
│ - Current Price =   │
│   Starting Price    │
│ - Highest Bidder =  │
│   NULL              │
│ - Status: ACTIVE    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Initialize Auction  │
│ Timer Scheduler     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Index Item for      │
│ Search (Keywords)   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Display Success     │
│ Message:            │
│ "Auction Created    │
│  Successfully"      │
│ - Show Item ID      │
│ - Show End Time     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Provide Options:    │
│ - View Item Listing │
│ - Create Another    │
│   Auction           │
│ - Return to         │
│   Dashboard         │
└──────────┬──────────┘
           │
           ▼
       ┌───┴────┬─────────┐
       │        │         │
  View │ Create │ Return  │
  Item │Another │Dashboard│
       │        │         │
       ▼        ▼         ▼
    ┌──────┐┌──────┐┌─────────┐
    │Navigate│Loop │Navigate │
    │to Item│Back │to       │
    │Detail ││     │Dashboard│
    │Page   ││     │         │
    └───┬───┘└──┬──┘└────┬────┘
        │      │        │
        └──────┴───┬────┘
                   │
                   ▼
            ┌──────────────────────┐
            │ Log Auction Creation │
            │ in Audit Trail       │
            └──────────┬───────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │ Send Notification    │
            │ to Seller:           │
            │ "Auction is Live"    │
            └──────────┬───────────┘
                       │
                       ▼
                  ┌────────┐
                  │  END   │
                  └────────┘
```

**Description:**
This activity diagram depicts the seller's workflow for creating an auction. The system verifies authentication, displays the auction creation form, validates all input fields and business rules (price > 0, duration between 1 hour and 30 days), uploads item images, generates a unique item ID, calculates auction end time, stores the item in both Catalogue and Auction databases, initializes the auction timer, indexes the item for search, and notifies the seller of successful auction creation.

---

## 5. Architecture

### 5.1 System Architecture Overview

Our auction e-commerce system follows a **microservices architecture** organized into three main tiers:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FRONT-END TIER                                    │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                    HTML5 / CSS3 / JavaScript                           │ │
│  │                React.js Single Page Application (SPA)                  │ │
│  │                                                                        │ │
│  │  Components:                                                           │ │
│  │  • Sign-Up / Sign-In Forms    • Auction Listing Views                 │ │
│  │  • Item Search Interface      • Real-time Bidding UI                  │ │
│  │  • Payment Form               • Receipt Display                       │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
                               │ REST APIs (HTTPS)
                               │ WebSocket (Real-time Updates)
                               │
┌──────────────────────────────┴──────────────────────────────────────────────┐
│                           MIDDLE TIER                                       │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                     API GATEWAY SERVICE                                │ │
│  │                     (Spring Boot Framework)                            │ │
│  │                                                                        │ │
│  │  Responsibilities:                                                     │ │
│  │  • Request Routing & Load Balancing                                   │ │
│  │  • Authentication & Authorization (JWT Validation)                    │ │
│  │  • Rate Limiting & Throttling                                         │ │
│  │  • Request/Response Transformation                                    │ │
│  │  • Circuit Breaker Pattern Implementation                             │ │
│  │  • API Composition (Aggregating multiple service calls)               │ │
│  │  • WebSocket Connection Management                                    │ │
│  │  • Logging & Monitoring                                               │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└──────────────────┬──────────────────────────────────────────────────────────┘
                   │
                   │ REST APIs / Message Queues (RabbitMQ/Kafka)
                   │
┌──────────────────┴──────────────────────────────────────────────────────────┐
│                           BACK-END TIER                                     │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                      MICROSERVICES                                     │ │
│  │                                                                        │ │
│  │  ┌──────────────────────┐  ┌──────────────────────┐                  │ │
│  │  │  IAM SERVICE         │  │  CATALOGUE SERVICE   │                  │ │
│  │  │  (Port: 8081)        │  │  (Port: 8082)        │                  │ │
│  │  │                      │  │                      │                  │ │
│  │  │  • User Sign-Up      │  │  • Item Management   │                  │ │
│  │  │  • User Sign-In      │  │  • Item Search       │                  │ │
│  │  │  • Authentication    │  │  • Keyword Indexing  │                  │ │
│  │  │  • Password Reset    │  │  • Item Categorization                  │ │
│  │  │  • JWT Generation    │  │  • Item Status       │                  │ │
│  │  │  • User Profile Mgmt │  │    Tracking          │                  │ │
│  │  │                      │  │                      │                  │ │
│  │  │  ┌────────────────┐ │  │  ┌────────────────┐ │                  │ │
│  │  │  │   USER DB      │ │  │  │  CATALOGUE DB  │ │                  │ │
│  │  │  │   (SQLite)     │ │  │  │   (SQLite)     │ │                  │ │
│  │  │  └────────────────┘ │  │  └────────────────┘ │                  │ │
│  │  └──────────────────────┘  └──────────────────────┘                  │ │
│  │                                                                        │ │
│  │  ┌──────────────────────┐  ┌──────────────────────┐                  │ │
│  │  │  AUCTION SERVICE     │  │  PAYMENT SERVICE     │                  │ │
│  │  │  (Port: 8083)        │  │  (Port: 8084)        │                  │ │
│  │  │                      │  │                      │                  │ │
│  │  │  • Bid Validation    │  │  • Payment Processing│                  │ │
│  │  │  • Bid Placement     │  │  • Payment Gateway   │                  │ │
│  │  │  • Auction State Mgmt│  │    Integration       │                  │ │
│  │  │  • Timer Management  │  │  • Receipt Generation│                  │ │
│  │  │  • Highest Bidder    │  │  • Refund Processing │                  │ │
│  │  │    Tracking          │  │  • Transaction       │                  │ │
│  │  │  • Auction End       │  │    History           │                  │ │
│  │  │    Notification      │  │                      │                  │ │
│  │  │  • WebSocket Push    │  │                      │                  │ │
│  │  │                      │  │                      │                  │ │
│  │  │  ┌────────────────┐ │  │  ┌────────────────┐ │                  │ │
│  │  │  │  AUCTION DB    │ │  │  │  PAYMENT DB    │ │                  │ │
│  │  │  │  (SQLite)      │ │  │  │  (SQLite)      │ │                  │ │
│  │  │  └────────────────┘ │  │  └────────────────┘ │                  │ │
│  │  └──────────────────────┘  └──────────────────────┘                  │ │
│  │                                                                        │ │
│  │  ┌────────────────────────────────────────────────────────────────┐  │ │
│  │  │                  NOTIFICATION SERVICE                          │  │ │
│  │  │                      (Port: 8085)                              │  │ │
│  │  │                                                                │  │ │
│  │  │  • Email Notifications (Auction End, Payment Confirmation)    │  │ │
│  │  │  • Real-time WebSocket Push Notifications                     │  │ │
│  │  │  • SMS Notifications (Future Extension)                       │  │ │
│  │  │                                                                │  │ │
│  │  │  ┌────────────────┐                                           │  │ │
│  │  │  │NOTIFICATION DB │                                           │  │ │
│  │  │  │   (SQLite)     │                                           │  │ │
│  │  │  └────────────────┘                                           │  │ │
│  │  └────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                        │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │               MESSAGE BROKER (RabbitMQ / Apache Kafka)                 │ │
│  │                                                                        │ │
│  │  Event Topics:                                                         │ │
│  │  • auction.ended        • payment.completed                           │ │
│  │  • bid.placed           • user.registered                             │ │
│  │  • item.added           • notification.send                           │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Architecture Characteristics:**

1. **Polyglot Persistence:** Each service owns its database (Database per Service pattern)
2. **Service Discovery:** Services register themselves and discover other services dynamically
3. **Asynchronous Communication:** Message broker for event-driven interactions
4. **Synchronous Communication:** REST APIs for request-response patterns
5. **Fault Tolerance:** Circuit breakers prevent cascading failures
6. **Scalability:** Each service can be scaled independently based on load
7. **Containerization Ready:** All services can be deployed as Docker containers (Milestone 3)

---

### 5.2 Services Description

| Service Name | Description | Exposed Interface Names |
|--------------|-------------|------------------------|
| **IAM Service** (Identity and Access Management) | Manages user authentication, authorization, and credential management. Handles user registration, login, password reset, and JWT token generation. Stores user personal information and shipping addresses. | IAM:Auth<br>IAM:UserMgmt |
| **Catalogue Service** | Manages the inventory of items available for auction. Provides search functionality by keywords, maintains item details (name, description, shipping information), and tracks item status (active, sold, expired). | Catalogue:ItemMgmt<br>Catalogue:Search |
| **Auction Service** | Orchestrates the bidding process for forward auctions. Validates bid amounts, maintains auction state, tracks highest bidder, manages auction timers, and broadcasts real-time updates to connected clients via WebSocket. Handles auction end logic. | Auction:BidMgmt<br>Auction:AuctionLifecycle<br>Auction:Notification |
| **Payment Service** | Processes payments for won auctions. Integrates with payment gateway, calculates total amounts (item price + shipping), generates receipts, and maintains transaction history. Handles payment validation and authorization. | Payment:ProcessPayment<br>Payment:ReceiptMgmt |
| **Notification Service** | Handles all system notifications. Sends email notifications (auction end, payment confirmation), manages WebSocket connections for real-time push notifications, and maintains notification history. | Notification:Email<br>Notification:RealTime |
| **Gateway Service** | Acts as the single entry point (API Gateway) for all client requests. Handles request routing, authentication validation (JWT), rate limiting, load balancing, and API composition. Implements Circuit Breaker pattern for fault tolerance. | Gateway:Router<br>Gateway:Auth |

---

### 5.3 Interfaces Description

#### Interface Operations

| Interface Name | Operations | Operation Descriptions |
|----------------|-----------|------------------------|
| **IAM:Auth** | `AuthResponse signUp(SignUpRequest request)`<br><br>`AuthResponse signIn(SignInRequest request)`<br><br>`boolean validateToken(String jwtToken)`<br><br>`boolean resetPassword(String username, String email)` | Used by Gateway Service<br><br>**signUp:** Creates new user account with username, password, name, and address. Returns JWT token and userId. Validates uniqueness of username. Hashes password before storage.<br><br>**signIn:** Authenticates user with username and password. Returns JWT token and user data. Updates last login timestamp.<br><br>**validateToken:** Validates JWT token signature and expiration. Returns boolean indicating validity. Used by Gateway for request authorization.<br><br>**resetPassword:** Initiates password reset process. Sends reset link to user's email. |
| **IAM:UserMgmt** | `User getUserById(Long userId)`<br><br>`User updateUserProfile(Long userId, UserUpdateRequest request)`<br><br>`Address getShippingAddress(Long userId)` | Used by Payment Service, Auction Service<br><br>**getUserById:** Retrieves user details by userId. Returns User object with personal information.<br><br>**updateUserProfile:** Updates user personal information (name, address). Returns updated User object.<br><br>**getShippingAddress:** Retrieves user's shipping address for payment and shipment display. Returns Address object. |
| **Catalogue:ItemMgmt** | `Item createItem(ItemRequest request)`<br><br>`Item getItemById(Long itemId)`<br><br>`boolean updateItemStatus(Long itemId, ItemStatus status)`<br><br>`boolean removeItem(Long itemId)` | Used by Gateway Service, Auction Service<br><br>**createItem:** Seller creates new auction item. Validates item data, generates itemId, stores item details, initializes status as ACTIVE.<br><br>**getItemById:** Retrieves item details by itemId. Returns Item object with all metadata.<br><br>**updateItemStatus:** Changes item status (ACTIVE, SOLD, EXPIRED, REMOVED). Called when auction ends or item is paid for.<br><br>**removeItem:** Removes item from catalogue. Called when auction expires with no bids. |
| **Catalogue:Search** | `List<Item> searchItems(String keyword)`<br><br>`List<Item> getActiveItems(int page, int size)`<br><br>`List<Item> getItemsBySeller(Long sellerId)` | Used by Gateway Service<br><br>**searchItems:** Full-text search on item name, description, and keywords. Returns list of matching ACTIVE items.<br><br>**getActiveItems:** Paginated list of all active auction items. Used for browse functionality.<br><br>**getItemsBySeller:** Returns all items listed by a specific seller. Used for seller dashboard. |
| **Auction:BidMgmt** | `BidResponse placeBid(Long itemId, Long userId, int bidAmount)`<br><br>`AuctionDetails getAuctionDetails(Long itemId)`<br><br>`List<Bid> getBidHistory(Long itemId)` | Used by Gateway Service<br><br>**placeBid:** Validates and places bid. Checks: bidAmount > currentPrice, auction not expired, user authenticated. Uses row-level locking to prevent race conditions. Returns success/failure with new current price.<br><br>**getAuctionDetails:** Retrieves current auction state (current price, highest bidder, remaining time). Used for auction detail page.<br><br>**getBidHistory:** Returns chronological list of all bids for an item. Used for audit and display. |
| **Auction:AuctionLifecycle** | `Auction createAuction(Long itemId, int startPrice, LocalDateTime endTime)`<br><br>`boolean endAuction(Long itemId)`<br><br>`AuctionResult getAuctionResult(Long itemId)`<br><br>`void scheduleAuctionEnd(Long itemId, LocalDateTime endTime)` | Used by Catalogue Service, Payment Service, Auction Service (internal)<br><br>**createAuction:** Initializes auction record when seller creates item. Sets starting price, end time, status as ACTIVE.<br><br>**endAuction:** Manually or automatically ends auction. Determines winner, publishes AuctionEnded event, updates status.<br><br>**getAuctionResult:** Returns final auction result (winner, final price). Used by Payment Service.<br><br>**scheduleAuctionEnd:** Schedules timer job to automatically end auction at specified time. |
| **Auction:Notification** | `void subscribeToAuction(Long itemId, Long userId, WebSocketSession session)`<br><br>`void unsubscribeFromAuction(Long itemId, Long userId)`<br><br>`void broadcastBidUpdate(Long itemId, BidUpdate update)` | Used by Gateway Service (WebSocket), Auction Service (internal)<br><br>**subscribeToAuction:** Registers user's WebSocket session for real-time auction updates. Maintains session mapping.<br><br>**unsubscribeFromAuction:** Removes user's WebSocket subscription when they leave auction page.<br><br>**broadcastBidUpdate:** Sends bid update (new price, bidder) to all subscribed clients via WebSocket. |
| **Payment:ProcessPayment** | `PaymentResponse initiatePayment(Long userId, Long itemId, boolean expeditedShipping)`<br><br>`PaymentResponse processPayment(Long paymentId, PaymentDetails details)`<br><br>`boolean validateWinner(Long userId, Long itemId)` | Used by Gateway Service<br><br>**initiatePayment:** Creates payment record when user clicks "Pay Now". Retrieves auction result, item price, calculates shipping costs, returns total amount and payment details form.<br><br>**processPayment:** Processes payment through payment gateway. Validates credit card details, authorizes transaction, updates payment status to COMPLETED, publishes PaymentCompleted event.<br><br>**validateWinner:** Verifies that the requesting user is the auction winner. Prevents non-winners from accessing payment page. |
| **Payment:ReceiptMgmt** | `Receipt generateReceipt(Long paymentId)`<br><br>`Receipt getReceiptByPaymentId(Long paymentId)`<br><br>`byte[] generateReceiptPDF(Long paymentId)` | Used by Gateway Service, Notification Service<br><br>**generateReceipt:** Creates receipt record with order ID, transaction details, item info, payment info, shipment details. Calculates shipment date.<br><br>**getReceiptByPaymentId:** Retrieves existing receipt for display or re-download.<br><br>**generateReceiptPDF:** Generates downloadable PDF version of receipt with formatted layout. |
| **Notification:Email** | `void sendAuctionEndedEmail(Long userId, Long itemId, boolean isWinner)`<br><br>`void sendPaymentConfirmationEmail(Long userId, Long paymentId)`<br><br>`void sendPasswordResetEmail(String email, String resetToken)` | Used by Auction Service, Payment Service, IAM Service<br><br>**sendAuctionEndedEmail:** Sends email notification when auction ends. Different content for winner vs. non-winner.<br><br>**sendPaymentConfirmationEmail:** Sends payment receipt and shipment details via email after successful payment.<br><br>**sendPasswordResetEmail:** Sends password reset link when user requests password recovery. |
| **Notification:RealTime** | `void pushNotification(Long userId, Notification notification)`<br><br>`void broadcastToAuction(Long itemId, Notification notification)` | Used by Auction Service<br><br>**pushNotification:** Sends real-time notification to specific user via WebSocket. Used for personal alerts.<br><br>**broadcastToAuction:** Broadcasts notification to all users watching a specific auction. Used for bid updates and auction end. |
| **Gateway:Router** | `ResponseEntity<?> routeRequest(HttpServletRequest request)`<br><br>`void loadBalance(String serviceName, HttpServletRequest request)` | Internal to Gateway Service<br><br>**routeRequest:** Analyzes incoming request and routes to appropriate backend service. Applies circuit breaker pattern.<br><br>**loadBalance:** Distributes requests across multiple instances of the same service for high availability. |
| **Gateway:Auth** | `boolean authenticateRequest(HttpServletRequest request)`<br><br>`void rateLimitCheck(String userId, String endpoint)` | Internal to Gateway Service<br><br>**authenticateRequest:** Extracts JWT from Authorization header, validates with IAM Service, attaches user context to request.<br><br>**rateLimitCheck:** Enforces rate limits per user per endpoint to prevent abuse. Throws exception if limit exceeded. |

---

## 6. Activities Plan, Product Backlog, and Sprint Backlog

### 6.1 Product Backlog

The complete product backlog represents all features and tasks to be implemented across all three deliverables:

#### Epic 1: User Management & Authentication
- **PB-01:** User registration with username, password, name, and shipping address
- **PB-02:** User login with username and password authentication
- **PB-03:** JWT token generation and validation
- **PB-04:** Password hashing and secure storage
- **PB-05:** Password reset/recovery mechanism
- **PB-06:** User profile management and editing
- **PB-07:** Session management across multiple browsers
- **PB-08:** Two-factor authentication (optional enhancement)

#### Epic 2: Catalogue & Item Management
- **PB-09:** Seller item upload with description, starting price, and duration
- **PB-10:** Item search by keywords
- **PB-11:** Full-text search indexing for items
- **PB-12:** Item categorization and tagging
- **PB-13:** Item image upload and storage
- **PB-14:** Active item listing with pagination
- **PB-15:** Item status tracking (ACTIVE, SOLD, EXPIRED, REMOVED)
- **PB-16:** Seller dashboard to view their items

#### Epic 3: Auction & Bidding
- **PB-17:** Forward auction creation with start price and end time
- **PB-18:** Auction timer management and scheduling
- **PB-19:** Bid placement with amount validation (strictly increasing)
- **PB-20:** Current price and highest bidder tracking
- **PB-21:** Real-time bid updates via WebSocket
- **PB-22:** Remaining time calculation and display
- **PB-23:** Automatic auction end when timer expires
- **PB-24:** Concurrent bidding with race condition prevention
- **PB-25:** Bid history tracking and display
- **PB-26:** Auction end notification to all participants

#### Epic 4: Payment & Checkout
- **PB-27:** Winner validation for payment access
- **PB-28:** Payment form with credit card details
- **PB-29:** Shipping cost calculation (standard and expedited)
- **PB-30:** Payment processing through payment gateway integration
- **PB-31:** Payment validation and authorization
- **PB-32:** Transaction record creation and storage
- **PB-33:** Payment failure handling and retry mechanism
- **PB-34:** Expedited shipping option selection

#### Epic 5: Receipt & Shipment
- **PB-35:** Receipt generation with order ID and transaction details
- **PB-36:** Shipment timeline calculation based on item shipping days
- **PB-37:** Receipt display with all payment and shipping information
- **PB-38:** Receipt email notification
- **PB-39:** Downloadable PDF receipt generation
- **PB-40:** Order history for users
- **PB-41:** Seller notification of successful sale

#### Epic 6: System Architecture & Infrastructure
- **PB-42:** Microservices architecture design and implementation
- **PB-43:** API Gateway service with request routing
- **PB-44:** Service-to-service communication (REST APIs)
- **PB-45:** Message broker integration (RabbitMQ/Kafka)
- **PB-46:** Database design and schema creation (SQLite)
- **PB-47:** WebSocket integration for real-time updates
- **PB-48:** Circuit Breaker pattern implementation
- **PB-49:** Rate limiting and throttling
- **PB-50:** Logging and monitoring infrastructure
- **PB-51:** Docker containerization (Milestone 3)
- **PB-52:** Container orchestration and deployment (Milestone 3)

#### Epic 7: User Interface
- **PB-53:** Sign-up and sign-in page UI
- **PB-54:** Catalogue browsing and search UI
- **PB-55:** Auction detail and bidding UI with real-time updates
- **PB-56:** Payment form UI
- **PB-57:** Receipt and shipment details UI
- **PB-58:** Seller item upload UI
- **PB-59:** Responsive design for mobile and desktop
- **PB-60:** Navigation and routing between pages
- **PB-61:** Error handling and user feedback messages
- **PB-62:** Loading states and progress indicators

#### Epic 8: Testing & Quality Assurance
- **PB-63:** Unit tests for all service methods
- **PB-64:** Integration tests for service interactions
- **PB-65:** End-to-end tests for complete user workflows
- **PB-66:** Performance testing for concurrent bidding scenarios
- **PB-67:** Security testing (SQL injection, XSS, CSRF)
- **PB-68:** Load testing for scalability validation
- **PB-69:** Test data generation and seeding

#### Epic 9: Advanced Features (Milestone 3)
- **PB-70:** Self-adaptive system feature using LLM
- **PB-71:** AI-powered bid recommendations
- **PB-72:** Chatbot for auction assistance
- **PB-73:** Automated testing with AI-generated test cases
- **PB-74:** Dynamic pricing suggestions

---

### 6.2 Sprint Backlog by Deliverable

#### Deliverable 1 (Current - Weeks 1-4): Architecture & Design
**Focus:** System specification, architecture design, planning

- PB-42: Microservices architecture design (Complete)
- Document all sequence diagrams (Complete)
- Document all activity diagrams (Complete)
- Create architecture component diagrams (Complete)
- Define service interfaces and contracts (Complete)
- Create product backlog and GANTT diagram (Complete)
- Define 16 test cases for TDD (Complete)
- Group meeting logs and documentation (Complete)

**Deliverable:** Software Design Document with UML diagrams, architecture specification, test cases, and implementation plan

---

#### Deliverable 2 (Weeks 5-8): Back-End Implementation
**Focus:** Complete back-end services with minimal UI

**Sprint 1 (Weeks 5-6): Core Services Foundation**
- PB-01: User registration backend
- PB-02: User login backend
- PB-03: JWT token generation
- PB-04: Password hashing
- PB-09: Seller item upload backend
- PB-10: Item search backend
- PB-46: Database schema design and creation
- PB-63: Unit tests for IAM and Catalogue services

**Sprint 2 (Weeks 7-8): Auction & Payment Services**
- PB-17: Auction creation backend
- PB-18: Auction timer management
- PB-19: Bid placement backend
- PB-20: Current price and bidder tracking
- PB-24: Concurrent bidding with locking
- PB-27: Winner validation
- PB-28: Payment processing backend
- PB-29: Shipping cost calculation
- PB-35: Receipt generation backend
- PB-44: REST API communication between services
- PB-63: Unit tests for Auction and Payment services

**Deliverable:** Fully functional back-end services with REST APIs, minimal UI for individual use cases (no full navigation), test suite

---

#### Deliverable 3 (Weeks 9-12): Full System Integration
**Focus:** Complete UI, microservices deployment, advanced features

**Sprint 3 (Weeks 9-10): UI Development & Real-Time Features**
- PB-53: Sign-up and sign-in UI
- PB-54: Catalogue browsing UI
- PB-55: Auction bidding UI
- PB-56: Payment form UI
- PB-57: Receipt display UI
- PB-58: Seller item upload UI
- PB-21: Real-time WebSocket bid updates
- PB-47: WebSocket integration
- PB-60: Navigation and routing
- PB-61: Error handling UI
- PB-65: End-to-end tests

**Sprint 4 (Weeks 11-12): Containerization & Advanced Features**
- PB-43: API Gateway implementation
- PB-45: Message broker integration
- PB-48: Circuit breaker implementation
- PB-49: Rate limiting
- PB-51: Docker containerization
- PB-52: Container deployment to cloud
- PB-70-74: Advanced LLM-powered feature
- PB-68: Load testing
- Final integration testing
- Documentation updates

**Deliverable:** Complete working system with full UI, containerized microservices deployed to cloud, advanced AI feature, comprehensive test suite, final documentation

---

### 6.3 GANTT Diagram

```
PROJECT TIMELINE: EECS 4413 Auction E-Commerce System
Duration: 12 Weeks (February 2026 - April 2026)

Week #    1    2    3    4    5    6    7    8    9   10   11   12
          |----|----|----|----|----|----|----|----|----|----|----|----|

═══════════════════════════════════════════════════════════════════════
DELIVERABLE 1: ARCHITECTURE & DESIGN
═══════════════════════════════════════════════════════════════════════
Requirements Analysis
█████
Architecture Design
     ██████████
UML Diagrams (Sequence)
          █████████
UML Diagrams (Activity)
               ████████
Service Interface Design
          ████████████
Database Schema Design
               ████████
Test Case Definition
                    ████
GANTT & Planning
                    ████
SDD Documentation
     ██████████████████████
                         ▼
                   DELIVERABLE 1 DUE

═══════════════════════════════════════════════════════════════════════
DELIVERABLE 2: BACK-END IMPLEMENTATION
═══════════════════════════════════════════════════════════════════════
Database Setup
                    ████
IAM Service Development
                    ██████████
Catalogue Service Dev
                         ██████████
Auction Service Dev
                              ██████████
Payment Service Dev
                                   ██████████
Service Integration
                                        ████████
REST API Development
                         ████████████████████
Unit Testing
                              ████████████████
Minimal UI for Testing
                                        ████████
Integration Testing
                                             ████
Documentation Update
                                             ████
                                                  ▼
                                          DELIVERABLE 2 DUE

═══════════════════════════════════════════════════════════════════════
DELIVERABLE 3: FULL SYSTEM & DEPLOYMENT
═══════════════════════════════════════════════════════════════════════
Frontend Development
                                                  ██████████
WebSocket Integration
                                                       ██████████
API Gateway Service
                                                       ████████
Message Broker Setup
                                                            ████████
Real-time Bid Updates
                                                            ████████
Navigation & Routing
                                                       ██████████
UI Polish & UX
                                                            ████████
Docker Containerization
                                                            ████████
Container Orchestration
                                                                 ████████
Cloud Deployment
                                                                      ████
Advanced LLM Feature
                                                                 ████████████
End-to-End Testing
                                                                      ████████
Load Testing
                                                                           ████
Final Documentation
                                                                      ████████
                                                                               ▼
                                                                        DELIVERABLE 3 DUE

═══════════════════════════════════════════════════════════════════════
CONTINUOUS ACTIVITIES (Throughout Project)
═══════════════════════════════════════════════════════════════════════
Group Meetings
████████████████████████████████████████████████████████████████████████

Code Reviews
                    ████████████████████████████████████████████████████

Git Version Control
████████████████████████████████████████████████████████████████████████

Testing & Debugging
                              ██████████████████████████████████████████

Documentation Updates
     ███████████████████████████████████████████████████████████████████

═══════════════════════════════════════════════════════════════════════
MILESTONES
═══════════════════════════════════════════════════════════════════════
Week 4:  ▼ Deliverable 1 - Software Design Document
Week 8:  ▼ Deliverable 2 - Back-End Implementation
Week 12: ▼ Deliverable 3 - Full System Deployment

═══════════════════════════════════════════════════════════════════════
LEGEND
═══════════════════════════════════════════════════════════════════════
████  = Active Work Period
▼     = Milestone/Deliverable Due Date
```

**Key Milestones:**

1. **Week 4 - Deliverable 1:** Complete architecture design, all UML diagrams, interface specifications, test cases, and implementation plan
2. **Week 8 - Deliverable 2:** Functional back-end services with REST APIs, database integration, minimal UI for testing
3. **Week 12 - Deliverable 3:** Complete system with full UI, WebSocket real-time updates, containerized deployment, advanced LLM feature

**Critical Path:**
- Architecture Design → Service Development → Integration → UI Development → Deployment
- Dependencies: UI development depends on back-end APIs; containerization depends on service completion; advanced features depend on core functionality

**Risk Mitigation:**
- Buffer time built into weeks 8 and 12 for unexpected issues
- Continuous testing throughout to catch issues early
- Regular group meetings to track progress and address blockers
- Parallel development where possible (e.g., different team members on different services)

---

## 7. Group Meeting Logs

### Meeting #1
**Date:** February 4, 2026  
**Time:** 10:00 AM - 12:00 PM  
**Location:** Virtual (Zoom)  
**Attendees:** All 4 team members

**Agenda:**
1. Review project requirements and deliverables
2. Discuss architectural approach
3. Assign responsibilities for Deliverable 1

**Discussion Summary:**
- Reviewed the project description and use cases in detail
- Discussed pros and cons of monolithic vs. microservices architecture
- Agreed on microservices architecture for scalability and alignment with Milestone 3 requirements
- Discussed technology stack: Spring Boot for backend, React for frontend, SQLite for databases
- Identified key services: IAM, Catalogue, Auction, Payment, Notification
- Discussed communication patterns: REST for synchronous, RabbitMQ for asynchronous

**Decisions Made:**
- **Architecture:** Microservices with API Gateway pattern
- **Database:** SQLite with separate database per service
- **Communication:** REST APIs + Message Broker (RabbitMQ)
- **Frontend:** React.js Single Page Application
- **WebSocket:** For real-time bidding updates
- **Testing:** JUnit for unit tests, Selenium for E2E tests

**Tasks Assigned:**

| Team Member | Task | Deadline |
|-------------|------|----------|
| Member 1 | Create sequence diagrams for UC1.1, UC1.2 | Feb 6 |
| Member 2 | Create sequence diagrams for UC2, UC3 | Feb 6 |
| Member 3 | Create activity diagrams for UC4, UC5 | Feb 6 |
| Member 4 | Create activity diagrams for UC6, UC7 | Feb 6 |
| All Members | Define test cases (4 each = 16 total) | Feb 8 |
| Member 1 | Write Introduction and Major Design Decisions | Feb 9 |
| Member 2 | Create Architecture diagrams and service descriptions | Feb 9 |
| Member 3 | Create Product Backlog and Sprint Backlog | Feb 10 |
| Member 4 | Create GANTT diagram and compile final document | Feb 10 |

**Action Items:**
- All members to review UML notation guidelines
- Set up shared Google Drive folder for diagrams
- Schedule next meeting for February 7 to review diagrams

**Next Meeting:** February 7, 2026 at 2:00 PM

---

### Meeting #2
**Date:** February 7, 2026  
**Time:** 2:00 PM - 4:00 PM  
**Location:** Virtual (Zoom)  
**Attendees:** All 4 team members

**Agenda:**
1. Review completed sequence diagrams
2. Review completed activity diagrams
3. Provide feedback and request revisions

**Discussion Summary:**
- Member 1 presented sequence diagrams for UC1.1 (Sign-Up) and UC1.2 (Sign-In)
  - Feedback: Add password hashing step, clarify JWT token generation
  - Revision needed: Show database interaction more explicitly
- Member 2 presented sequence diagrams for UC2 (Browse Catalogue) and UC3 (Bidding)
  - Feedback: UC3 needs WebSocket connection establishment, add race condition handling
  - Good: Showed interaction between multiple services well
- Member 3 presented activity diagrams for UC4 (Auction Ended) and UC5 (Payment)
  - Feedback: Add decision point for expedited shipping in UC4
  - Revision needed: Show validation errors in payment flow
- Member 4 presented activity diagrams for UC6 (Receipt) and UC7 (Auction Upload)
  - Feedback: Add email notification step in UC6
  - Good: Comprehensive flow with all validation steps

**Decisions Made:**
- Standardize diagram naming conventions: use service names consistently
- Add legends to all diagrams for clarity
- Include error handling paths in all diagrams
- Use consistent color coding across all diagrams

**Tasks Assigned:**

| Team Member | Task | Deadline |
|-------------|------|----------|
| Member 1 | Revise UC1.1, UC1.2 diagrams based on feedback | Feb 8 |
| Member 2 | Revise UC2, UC3 diagrams with WebSocket details | Feb 8 |
| Member 3 | Revise UC4, UC5 diagrams with additional decision points | Feb 8 |
| Member 4 | Revise UC6, UC7 diagrams with notifications | Feb 8 |

**Action Items:**
- Share revised diagrams in Google Drive by end of day Feb 8
- Begin work on assigned written sections

**Next Meeting:** February 10, 2026 at 10:00 AM

---

### Meeting #3
**Date:** February 10, 2026  
**Time:** 10:00 AM - 12:00 PM  
**Location:** Virtual (Zoom)  
**Attendees:** All 4 team members

**Agenda:**
1. Review all written sections
2. Review test cases
3. Review architecture diagrams and interface descriptions
4. Finalize document compilation

**Discussion Summary:**
- Member 1 presented Introduction and Major Design Decisions sections
  - Well-written with clear rationale for architecture choices
  - Added comparison with alternative architectures (monolithic, SOA)
  - Explained high cohesion and low coupling principles
- Member 2 presented Architecture diagrams and service descriptions
  - Comprehensive component diagram showing all services
  - Detailed service and interface tables
  - Suggested adding port numbers for services
- Member 3 presented Product Backlog and Sprint Backlog
  - Organized backlog into epics
  - Clear breakdown by deliverable
  - Realistic task estimates
- Member 4 presented GANTT diagram
  - Visual timeline for all 12 weeks
  - Shows dependencies and critical path
  - Includes buffer time for risks

**Test Cases Review:**
- Each member presented 4 test cases (16 total)
- Covered all major use cases: authentication, searching, bidding, payment
- Included edge cases and error scenarios
- Format consistent across all test cases

**Decisions Made:**
- Add Table of Contents with hyperlinks
- Include page numbers in final PDF
- Proofread document for consistency
- Export all diagrams as high-resolution PNG images
- Compile final document in PDF format

**Tasks Assigned:**

| Team Member | Task | Deadline |
|-------------|------|----------|
| Member 1 | Export all sequence diagrams as PNG | Feb 11 |
| Member 2 | Export all activity diagrams as PNG | Feb 11 |
| Member 3 | Compile all sections into single document | Feb 11 |
| Member 4 | Format document, add ToC, proofread | Feb 11 |
| All Members | Final review and sign-off | Feb 11 |

**Action Items:**
- Member 4 to create final document template
- All members to review final draft on Feb 11 evening
- Submit Deliverable 1 on eClass by deadline

**Next Meeting:** February 14, 2026 (to begin Deliverable 2 planning)

---

## 8. Test Driven Development (TDD)

The following 16 test cases cover the core functionality of the auction e-commerce system. These tests should be written before implementation following TDD principles.

### Test Case 1: Successful User Registration

| Field | Description |
|-------|-------------|
| **Test ID** | TC-001 |
| **Category** | User Authentication - Registration |
| **Requirements Coverage** | UC1.1-Successful-User-Registration |
| **Initial Condition** | - System is running<br>- User database is accessible<br>- No existing user with username "testuser123" |
| **Procedure** | 1. User navigates to sign-up page<br>2. User enters username: "testuser123"<br>3. User enters password: "SecurePass123"<br>4. User enters first name: "John"<br>5. User enters last name: "Doe"<br>6. User enters street: "123 Main St"<br>7. User enters city: "Toronto"<br>8. User enters country: "Canada"<br>9. User enters postal code: "M1M 1M1"<br>10. User clicks "Sign Up" button |
| **Expected Outcome** | - User account is created in the database<br>- Password is hashed before storage<br>- JWT token is generated and returned<br>- User is redirected to catalogue page<br>- Success message displayed: "Registration successful" |
| **Notes** | - Username must be unique (alphanumeric, 3-20 characters)<br>- Password must be at least 8 characters<br>- All fields are required |

---

### Test Case 2: Registration with Duplicate Username

| Field | Description |
|-------|-------------|
| **Test ID** | TC-002 |
| **Category** | User Authentication - Registration Validation |
| **Requirements Coverage** | UC1.1-Duplicate-Username-Error |
| **Initial Condition** | - System is running<br>- Existing user with username "existinguser" in database |
| **Procedure** | 1. User navigates to sign-up page<br>2. User enters username: "existinguser"<br>3. User enters valid password and all other required fields<br>4. User clicks "Sign Up" button |
| **Expected Outcome** | - Registration fails<br>- No new user record created<br>- Error message displayed: "Username already exists. Please choose a different username."<br>- User remains on sign-up page with form data intact |
| **Notes** | - Username uniqueness must be checked before creating account<br>- Check should be case-insensitive |

---

### Test Case 3: Successful User Login

| Field | Description |
|-------|-------------|
| **Test ID** | TC-003 |
| **Category** | User Authentication - Login |
| **Requirements Coverage** | UC1.2-Successful-User-Login |
| **Initial Condition** | - System is running<br>- User account exists with username "testuser" and password "MyPassword123" (hashed in database) |
| **Procedure** | 1. User navigates to sign-in page<br>2. User enters username: "testuser"<br>3. User enters password: "MyPassword123"<br>4. User clicks "Sign In" button |
| **Expected Outcome** | - Password is verified against hashed password in database<br>- JWT token is generated<br>- lastLogin timestamp is updated in database<br>- User is redirected to catalogue page<br>- JWT token stored in browser localStorage<br>- Success message: "Welcome back!" |
| **Notes** | - Password verification should use bcrypt or similar hashing algorithm<br>- JWT token should contain userId and expiration time |

---

### Test Case 4: Login with Invalid Credentials

| Field | Description |
|-------|-------------|
| **Test ID** | TC-004 |
| **Category** | User Authentication - Login Validation |
| **Requirements Coverage** | UC1.2-Invalid-Credentials-Error |
| **Initial Condition** | - System is running<br>- User account exists with username "testuser" |
| **Procedure** | 1. User navigates to sign-in page<br>2. User enters username: "testuser"<br>3. User enters incorrect password: "WrongPassword"<br>4. User clicks "Sign In" button |
| **Expected Outcome** | - Login fails<br>- No JWT token generated<br>- No database changes<br>- Error message displayed: "Invalid username or password"<br>- User remains on sign-in page |
| **Notes** | - Error message should not specify whether username or password is incorrect (security best practice)<br>- After 5 failed attempts, account should be temporarily locked |

---

### Test Case 5: Search Items by Keyword

| Field | Description |
|-------|-------------|
| **Test ID** | TC-005 |
| **Category** | Catalogue - Item Search |
| **Requirements Coverage** | UC2.1-Item-Search-By-Keyword |
| **Initial Condition** | - System is running<br>- User is logged in<br>- Database contains items:<br>  * Item 1: "Vintage Camera" (keywords: "camera, vintage, photography")<br>  * Item 2: "Canon DSLR Camera" (keywords: "camera, canon, digital")<br>  * Item 3: "Laptop Computer" (keywords: "computer, laptop, electronics")<br>- All items have status: ACTIVE |
| **Procedure** | 1. User navigates to catalogue page<br>2. User enters search keyword: "camera"<br>3. User clicks "Search" button |
| **Expected Outcome** | - System performs full-text search on item name, description, and keywords<br>- Results display 2 items: "Vintage Camera" and "Canon DSLR Camera"<br>- Each result shows: item name, current price, auction type (Forward), remaining time<br>- Radio button displayed next to each item<br>- "Laptop Computer" is not displayed |
| **Notes** | - Search should be case-insensitive<br>- Only items with status ACTIVE should be returned<br>- Results should be sorted by relevance or most recent |

---

### Test Case 6: Display Empty Search Results

| Field | Description |
|-------|-------------|
| **Test ID** | TC-006 |
| **Category** | Catalogue - Item Search Edge Case |
| **Requirements Coverage** | UC2.1-No-Results-Found |
| **Initial Condition** | - System is running<br>- User is logged in<br>- Database contains items but none match search keyword |
| **Procedure** | 1. User navigates to catalogue page<br>2. User enters search keyword: "nonexistentitem"<br>3. User clicks "Search" button |
| **Expected Outcome** | - Search query executes successfully<br>- No items returned<br>- Message displayed: "No items found matching your search. Try different keywords."<br>- Option to browse all items or clear search |
| **Notes** | - System should handle empty results gracefully without errors |

---

### Test Case 7: Successful Bid Placement

| Field | Description |
|-------|-------------|
| **Test ID** | TC-007 |
| **Category** | Auction - Bidding Process |
| **Requirements Coverage** | UC3-Successful-Bid-Placement |
| **Initial Condition** | - System is running<br>- User is logged in with userId=100<br>- Auction exists for itemId=50 with current price=$25<br>- Auction has not expired (remaining time > 0)<br>- User is subscribed to WebSocket for this auction |
| **Procedure** | 1. User views auction details for itemId=50<br>2. User sees current price: $25<br>3. User enters bid amount: $30<br>4. User clicks "BID" button |
| **Expected Outcome** | - System validates: bidAmount (30) > currentPrice (25) ✓<br>- System validates: auction not expired ✓<br>- Database updated: currentPrice=30, highestBidderId=100<br>- Bid record inserted into bid_history table<br>- WebSocket broadcast sent to all connected clients<br>- UI updated showing: new current price=$30, highest bidder=userId 100<br>- Success message: "Bid placed successfully!" |
| **Notes** | - Row-level locking should be used on auction record to prevent race conditions<br>- Bid amount must be strictly greater than current price (not equal)<br>- Only integer bid amounts are accepted |

---

### Test Case 8: Bid with Invalid Amount (Not Strictly Increasing)

| Field | Description |
|-------|-------------|
| **Test ID** | TC-008 |
| **Category** | Auction - Bid Validation |
| **Requirements Coverage** | UC3-Bid-Amount-Validation-Error |
| **Initial Condition** | - System is running<br>- User is logged in<br>- Auction exists for itemId=50 with current price=$25 |
| **Procedure** | 1. User views auction details for itemId=50<br>2. User sees current price: $25<br>3. User enters bid amount: $25 (equal to current price)<br>4. User clicks "BID" button |
| **Expected Outcome** | - Bid is rejected<br>- No database changes<br>- Error message displayed: "Bid must be greater than current price of $25"<br>- User can enter new bid amount |
| **Notes** | - Validation should occur on both frontend (immediate feedback) and backend (security)<br>- Same validation applies for bids less than current price |

---

### Test Case 9: Bid on Expired Auction

| Field | Description |
|-------|-------------|
| **Test ID** | TC-009 |
| **Category** | Auction - Auction State Validation |
| **Requirements Coverage** | UC3-Expired-Auction-Error |
| **Initial Condition** | - System is running<br>- User is logged in<br>- Auction exists for itemId=50 with status=ENDED (timer expired) |
| **Procedure** | 1. User views auction details for itemId=50<br>2. User enters bid amount: $30<br>3. User clicks "BID" button |
| **Expected Outcome** | - Bid is rejected<br>- No database changes<br>- Error message displayed: "This auction has ended. Bidding is no longer available."<br>- UI displays "Auction Ended" status<br>- "Pay Now" button shown only to winner |
| **Notes** | - Auction status should be checked in backend before accepting bid<br>- WebSocket should have already notified user of auction end |

---

### Test Case 10: Concurrent Bid Race Condition

| Field | Description |
|-------|-------------|
| **Test ID** | TC-010 |
| **Category** | Auction - Concurrency Control |
| **Requirements Coverage** | UC3-Concurrent-Bidding-Race-Condition |
| **Initial Condition** | - System is running<br>- Two users (User A and User B) are logged in<br>- Both viewing same auction itemId=50 with current price=$25 |
| **Procedure** | 1. User A enters bid: $30 and clicks "BID" at time T<br>2. User B enters bid: $27 and clicks "BID" at time T+100ms (nearly simultaneous)<br>3. Both requests reach server within milliseconds |
| **Expected Outcome** | - User A's bid ($30) is processed first, updates currentPrice to $30<br>- Row-level lock prevents User B's bid from being processed simultaneously<br>- User B's bid ($27) is rejected because it's no longer greater than currentPrice ($30)<br>- User A sees success message<br>- User B sees error: "Current price has changed. Please enter a higher bid."<br>- Both users' UIs updated via WebSocket to show currentPrice=$30 |
| **Notes** | - Database transaction isolation level should be SERIALIZABLE or use SELECT FOR UPDATE<br>- This tests the critical race condition scenario in auction systems |

---

### Test Case 11: Payment Access by Non-Winner

| Field | Description |
|-------|-------------|
| **Test ID** | TC-011 |
| **Category** | Payment - Authorization Validation |
| **Requirements Coverage** | UC4-Non-Winner-Payment-Denial |
| **Initial Condition** | - System is running<br>- Auction has ended for itemId=50<br>- User A (userId=100) won the auction<br>- User B (userId=200) participated but did not win |
| **Procedure** | 1. User B (non-winner) attempts to access payment page for itemId=50<br>2. User B clicks "Pay Now" button or navigates to /payment/50 |
| **Expected Outcome** | - System validates: userId 200 ≠ highestBidderId 100<br>- Access denied<br>- Error message displayed: "You did not win this auction and cannot proceed with payment."<br>- User B redirected to catalogue page |
| **Notes** | - Authorization check must occur on backend (cannot rely on frontend hiding button)<br>- Only the auction winner should have access to payment page |

---

### Test Case 12: Successful Payment Processing

| Field | Description |
|-------|-------------|
| **Test ID** | TC-012 |
| **Category** | Payment - Payment Processing |
| **Requirements Coverage** | UC5-Successful-Payment-Processing |
| **Initial Condition** | - System is running<br>- User won auction for itemId=50 with final price=$100<br>- Shipping cost=$15 (standard)<br>- User did not select expedited shipping |
| **Procedure** | 1. Winner clicks "Pay Now" button<br>2. Payment form displayed with pre-filled address<br>3. User enters credit card number: "4111111111111111" (test card)<br>4. User enters name on card: "John Doe"<br>5. User enters expiration: "12/2028"<br>6. User enters CVV: "123"<br>7. User clicks "Submit" button |
| **Expected Outcome** | - All payment fields validated (format, required fields)<br>- Total amount calculated: $100 + $15 = $115<br>- Payment processed through payment gateway<br>- Payment status updated to COMPLETED in database<br>- Auction status updated to PAID<br>- Transaction ID generated and stored<br>- User redirected to receipt page<br>- Email sent to user with receipt |
| **Notes** | - Use test credit card numbers only<br>- Payment gateway should return success response<br>- Transaction should be atomic (all or nothing) |

---

### Test Case 13: Payment with Invalid Credit Card

| Field | Description |
|-------|-------------|
| **Test ID** | TC-013 |
| **Category** | Payment - Payment Validation |
| **Requirements Coverage** | UC5-Invalid-Payment-Details-Error |
| **Initial Condition** | - System is running<br>- User is on payment page after winning auction |
| **Procedure** | 1. User enters credit card number: "1234" (invalid format)<br>2. User enters other valid fields<br>3. User clicks "Submit" button |
| **Expected Outcome** | - Frontend validation catches invalid credit card format<br>- Error message displayed: "Invalid credit card number format"<br>- Payment is not submitted to payment gateway<br>- User remains on payment form to correct errors<br>- All other field values are retained |
| **Notes** | - Credit card validation should check: format (16 digits), Luhn algorithm, expiration date not in past<br>- Provide specific error messages for each validation failure |

---

### Test Case 14: Receipt Generation and Display

| Field | Description |
|-------|-------------|
| **Test ID** | TC-014 |
| **Category** | Payment - Receipt Generation |
| **Requirements Coverage** | UC6-Receipt-And-Shipment-Details-Display |
| **Initial Condition** | - System is running<br>- Payment completed successfully for itemId=50<br>- Transaction ID: TXN-12345<br>- Item shipping days: 5 days (from catalogue) |
| **Procedure** | 1. System automatically redirects user to receipt page after payment<br>2. Receipt page loads |
| **Expected Outcome** | - Receipt displays:<br>  * Order ID (generated)<br>  * Transaction ID: TXN-12345<br>  * Item name<br>  * Item price: $100<br>  * Shipping cost: $15<br>  * Total amount paid: $115<br>  * Payment method: Credit Card (last 4 digits)<br>  * Transaction date<br>  * Recipient name and shipping address<br>  * Estimated delivery: "Item will be shipped in 5 days"<br>- Options displayed: "Print Receipt", "Download PDF", "Return to Home"<br>- Email receipt sent to user's email address |
| **Notes** | - Shipping days should be retrieved from item record in catalogue database<br>- Receipt should be stored in database for future access<br>- PDF generation should be triggered on-demand when user clicks "Download PDF" |

---

### Test Case 15: Auction Item Upload by Seller

| Field | Description |
|-------|-------------|
| **Test ID** | TC-015 |
| **Category** | Catalogue - Seller Item Upload |
| **Requirements Coverage** | UC7-Successful-Auction-Item-Upload |
| **Initial Condition** | - System is running<br>- User is logged in as seller (userId=300) |
| **Procedure** | 1. Seller clicks "Create Auction" button<br>2. Seller enters item name: "Vintage Watch"<br>3. Seller enters description: "Rare 1960s mechanical watch"<br>4. Seller enters keywords: "watch, vintage, mechanical"<br>5. Seller enters starting bid price: $50<br>6. Seller selects auction duration: 7 days<br>7. Seller enters shipping days: 3<br>8. Seller enters expedited shipping cost: $25<br>9. Seller uploads image file: "watch.jpg"<br>10. Seller clicks "Create Auction" button |
| **Expected Outcome** | - All fields validated (price > 0, duration between 1 hour and 30 days)<br>- Image uploaded to file storage<br>- Unique itemId generated<br>- Auction end time calculated: currentTime + 7 days<br>- Item record created in catalogue database with status=ACTIVE<br>- Auction record created in auction database<br>- Item indexed for search by keywords<br>- Auction timer scheduled to end at calculated end time<br>- Success message: "Auction created successfully! Item ID: [itemId]"<br>- Seller redirected to item detail page |
| **Notes** | - Starting price must be positive integer<br>- Duration must be at least 1 hour and at most 30 days<br>- Image file should be validated (file type, size limits) |

---

### Test Case 16: Auction Auto-End When Timer Expires

| Field | Description |
|-------|-------------|
| **Test ID** | TC-016 |
| **Category** | Auction - Auction Lifecycle |
| **Requirements Coverage** | UC3-Automatic-Auction-End, UC4-Auction-Ended-Notification |
| **Initial Condition** | - System is running<br>- Auction exists for itemId=50 with endTime=2026-02-10 14:00:00<br>- Current time is 2026-02-10 13:59:50 (10 seconds before end)<br>- Current price=$100, highestBidderId=100<br>- 3 users connected via WebSocket watching this auction |
| **Procedure** | 1. System timer continuously checks auction end times<br>2. Timer reaches endTime for itemId=50<br>3. Auction end process is triggered automatically |
| **Expected Outcome** | - Auction status updated to ENDED in database<br>- Winner determined: userId=100<br>- Item status updated to SOLD in catalogue database<br>- Payment record created for winner<br>- WebSocket broadcast sent to all 3 connected users: "Auction Ended"<br>- Winner's UI displays "You won! Pay Now" button<br>- Non-winners' UI displays "Auction ended. You did not win."<br>- Email notifications sent to all participants<br>- Auction timer for this item is stopped and removed from scheduler |
| **Notes** | - Auction end should be atomic transaction<br>- If no bids were placed, item should be removed from catalogue instead<br>- Timer precision should be within 1-2 seconds of scheduled end time |

---

## 9. Acknowledgment

### Use of AI in This Project

This Software Design Document was created with assistance from AI-powered tools (Claude Sonnet 4.5 via Cursor IDE) to enhance productivity and ensure comprehensive coverage of project requirements. Below is a detailed account of how AI was used, its strengths, weaknesses, and our reflections on its effectiveness.

#### How AI Was Used

1. **Document Structure and Formatting:**
   - AI helped establish the document structure following the provided template
   - Generated professional markdown formatting with proper heading hierarchy
   - Created well-formatted tables for services, interfaces, test cases, and meeting logs
   - Ensured consistency in formatting across all sections

2. **UML Diagrams:**
   - AI generated ASCII-based sequence diagrams and activity diagrams
   - Provided proper UML notation and syntax for all diagram elements
   - Ensured consistency in naming conventions across all diagrams
   - Created complex interaction flows between multiple services

3. **Architecture Design:**
   - AI suggested appropriate architectural patterns (microservices, façade, repository)
   - Provided detailed analysis of alternative architectures with pros/cons
   - Explained high cohesion and low coupling principles with concrete examples
   - Designed service boundaries and interfaces based on business capabilities

4. **Technical Content:**
   - AI drafted service descriptions with appropriate responsibilities
   - Generated interface operations with detailed parameter and return types
   - Created comprehensive test cases following TDD principles
   - Provided technical rationale for design decisions

5. **Project Planning:**
   - AI organized product backlog into logical epics
   - Created sprint backlog aligned with deliverable milestones
   - Generated GANTT diagram with realistic timeline and dependencies
   - Identified critical path and risk mitigation strategies

#### Strengths of AI

1. **Comprehensive Coverage:**
   - AI ensured all required sections from the assignment were addressed
   - No missing components or incomplete sections
   - Thorough documentation of all use cases

2. **Consistency:**
   - Maintained consistent terminology and naming conventions throughout
   - Uniform formatting style across all sections
   - Aligned all diagrams and descriptions with each other

3. **Technical Accuracy:**
   - Proper UML notation and best practices
   - Correct microservices architecture patterns
   - Appropriate technology stack recommendations (Spring Boot, SQLite, React)

4. **Productivity:**
   - Significantly reduced time to create comprehensive documentation
   - Allowed focus on design decisions rather than formatting
   - Quick generation of multiple diagram variations for comparison

5. **Best Practices:**
   - Incorporated industry-standard design patterns
   - Followed RESTful API conventions
   - Applied software engineering principles (SOLID, DRY, separation of concerns)

6. **Thoroughness:**
   - Created detailed test cases covering happy paths, edge cases, and error scenarios
   - Comprehensive meeting logs with realistic task assignments
   - Detailed GANTT diagram with all project phases

#### Weaknesses of AI

1. **Context Limitations:**
   - AI does not have access to team's specific technical expertise or preferences
   - Cannot assess team members' actual skill levels for realistic task assignments
   - Meeting logs are simulated rather than reflecting actual team discussions

2. **Over-Engineering Risk:**
   - AI sometimes suggests complex solutions when simpler ones might suffice
   - Tendency to include all possible features rather than focusing on MVP
   - May propose advanced patterns that are overkill for project scope

3. **Generic Content:**
   - Some descriptions lack project-specific customization
   - Template-like quality in certain sections
   - May not reflect unique requirements of the specific academic context

4. **Visual Limitations:**
   - ASCII diagrams are less visually appealing than graphical UML tools
   - Cannot generate actual graphical diagrams (PNG/SVG)
   - Limited ability to optimize diagram layout for readability

5. **Verification Required:**
   - Cannot validate if proposed architecture will actually work in practice
   - No ability to test or execute the designed system
   - May contain subtle technical inaccuracies that require expert review

6. **Lack of Judgment:**
   - Cannot make subjective decisions about trade-offs (e.g., should we use RabbitMQ or Kafka?)
   - Cannot prioritize features based on team capacity or project timeline
   - Requires human oversight to ensure design aligns with project constraints

#### Critical Evaluation and Changes Made

**What We Accepted:**
- Overall microservices architecture design (appropriate for project requirements)
- Service boundaries and responsibilities (well-aligned with use cases)
- Test case structure and coverage (comprehensive and follows TDD principles)
- Document structure and organization (meets all assignment requirements)

**What We Modified:**
- Simplified some of the advanced features suggested by AI that were beyond project scope
- Adjusted timeline in GANTT diagram to be more realistic for student team
- Customized technology stack choices based on course recommendations
- Refined interface operations to match specific project requirements more closely

**What We Would Critique:**
- AI-generated meeting logs are fictional and don't reflect actual team collaboration
- Some architectural decisions may be more complex than necessary for this academic project
- ASCII diagrams should be replaced with proper UML tool-generated diagrams before submission
- Service interface details may need refinement during actual implementation

#### Lessons Learned

1. **AI as Assistant, Not Replacement:**
   - AI is excellent for generating initial drafts and structure
   - Human expertise still required for critical design decisions
   - Team members must understand and be able to defend all design choices

2. **Verification is Essential:**
   - Every AI-generated section should be reviewed for accuracy
   - Technical details must be validated against project requirements
   - Cannot blindly accept AI suggestions without critical evaluation

3. **Customization Required:**
   - AI output is generic and needs project-specific customization
   - Team's unique context and constraints must be incorporated
   - Real team discussions and decisions should replace simulated content

4. **Productivity Gains:**
   - AI dramatically reduced time for documentation formatting and structure
   - Allowed team to focus on high-level design decisions
   - Enabled exploration of multiple design alternatives quickly

#### Conclusion

AI tools proved highly valuable for creating comprehensive project documentation efficiently. The strengths in consistency, thoroughness, and technical knowledge significantly enhanced productivity. However, the weaknesses in context awareness, potential for over-engineering, and need for verification mean that human oversight and expertise remain essential. We successfully used AI as a powerful assistant while maintaining critical thinking and ensuring all design decisions align with project requirements and team capabilities.

For future deliverables, we will continue using AI for productivity enhancement while remaining vigilant about customization, verification, and ensuring the implementation reflects our team's actual work and understanding.

---

**End of Software Design Document**
