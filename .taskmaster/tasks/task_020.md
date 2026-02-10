# Task ID: 20

**Title:** Develop File Tree UI Component

**Status:** pending

**Dependencies:** 19

**Priority:** medium

**Description:** Create a reusable frontend UI component capable of rendering a hierarchical list of files and directories within a repository.

**Details:**

Design and implement a component (e.g., `FileTree`) that takes an array of file/directory objects (as returned by Task 17) as props. The component should:
1.  Display file/directory names clearly.
2.  Visually differentiate between files and directories (e.g., using icons).
3.  Make each item clickable.
4.  Handle loading states (e.g., showing a spinner).
5.  Include a dropdown or similar UI element for branch selection.

Consider using a UI library's tree view component or building a custom list that mimics a tree structure.

**Test Strategy:**

Unit tests for the `FileTree` component to verify correct rendering of different data sets (empty, with files, with directories). Test interaction (e.g., `onClick` events) are correctly triggered when items are clicked. Snapshot tests can be used to ensure consistent UI rendering. Accessibility checks.
