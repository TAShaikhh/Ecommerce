# Assignment 3 Frontend and AI Chatbot Test Cases

## 1. Scope

This document defines the comprehensive frontend test suite for Assignment 3.

In scope:
- all user-facing UI routes in the Next.js frontend
- all frontend flows for UC1 to UC7
- the distinguishable Assignment 3 feature: PrimeBid AI chatbot and auto-bid assistant
- client-side validation, navigation, usability, and negative-path behavior

Out of scope:
- backend unit tests and backend implementation changes
- backend load-test implementation details
- database-level validation already covered by backend testing
- browser screenshot automation

## 2. Frontend Routes Under Test

| Route | Page | Primary coverage |
| --- | --- | --- |
| `/` | Home | landing page, navigation entry points |
| `/signup` | Sign up | UC1 registration |
| `/login` | Login | UC1 authentication |
| `/catalogue` | Catalogue | UC2 browse and search |
| `/catalogue/[id]` | Item detail | UC2, UC3, UC4, AI chatbot |
| `/dashboard/sell` | Sell item | UC7 create item and auction |
| `/dashboard/settings` | Security settings | password reset |
| `/payment/[id]` | Payment page | UC5 payment |
| `/receipt/[id]` | Receipt page | UC6 receipt |

## 3. Test Environment

- Frontend running at `http://localhost:3000`
- Gateway running at `http://localhost:8080`
- IAM, catalogue, auction, and payment services reachable through the gateway
- Docker Compose or local service startup already completed
- Gemini API key configured if live AI responses are expected

Recommended test accounts:
- `seller_a3`
- `buyer_a3`
- `buyer_competitor_a3`

Recommended reusable test data:
- one active auction item
- one expired item with no bids
- one sold item with the logged-in user as winner
- one sold item with the logged-in user as non-winner

### 3.1 What This Test Suite Means for Frontend Testing

For Assignment 3, frontend testing should be interpreted primarily as **functional testing of the UI**:

- verify that pages render correctly
- verify that navigation and user flows work
- verify that client-side validation blocks bad input
- verify that backend failures surface safely in the interface
- verify that the AI assistant behaves correctly from the user's point of view

This means the frontend can be tested in two complementary ways:

- **functional/manual test cases**: the user executes the steps and checks the expected result
- **automated end-to-end tests**: tools such as Playwright or Cypress run the same scenarios automatically

For the AI chatbot, exact wording is not the right assertion target. AI validation should be based on:

- whether the assistant returns a usable answer
- whether it recommends a valid action when appropriate
- whether it respects budget and auction state constraints
- whether the UI renders success, error, and fallback states safely

### 3.2 Edge-Case Coverage Included in This Suite

This suite intentionally includes edge cases, not only happy paths. The covered edge-case categories include:

- duplicate signup and invalid login
- missing required auth, seller, bidding, and payment inputs
- unauthorized access to seller-only or winner-only pages
- invalid bid amounts and ended-auction bidding attempts
- invalid payment expiry dates and payment access by non-winners
- seller misuse of the AI auto-bid flow
- low-budget, duplicate-session, ended-auction, and degraded-response AI scenarios

## 4. Traceability Matrix

| Use case / feature | Frontend pages | Test IDs |
| --- | --- | --- |
| UC1 Registration and Login | `/signup`, `/login`, `/dashboard/settings`, navbar auth controls | `NAV-01` to `NAV-04`, `AUTH-01` to `AUTH-10`, `SET-01` to `SET-04` |
| UC2 Browse, Search, View, Select Item | `/`, `/catalogue`, `/catalogue/[id]` | `HOME-01` to `HOME-03`, `CAT-01` to `CAT-08` |
| UC3 Place Bid and View Bid History | `/catalogue/[id]` | `BID-01` to `BID-07` |
| UC4 Auction Result | `/catalogue/[id]` | `AUC-01` to `AUC-05` |
| UC5 Payment | `/payment/[id]` | `PAY-01` to `PAY-09` |
| UC6 Receipt | `/receipt/[id]` | `REC-01` to `REC-05` |
| UC7 Create Item and Auction | `/dashboard/sell`, `/catalogue`, `/catalogue/[id]` | `SELL-01` to `SELL-08` |
| A3 Distinguishable Feature: PrimeBid AI | `/catalogue/[id]` | `AI-01` to `AI-14` |

## 5. Shared Navigation and Layout Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `NAV-01` | Verify landing page entry points | User is logged out | Open `/` | Home page loads without console-breaking UI errors; primary CTA goes to catalogue; secondary CTA goes to signup |
| `NAV-02` | Verify auth navbar while logged out | User is logged out | Open `/catalogue` on desktop and mobile widths | Navbar shows `Catalogue`, `Sign in`, and `Create account`; mobile menu opens and closes correctly |
| `NAV-03` | Verify auth navbar while logged in | User is logged in | Open `/catalogue` | Navbar shows `Sell Item`, `Settings`, user identity chip, and sign-out control |
| `NAV-04` | Verify auth pages hide the shared navbar | User is logged out | Open `/login` and `/signup` | Global navbar is hidden on auth pages; only the auth page menu container is visible |

## 6. Home Page Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `HOME-01` | Verify home page content renders | None | Open `/` | Hero title, CTA buttons, and feature cards render correctly |
| `HOME-02` | Verify route to catalogue from home | None | Click `Enter Catalogue` | User is routed to `/catalogue` |
| `HOME-03` | Verify route to signup from home | None | Click `Create account` | User is routed to `/signup` |

## 7. UC1 Authentication Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `AUTH-01` | Register a new user successfully | Unique username available | Open `/signup`, complete all fields, submit | User is redirected to `/login?registered=true`; success banner appears |
| `AUTH-02` | Validate required signup fields | None | Submit signup form with one or more blank fields | Browser blocks submission or UI prevents submit until required fields are provided |
| `AUTH-03` | Validate minimum signup password length | None | Enter password shorter than 6 characters and submit | Submission is blocked or validation message appears |
| `AUTH-04` | Handle duplicate username on signup | Existing username already present | Submit signup form with an existing username | Error banner appears and the page stays usable |
| `AUTH-05` | Login successfully with valid credentials | Registered user exists | Open `/login`, enter valid credentials, submit | User is routed to `/catalogue`; authenticated navbar state is shown |
| `AUTH-06` | Reject invalid login credentials | Registered user exists | Attempt login with wrong password | Error banner appears; user remains on login page |
| `AUTH-07` | Prevent login submit with empty fields | None | Leave username or password blank and attempt submit | Submit button remains disabled or browser blocks submission |
| `AUTH-08` | Preserve post-signup feedback | A user has just registered | Land on `/login?registered=true` | Success state is visible and instructs the user to sign in |
| `AUTH-09` | Verify logout behavior | Logged-in user session exists | Click sign-out from navbar or mobile menu | Session clears and navbar returns to logged-out state |
| `AUTH-10` | Prevent unauthorized privileged actions | User is logged out | Attempt seller-only or payment-only action from UI | Action does not complete; UI shows error or redirects to sign in |

## 8. Settings and Password Reset Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `SET-01` | Load security settings page | Logged-in user | Open `/dashboard/settings` | Security settings page loads; reset password form is visible |
| `SET-02` | Reset password successfully | Logged-in user knows current password | Enter valid current password and valid new password, then submit | Success banner appears; password fields clear |
| `SET-03` | Reject incorrect current password | Logged-in user | Enter wrong current password and submit | Error banner appears; page stays usable |
| `SET-04` | Enforce required password-reset inputs | Logged-in user | Leave either field blank and attempt submit | Submit is blocked until both fields are provided |

## 9. UC7 Seller Item Creation Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `SELL-01` | Open seller listing page | Logged-in seller | Open `/dashboard/sell` | Seller form renders with asset details and logistics sections |
| `SELL-02` | Create an item successfully | Logged-in seller | Fill all required fields with valid values and submit | User is routed to `/catalogue/[itemId]`; item page shows the new listing |
| `SELL-03` | Validate required seller form fields | Logged-in seller | Leave a required field blank and attempt submit | Submission is blocked or a visible error is shown |
| `SELL-04` | Validate numeric constraints on seller form | Logged-in seller | Enter invalid values such as negative price or invalid duration | UI prevents bad input or backend error is surfaced in-page without breaking layout |
| `SELL-05` | Verify create-item CTA status feedback | Logged-in seller | Submit valid form | Button switches to loading state and prevents duplicate submits |
| `SELL-06` | Verify created item appears in catalogue | Newly created item exists | Return to `/catalogue` | New item is visible in the list with correct title and status |
| `SELL-07` | Verify seller item detail information | Newly created item exists | Open the item detail page | Description, condition, keywords, shipping, and seller metadata display correctly |
| `SELL-08` | Reject unauthenticated item creation | Logged-out user | Attempt to submit seller form | Item is not created; UI surfaces authorization failure gracefully |

## 10. UC2 Catalogue and Item Detail Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `CAT-01` | Load catalogue page successfully | None | Open `/catalogue` | Catalogue title and search UI appear; item cards render if data exists |
| `CAT-02` | Search catalogue by keyword | At least one matching item exists | Enter search term in the search box | Matching items are shown and irrelevant items are filtered out |
| `CAT-03` | Empty search state handling | Search term has no matches | Search for a non-existent keyword | Empty state message appears without breaking page layout |
| `CAT-04` | Open item detail page | At least one item exists | Click a catalogue card | User is routed to `/catalogue/[id]` |
| `CAT-05` | Verify active-auction detail view | Active item exists | Open active item detail page | Current price, timer, seller data, and bid history region display correctly |
| `CAT-06` | Verify unauthenticated bid prompt | Logged-out user on active item | Open item detail page | Bid area shows sign-in prompt instead of allowing a completed bid |
| `CAT-07` | Verify bid-history rendering | Item has bids | Open item detail page | Bid history shows bids ordered with highest highlighted |
| `CAT-08` | Verify countdown state transition | Active auction close to end | Watch item detail through auction expiry | UI transitions from active to ending/finalizing to ended state without crashing |

## 11. UC3 Bidding Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `BID-01` | Place a valid bid successfully | Logged-in buyer, active auction, item selected or selectable | Enter a bid above current price and submit | Success message appears; current price and bid history update |
| `BID-02` | Reject bid below required increment | Logged-in buyer, active auction | Enter a bid below the displayed minimum and submit | Bid is rejected and a clear error message is shown |
| `BID-03` | Reject empty bid submission | Logged-in buyer, active auction | Leave bid field blank and attempt submit | Submit remains disabled or request is not sent |
| `BID-04` | Verify loading state during bidding | Logged-in buyer, active auction | Submit a valid bid | Button enters loading state and duplicate submission is prevented |
| `BID-05` | Verify bid history updates after own bid | Logged-in buyer | Place a bid successfully | New bid appears in bid history and highest state updates |
| `BID-06` | Verify another user can outbid | Two buyers available on same active auction | Buyer A bids, then Buyer B bids higher | Highest bid and leader information update correctly on reload/poll |
| `BID-07` | Prevent bid attempt after auction ended | Logged-in buyer, ended auction | Attempt to bid after result is final | Bid form is no longer active; user sees ended/result state instead |

## 12. UC4 Auction Result Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `AUC-01` | Winner sees sold result state | Logged-in winning buyer, sold item | Open ended item detail page | Winner banner appears and payment CTA is visible |
| `AUC-02` | Non-winner sees sold result state | Logged-in non-winning buyer, sold item | Open ended item detail page | Winner name and final price show; payment CTA is not offered |
| `AUC-03` | Seller sees ended result state | Logged-in seller of the item | Open ended item detail page | Auction is shown as concluded with winner/final result information |
| `AUC-04` | Logged-out user sees limited ended state | Logged-out user, ended item | Open ended item detail page | Sign-in prompt is shown for private result details |
| `AUC-05` | Unsold auction is handled correctly | Ended item with no bids | Open ended item detail page | UI shows `Auction Ended` or `No bids` style message instead of payment path |

## 13. UC5 Payment Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `PAY-01` | Load payment page for winner | Logged-in winner with payable item | Open `/payment/[id]` | Secure checkout page loads with order summary and shipping address |
| `PAY-02` | Toggle expedited shipping | Logged-in winner on payment page | Toggle expedited shipping on and off | Total updates correctly and toggle state is reflected visually |
| `PAY-03` | Submit payment with valid details | Logged-in winner on payment page | Enter valid card name, card number, expiry, and CVC, then submit | User is redirected to receipt page |
| `PAY-04` | Reject past expiry date | Logged-in winner on payment page | Enter an expiry date in the past and submit | Client-side error message appears and payment is blocked |
| `PAY-05` | Reject invalid expiry month format | Logged-in winner on payment page | Enter month `00` or `13` | Validation prevents payment submission |
| `PAY-06` | Enforce required payment fields | Logged-in winner on payment page | Leave one or more payment fields blank and submit | Submission is blocked or page shows validation errors |
| `PAY-07` | Prevent unauthorized payment access | Logged-in non-winner or logged-out user | Open `/payment/[id]` for another user's item | Payment cannot be completed; UI surfaces access failure safely |
| `PAY-08` | Verify payment button loading state | Logged-in winner on payment page | Submit valid payment | Button shows processing state and blocks duplicate submits |
| `PAY-09` | Verify payment page survives backend errors gracefully | Simulate failed payment or unavailable downstream service | Submit payment | Error banner appears and the page remains usable for correction/retry |

## 14. UC6 Receipt Tests

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `REC-01` | Load receipt page after successful payment | Payment just completed | Follow redirect to `/receipt/[paymentId]` | `Payment Confirmed` page appears |
| `REC-02` | Verify receipt financial summary | Successful payment exists | Inspect receipt page | Winning bid, shipping, and total paid values are displayed |
| `REC-03` | Verify masked card and shipping message | Successful payment exists | Inspect receipt metadata card | Cardholder name, masked last four digits, and shipping message are shown |
| `REC-04` | Verify return path to catalogue | Successful receipt exists | Click `Return to Catalogue` | User is routed back to `/catalogue` |
| `REC-05` | Verify invalid receipt access handling | Unknown or unauthorized receipt id | Open `/receipt/[badId]` | Friendly error state is shown without app crash |

## 15. A3 PrimeBid AI Chatbot and Auto-Bid Tests

Important note:
- expected AI wording should be evaluated semantically, not by exact sentence match
- the frontend should be judged on response rendering, action affordances, validation, and failure handling

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `AI-01` | AI assistant is hidden for logged-out users | Logged-out user on item detail page | Open an active item detail page | PrimeBid AI panel is not shown |
| `AI-02` | Open AI assistant as authenticated buyer | Logged-in buyer on active item | Click `PrimeBid AI` | Assistant expands with welcome message, budget field, prompts, and input box |
| `AI-03` | Close AI assistant cleanly | Logged-in buyer on active item | Open assistant, then click `Close AI Assistant` | Panel collapses without affecting the rest of the page |
| `AI-04` | Ask for auction analysis | Logged-in buyer on active item | Open assistant, send a message asking for analysis | AI response renders in chat and stays within the UI bubble layout |
| `AI-05` | Ask for strategy recommendation with budget | Logged-in buyer on active item | Enter a budget, ask AI how to bid | AI returns a recommendation and may include suggested strategy/max bid |
| `AI-06` | Start auto-bid suggestion flow | Logged-in buyer on active item with budget entered | Ask AI to start auto-bidding | UI renders an `Activate ...` action button when AI returns `START_AUTOBID` |
| `AI-07` | Activate AI auto-bidding | Logged-in buyer on active item | Click the AI activation button | Auto-bid session status bar appears with strategy, bid count, and stop control |
| `AI-08` | Poll live auto-bid status | Active auto-bid session exists | Leave assistant open during auction | Status updates without page crash; action log state continues to refresh |
| `AI-09` | Stop AI auto-bidding | Active auto-bid session exists | Click `Stop` in the assistant | Session stops; assistant shows confirmation message |
| `AI-10` | Reject seller activating AI on own item | Logged-in seller on own item detail page | Attempt to activate auto-bidder via AI assistant | UI shows readable error returned from backend; no active session bar remains |
| `AI-11` | Reject auto-bid when budget is below current price | Logged-in buyer on active item | Attempt AI activation with a too-low budget | UI shows an error and does not create a session |
| `AI-12` | Reject duplicate active auto-bid sessions | Logged-in buyer with one active auto-bid session | Try to activate a second session for the same item | UI shows duplicate-session error rather than creating another active session |
| `AI-13` | Disable AI chat after auction ends | Logged-in buyer on ended item | Open assistant after end state | Input is disabled or placeholder indicates auction has ended |
| `AI-14` | AI error handling / fallback rendering | Force AI request failure or fallback path | Send an AI message during degraded conditions | Assistant shows a safe error/fallback response in chat and the page remains usable |

## 16. Mobile and Responsive Checks

These checks should be run at minimum on a narrow mobile viewport and a desktop viewport.

| ID | Objective | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- |
| `RESP-01` | Auth menu remains readable on mobile | Logged-out user | Open `/login` and `/signup` on mobile width | Auth menu container remains visible, readable, and non-overlapping |
| `RESP-02` | Mobile navbar drawer works | Logged-in and logged-out sessions | Open mobile menu on catalogue | Drawer opens/closes and routes remain tappable |
| `RESP-03` | Signup form stacks correctly on mobile | Logged-out user | Open `/signup` on mobile width | Name fields stack cleanly and no content clips off-screen |
| `RESP-04` | Sell form remains usable on mobile | Logged-in seller | Open `/dashboard/sell` on mobile width | All required inputs remain visible and actionable without layout breakage |
| `RESP-05` | Payment form remains usable on mobile | Logged-in winner | Open payment page on mobile width | Form fields, toggle, totals, and CTA remain readable and tappable |

## 17. Exit Criteria

The frontend and AI chatbot testing pass is considered complete when:

- all high-priority tests in UC1 to UC7 pass
- all high-priority AI tests `AI-01` to `AI-13` pass
- no blocking navigation, authentication, bidding, payment, or receipt regressions remain
- mobile checks complete for auth, catalogue, sell, and payment pages
- known issues are documented with reproducible steps

## 18. Known Diagram Impact

If the Deliverable 2 diagrams do not already show the following, they should be updated for Assignment 3:

- add a distinct buyer-facing AI use case such as `UC8 - PrimeBid AI Advisor / Auto-Bid Assistant`
- add a sequence diagram for `/auto-bid/chat`, `/auto-bid/start`, `/auto-bid/status/{sessionId}`, and `/auto-bid/stop/{sessionId}`
- update the component diagram to include `AiBidAssistant`, `AutoBidController`, `GenAiAdvisor`, `AutoBidStore`, and the Gemini integration
- update the deployment/component view to show the Next.js frontend, gateway, IAM, catalogue, auction, payment, and Docker Compose containerized runtime
- update any activity or sequence diagrams that still reflect the old payment or auth page flow if they do not match the current frontend
