# EECS 4413 Project Report - Deliverable 3

## 1. Assignment 3 Scope

This deliverable completes the frontend/UI layer for PrimeBid, documents the Assignment 3 distinguishable feature, and consolidates testing and deployment evidence for the final system.

For this submission pass, the focus is:
- frontend coverage for UC1 to UC7
- the distinguishable feature: PrimeBid AI chatbot and auto-bid assistant
- frontend robustness and client-side validation
- final design/report updates required by the A3 rubric

Backend functionality, backend automated tests, and backend service behavior were treated as already completed and were not changed as part of this update.

## 2. Team Member Contributions

Replace this section with your real team information before submission.

| Member | Contribution summary |
| --- | --- |
| Member 1 | Frontend route implementation, auth pages, catalogue/detail UI |
| Member 2 | AI assistant UI, chatbot interaction flow, auto-bid frontend integration |
| Member 3 | Docker packaging, Compose setup, deployment validation, README/testing docs |
| Member 4 | Backend services, API design, robustness testing, payment/receipt integration |

## 3. Distinguishable Feature: PrimeBid AI

### 3.1 Feature Summary

PrimeBid AI is the Assignment 3 distinguishable feature. It extends the item-detail page with a buyer-facing conversational assistant that:

- explains auction state in natural language
- recommends bidding strategies
- converts a user's request into a structured auto-bid action
- starts, monitors, and stops an auto-bid session from the UI

From the frontend perspective, the AI capability is surfaced through the `AiBidAssistant` component on the item-detail page. The assistant is available only to authenticated users and is embedded directly into the live bidding experience instead of being separated into a back-office screen.

### 3.2 User-Facing Workflow

The user workflow for PrimeBid AI is:

1. Open an active item detail page.
2. Expand the `PrimeBid AI` panel.
3. Enter a budget or ask a natural-language question about strategy or bidding.
4. Review the assistant's response and any suggested action.
5. Confirm activation of auto-bidding if the assistant returns a recommendation.
6. Monitor live status and optionally stop the session.

### 3.3 Technical Design Summary

The distinguishable feature spans both frontend and gateway integration:

- Frontend component: `frontend/app/components/AiBidAssistant.tsx`
- Item page integration: `frontend/app/(main)/catalogue/[id]/page.tsx`
- Gateway endpoints:
  - `POST /auto-bid/chat`
  - `POST /auto-bid/start`
  - `GET /auto-bid/status/{sessionId}`
  - `POST /auto-bid/stop/{sessionId}`
- Backend advisor and prompt orchestration:
  - `gateway/src/main/java/com/forwardauction/gateway/AutoBidController.java`
  - `gateway/src/main/java/com/forwardauction/gateway/autobid/GenAiAdvisor.java`

The current prompt design injects live auction context before each AI call, including current price, starting price, time remaining, highest bidder, number of bids, and whether the user already has an active auto-bid session. That context is used to keep the assistant grounded in the current auction state instead of responding only from generic LLM behavior.

### 3.4 Validation Approach

Because LLM output is non-deterministic, validation for PrimeBid AI should be based on behavior classes rather than exact sentence matching. The test suite therefore verifies:

- the assistant opens and closes correctly in the UI
- authenticated users can submit prompts
- strategy suggestions are rendered safely
- the assistant can produce an actionable auto-bid recommendation
- active auto-bid status is displayed and refreshed
- stopping a session updates the UI state cleanly
- seller misuse, low budgets, duplicate sessions, and ended-auction states are handled without crashing the page

If your team already ran a prompt-evaluation spreadsheet or sample prompt set, insert the measured AI quality summary here before final submission. Do not invent accuracy numbers. A simple acceptable table is:

| Validation dimension | Example metric to report | Your measured result |
| --- | --- | --- |
| Intent detection | Start / stop / analyze classification accuracy | `TBD - replace with measured value` |
| Strategy recommendation quality | Correct strategy class for scenario set | `TBD - replace with measured value` |
| Budget adherence | Sessions never exceed requested max bid | `TBD - replace with measured value` |
| Failure handling | Graceful fallback/error rendering rate | `TBD - replace with measured value` |

## 4. Frontend Implementation Coverage

The current frontend covers all major user journeys required by UC1 to UC7.

| Use case | Frontend route(s) | Coverage summary |
| --- | --- | --- |
| UC1 Registration and Login | `/signup`, `/login`, `/dashboard/settings` | Signup, login, logout, and password reset UI |
| UC2 Browse/Search Catalogue | `/`, `/catalogue`, `/catalogue/[id]` | Landing page, catalogue browsing, search, item detail |
| UC3 Place Bid | `/catalogue/[id]` | Bid placement, bid validation, bid history, success/error states |
| UC4 View Auction Result | `/catalogue/[id]` | Winner, non-winner, unsold, and logged-out ended states |
| UC5 Payment | `/payment/[id]` | Checkout, shipping selection, card form, expiry validation |
| UC6 Receipt | `/receipt/[id]` | Payment confirmation, totals, card summary, return path |
| UC7 Create Item and Auction | `/dashboard/sell` | Seller item creation with auction metadata |

Additional frontend pages and shared UI areas covered in this deliverable:

- home page
- navbar and mobile menu
- responsive auth-page menu container
- security settings page
- PrimeBid AI assistant on the item detail page

## 5. Frontend and AI Testing Report

### 5.1 Test Artifact

The complete frontend and AI chatbot manual test suite is documented in:

- `docs/frontend-testing/A3_FRONTEND_AI_TEST_CASES.md`

That document is the main traceability artifact for Assignment 3 frontend verification. It maps the UI to:

- UC1 to UC7
- shared navigation and mobile layout behavior
- client-side validation
- the PrimeBid AI distinguishable feature

### 5.2 Coverage Summary

The documented suite includes the following areas:

- home page and shared navigation
- signup, login, logout, and password reset
- seller item creation flow
- catalogue browse, search, item-detail navigation, and selection
- bidding and bid-history states
- ended-auction result states
- payment and receipt pages
- PrimeBid AI open/close, analysis, recommendation, start, stop, polling, and error handling
- responsive/mobile verification for the most visible user journeys

### 5.3 Why This Satisfies the A3 Frontend Testing Requirement

The rubric asks for evidence that the UI works, supports navigation, and includes robustness validations. This test suite addresses that by checking:

- happy paths for all main use cases
- negative paths and client-side validation
- authenticated vs unauthenticated behavior
- usability of the AI-assisted flow
- payment validation on the client side
- mobile layout survivability for the most important screens

## 6. Testing Reliability Report

The rubric explicitly asks for a software reliability estimate using the assumption `v0 = 500`. The Jelinski-Moranda-style reasoning below can be included directly in the final report.

### 6.1 Assumptions

- Initial latent defects: `v0 = 500`
- Defect discovery proportionality constant: `Phi = 0.003` per CPU testing hour

### 6.2 Failure Rate Model

Let `mu(t)` be the cumulative number of discovered defects after `t` CPU testing hours.

`mu(t) = v0 * (1 - e^(-Phi * t))`

The instantaneous failure rate is:

`lambda(t) = Phi * (v0 - mu(t))`

### 6.3 CPU Testing Time to Remove Nearly All Defects

To estimate the testing effort needed to eliminate approximately `499` of the `500` defects:

`499 = 500 * (1 - e^(-0.003 * t))`

`0.998 = 1 - e^(-0.003 * t)`

`e^(-0.003 * t) = 0.002`

`-0.003 * t = ln(0.002)`

`t = -ln(0.002) / 0.003`

`t ~= 2071.5 CPU hours`

### 6.4 Reliability Conclusion

Under the stated assumptions, approximately `2071.5 CPU hours` of testing would be required to reduce the original latent defect count from `500` to roughly `1` remaining defect. This value should be interpreted as a planning estimate, not as a measured result for the current repository.

## 7. Robustness and Security Validation

Frontend robustness and security-relevant behavior are addressed in the UI through the following areas:

- required auth forms and disabled submits for incomplete inputs
- password reset validation and error reporting
- seller form validation for item and auction creation
- bid input validation and clear negative feedback
- payment expiry-date validation preventing past dates
- unauthorized action handling for protected operations
- AI assistant error handling when a request fails or when the auction state makes the action invalid

The backend test suite and API-level robustness scripts remain the source of truth for service-level security, scalability, and malformed-request validation. This report section focuses only on how those conditions surface in the frontend.

## 8. Performance Report

The rubric also asks for a performance report using JMeter. Because this repository does not contain measured JMeter output files, this report should not fabricate response-time numbers.

If your team already ran JMeter against the gateway/backend, paste the measured values into the table below before submission:

| Arrival rate (req/sec) | Average response time | Error rate | Observed notes |
| --- | --- | --- | --- |
| `TBD` | `TBD` | `TBD` | `TBD` |
| `TBD` | `TBD` | `TBD` | `TBD` |
| `TBD` | `TBD` | `TBD` | `TBD` |

Suggested write-up:

- identify the endpoints included in the load mix
- describe ramp-up strategy and concurrency
- explain where latency started to rise sharply
- note whether contention, timeouts, or queueing appeared

This keeps the report truthful while still aligning with the rubric.

## 9. Diagram Updates Included for Assignment 3

The updated Deliverable 3 diagram set is stored in:

- `docs/diagrams deliverable 3/component_d3_as_built.png`
- `docs/diagrams deliverable 3/deployment_a3_containers.png`
- `docs/diagrams deliverable 3/use_case_a3_primebid_ai.png`
- `docs/diagrams deliverable 3/sequence_uc8_ai_autobid_a3.png`

These diagrams now extend the Deliverable 2 views so the Assignment 3 frontend and AI work are represented explicitly.

### 9.1 Use Case Diagram

The Deliverable 3 use case diagram adds the buyer-facing AI use case:

- `UC8 - PrimeBid AI Advisor / Auto-Bid Assistant`

That view connects the buyer actor to:

- ask AI for bidding analysis
- receive a strategy recommendation
- start auto-bidding
- stop auto-bidding

### 9.2 Sequence Diagram

The Deliverable 3 sequence diagram covers:

- frontend item detail page
- `AiBidAssistant`
- gateway `AutoBidController`
- `GenAiAdvisor`
- session store and auto-bid store
- external Gemini API
- auction service status lookup

The sequence shows:

1. user prompt submission
2. AI response returned to frontend
3. user confirmation to start auto-bid
4. session creation
5. status polling
6. stop action

### 9.3 Component / Deployment Diagram

The Deliverable 3 component and deployment diagrams show:

- Next.js frontend container
- gateway container
- IAM, catalogue, auction, and payment containers
- PrimeBid AI UI component in the frontend
- gateway AI orchestration classes
- Gemini integration boundary
- Docker Compose as the local deployment topology

### 9.4 Activity / Payment Flow Diagrams

The existing activity and payment-related diagrams should be checked against the final submitted frontend so they reflect:

- auth page navigation as implemented now
- payment expiry-date validation before submission
- receipt page after successful payment

## 10. Submission Notes

Before final submission, replace the remaining placeholders with real values:

- actual team member names and exact contributions
- actual JMeter results from your backend performance run
- actual AI validation numbers if your team measured them
- updated UML/deployment screenshots with readable fonts

## 11. References to Project Artifacts

- Frontend test suite: `docs/frontend-testing/A3_FRONTEND_AI_TEST_CASES.md`
- Updated Deliverable 3 diagrams:
  - `docs/diagrams deliverable 3/component_d3_as_built.png`
  - `docs/diagrams deliverable 3/deployment_a3_containers.png`
  - `docs/diagrams deliverable 3/use_case_a3_primebid_ai.png`
  - `docs/diagrams deliverable 3/sequence_uc8_ai_autobid_a3.png`
- Existing use-case API walkthroughs:
  - `INDIVIDUAL_USE_CASE_TESTING.md`
  - `TESTING_INSTRUCTIONS.md`
- Existing D2 diagram assets:
  - `docs/diagrams/sequence_uc1_auth_d2.png`
  - `docs/diagrams/sequence_uc2_browse_select_d2.png`
  - `docs/diagrams/sequence_uc3_bid_d2.png`
  - `docs/diagrams/sequence_uc5_uc6_payment_receipt_d2.png`
  - `docs/diagrams/component_d2_as_built.png`

## 12. Final Summary

For Assignment 3, the frontend now has a complete documented testing plan that covers the full user-facing system plus the new PrimeBid AI capability. The remaining manual work before submission is not code work; it is report completion work:

- fill in real team/member details
- paste measured load-test and AI-validation results
- update diagrams so they explicitly show the AI assistant and current containerized architecture
