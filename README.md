# Quiz Leaderboard System — Bajaj Finserv Health Internship Assignment

**Registration Number:** RA2311003020408

## Problem Statement

Build an application that polls a quiz validator API 10 times, deduplicates overlapping event data, aggregates scores per participant, and submits a sorted leaderboard.

## Solution Approach

### Architecture
- **Language:** Java 17+ (uses `java.net.http.HttpClient`)
- **JSON Library:** `org.json` (json-20231013.jar)
- **Single-file design** — `Main.java` contains all logic

### How It Works

1. **Polling**: The program calls `GET /quiz/messages?regNo=RA2311003020408&poll={0..9}` — 10 times total, with a **mandatory 5-second delay** between each request.

2. **Deduplication**: Each event is uniquely identified by the composite key `roundId + "|" + participant`. A `HashSet<String>` tracks all seen keys. If a key has already been seen, the event is skipped — preventing double-counting of scores from duplicate API responses.

3. **Aggregation**: Scores are accumulated per participant in a `HashMap<String, Integer>` using `Map.merge()`.

4. **Leaderboard Generation**: Entries are sorted in **descending order** by `totalScore`.

5. **Submission**: The sorted leaderboard is submitted via `POST /quiz/submit` as a JSON payload.

### Why `roundId + participant` for Deduplication?

In distributed systems, the same event can be delivered multiple times across different API polls. The assignment specifies that `(roundId, participant)` uniquely identifies a scoring event — meaning each participant scores **exactly once** per round. Using the full JSON object string would be fragile (field ordering is not guaranteed in JSON), so the composite key approach is both correct and robust.

## Results

| Participant | Total Score |
|-------------|-------------|
| Bob         | 295         |
| Alice       | 280         |
| Charlie     | 260         |
| **Total**   | **835**     |

- **Unique events**: 9
- **Duplicates filtered**: Multiple events correctly ignored across polls

## How to Run

### Prerequisites
- Java 17 or higher
- `json-20231013.jar` (included in repo)

### Compile & Run

```bash
javac -cp json-20231013.jar Main.java
java -cp .:json-20231013.jar Main
```

> **Note:** On Windows, use `;` instead of `:` as the classpath separator:
> ```bash
> java -cp .;json-20231013.jar Main
> ```

### Expected Output

```
Poll 0 completed — 2 new, 0 duplicates
Poll 1 completed — 1 new, 0 duplicates
Poll 2 completed — 1 new, 1 duplicates
Poll 3 completed — 0 new, 1 duplicates
Poll 4 completed — 1 new, 1 duplicates
Poll 5 completed — 1 new, 0 duplicates
Poll 6 completed — 2 new, 0 duplicates
Poll 7 completed — 0 new, 1 duplicates
Poll 8 completed — 1 new, 1 duplicates
Poll 9 completed — 0 new, 1 duplicates

--- Final Scores ---
Bob : 295
Alice : 280
Charlie : 260
Total Score = 835
Unique events = 9

--- Server Response ---
{"regNo":"RA2311003020408","totalPollsMade":72,"submittedTotal":835,"attemptCount":7}
```

## File Structure

```
├── Main.java           # Main application source code
├── json-20231013.jar   # JSON parsing library
└── README.md           # This file
```

## Key Design Decisions

1. **Composite dedup key** (`roundId|participant`) — avoids JSON serialization inconsistencies
2. **Graceful error handling** — non-JSON responses are skipped without crashing
3. **5-second mandatory delay** — ensures compliance with API rate limiting
4. **Single submission** — leaderboard is submitted only once after all data is collected
