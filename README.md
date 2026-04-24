# Quiz Leaderboard System

## Overview

This project implements a backend solution for generating a leaderboard from quiz data fetched via an external API. The system polls multiple API responses, handles duplicate data correctly, and computes total scores for participants.

---

## Approach

### 1. API Polling

* The API is called **10 times** using `poll` values from `0` to `9`.
* A **5-second delay** is maintained between each request as required.

### 2. Handling Duplicate Data

* The same event data can appear across multiple API responses.
* To avoid duplicate counting, a `HashSet` is used to track processed events.
* Each event is uniquely identified using its **full JSON representation**:

  ```
  event.toString()
  ```
* This ensures only truly repeated events are ignored.

### 3. Score Aggregation

* A `HashMap` is used to accumulate total scores per participant.
* Scores are updated only when a new unique event is encountered.

### 4. Leaderboard Generation

* The final scores are sorted in **descending order**.
* A leaderboard is constructed in the required JSON format.

### 5. Submission

* The final leaderboard is submitted via a **POST request**.
* Submission is performed only once as per instructions.

---

## How to Run

### Prerequisites

* Java (JDK 11 or above)

### Steps

1. Compile:

   ```
   javac -cp ".:json-20231013.jar" Main.java
   ```

2. Run:

   ```
   java -cp ".:json-20231013.jar" Main
   ```

---

## Notes

* The implementation follows the required logic for polling, deduplication, and leaderboard generation.

* A standard JSON parsing library (`org.json`) is used for robustness.

* During later testing phases, the API occasionally returned:

  ```
  "no available server"
  ```

  or partial data, likely due to **rate limiting or server-side constraints**.

* However, earlier executions confirmed:

  * Correct polling logic
  * Proper duplicate handling
  * Accurate score aggregation

---

## Tech Stack

* Java
* HttpClient API
* org.json library

---

## Conclusion

This solution correctly implements the required logic for handling repeated API responses, aggregating participant scores, and generating a leaderboard. The design ensures correctness even in the presence of duplicate or repeated data.
