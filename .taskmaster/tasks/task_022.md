# Task ID: 22

**Title:** Implement Frontend Tree Navigation Logic

**Status:** pending

**Dependencies:** 19, 21

**Priority:** high

**Description:** Add interactivity to the `FileTree` UI component to handle user clicks on directories and branch selection, updating the URL and fetching new data.

**Details:**

Modify the `FileTree` component (Task 20) to:
1.  On clicking a directory, update the current route's `path` parameter to reflect the new directory's path. Use the frontend router's navigation API (e.g., `navigate('/repositories/.../new/path')`).
2.  Implement logic for the branch selection dropdown. When a new branch is selected, update the route's `branch` parameter, triggering a re-fetch of the file tree for the new branch.

Ensure that URL changes automatically trigger the data fetching in Task 21.

**Test Strategy:**

Unit tests for the `FileTree` component's navigation handlers, verifying that `onClick` for directories and `onChange` for branch selection correctly invoke the router's navigation function with the expected new URL. Manual testing to ensure seamless navigation between directories and branches, with the URL correctly reflecting the current view.
