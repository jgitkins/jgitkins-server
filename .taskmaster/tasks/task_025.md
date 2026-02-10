# Task ID: 25

**Title:** Implement Robust Error Handling and Loading States

**Status:** pending

**Dependencies:** 21, 24

**Priority:** medium

**Description:** Add comprehensive error handling (e.g., repository not found, file/path not found, API errors) and loading indicators across both file tree and file detail views to improve user experience.

**Details:**

Enhance both frontend and backend:
**Frontend:**
1.  Display user-friendly error messages for failed API calls (e.g., 'Repository not found', 'File not found', 'Network error').
2.  Implement visual loading indicators (spinners, skeletons) during all data fetches (file tree, file content).
3.  Provide clear UI feedback when a branch is switched or directory is navigated.

**Backend:**
1.  Implement custom exception handling for specific Git errors (e.g., `RepositoryNotFoundException`, `BranchNotFoundException`, `PathNotFoundException`).
2.  Map these exceptions to appropriate HTTP status codes (e.g., 404 Not Found, 500 Internal Server Error) and provide informative error bodies.

Ensure that an unhandled error doesn't crash the application.

**Test Strategy:**

Integration tests for both frontend and backend to verify error paths. For backend, trigger errors (e.g., request non-existent repo/branch/path) and assert correct HTTP status codes and error messages. For frontend, use mocked API errors to confirm loading states and error messages are displayed correctly to the user. End-to-end tests for navigation to invalid URLs or non-existent resources.
