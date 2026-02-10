# Task ID: 24

**Title:** Integrate File Blob API and Navigation for File Details

**Status:** pending

**Dependencies:** 18, 22, 23

**Priority:** high

**Description:** Connect the `FileContentViewer` component to the backend API (Task 18) and implement navigation logic for clicking files in the tree view to display their content.

**Details:**

Extend the `RepositoryView` or a relevant child component:
1.  Modify the `FileTree` component (Task 20) to handle clicks on *files*. When a file is clicked, update the current route's `path` parameter to point to the file's path.
2.  When the URL's `path` parameter points to a file, trigger an API call to `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**filePath` (Task 18).
3.  Pass the fetched raw file content and its type to the `FileContentViewer` component (Task 23) for display.
4.  Handle loading states and errors for file content fetching.

**Test Strategy:**

Integration tests to verify that clicking a file in the tree triggers a new API call for its content and the `FileContentViewer` component displays it correctly. End-to-end tests to simulate user interaction, clicking on various files (e.g., Markdown, Java) and verifying the content appears as expected in the WYSIWYG viewer. Test navigation to non-existent files.
