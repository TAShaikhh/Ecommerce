# Omer's simple Explanation - A Guide to Your Project

This document is your cheat sheet to explain exactly how the project was built, designed specifically so that you sound like a pro when the TA asks you questions!

## The AI Feature (Gemini API)
Yes, we are using the **Google Gemini 2.0 API** for the chat feature! 
Specifically, the Gateway service (which acts as the main door for all backend traffic) takes whatever the user types into the chat, combines it with the *current live details of the auction* (like the current highest bid, time left, and user's budget), and sends it to the Gemini API using simple HTTP requests via Spring Boot's `RestTemplate`.

### How the AI Autobidder works (Simple Terms):
1. **The User Texts the AI:** A user says "Hey, bid aggressively for me up to $500."
2. **The Gateway Adds Context:** Our backend code silently injects the current auction rules into a "System Prompt" hiding behind the scenes. This prompt tells Gemini it must *strictly* output JSON data. 
3. **The AI Decides:** Gemini responds, recognizing what the user wants to do, and spits back a JSON response that says `"action": "START_AUTOBID"` and `"suggestedStrategy": "AGGRESSIVE"`.
4. **The Bot Takes Over:** A scheduler inside our backend wakes up every 3 seconds, realizes an "Auto-Bid" session is active, and automatically places native API bids on the user's behalf up to their maximum budget.

## Frontend Explanation (React & Next.js)
The frontend UI is built using **Next.js** (a popular framework for React) and **TypeScript**.

### How the Frontend Architecture Connects:
- **Routing:** Every folder inside the `app/` directory represents a web page. For example, the `app/(auth)/login/page.tsx` file controls the `/login` screen.
- **Client Components:** At the top of most files, you'll see `'use client';`. This tells Next.js that this part of the UI runs directly in the user's browser, allowing us to use interactive React hooks like `useState` (to store variables like what they typed in a text box) and `useEffect` (to run code automatically, like ticking the auction timers).
- **Framer Motion:** What makes the UI look cinematic and smooth? We use an animation library called `framer-motion`. When you see `<motion.div>`, it's telling the browser to animate that box smoothly onto the screen.
- **API Fetching & Auth:** The UI talks to the Gateway API (port 8080) for everything. When the user logs in, the Gateway issues a piece of text called a **JWT Token**. The frontend saves this token in Chrome's `localStorage` and silently attaches it to all future API calls to prove the user is logged in. 
- **Zustand:** This is the tool we used to handle "Global State." It remembers who is logged in across all pages without having to pass data down manually.

## Docker Explanation (You MUST push this to Docker Hub)

### Do I upload this project to the Docker website?
**YES.** The marking rubric explicitly says: *"Ideally, the application is packaged in Docker containers, can be pulled from Docker repository, and run... To be considered cloud enabled, at minimum, the backend is packaged into a container that can be pulled and run using docker commands."*

This means you build your Docker files locally, and push them to Docker Hub so the TA can simply type `docker pull your-username/gateway` and it magically downloads to their machine. **This maximizes your points for "Cloud and Containers".**

### The easiest way to get full Docker marks:
We already built the `Dockerfile`s and the `docker-compose.yml`. You just need to build them and push them up to your public Docker Hub account. 

Here are the step-by-step commands to do that beautifully:

1. Go to [https://hub.docker.com/](https://hub.docker.com/) and create a free account. Note your **username**.
2. Open your `docker-compose.yml` file and edit the lines where it says `image: <your-docker-username>/...` and replace `<your-docker-username>` with your actual Docker Hub username.
3. Open a terminal and log into Docker:
   ```bash
   docker login
   ```
4. Build all the images using our compose file:
   ```bash
   docker-compose build
   ```
5. Push them all to the Docker Hub website in one single stroke!
   ```bash
   docker-compose push
   ```

When you demo this for the TA, you can tell them: *"TAs, you don't even need my local files to run the backend. Just run `docker-compose up -d` pulling from my Docker Hub repository, and the cloud will handle the rest."* This directly fulfills the rubric's "Ease of install/run" requirement.
