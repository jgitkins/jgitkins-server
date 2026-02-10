# Task ID: 18

**Title:** Implement Backend API for File Blob Content

**Status:** pending

**Dependencies:** 16

**Priority:** high

**Description:** Develop a REST API endpoint to retrieve the raw content of a specified file (blob) within a Git repository for a given branch and file path. This will support the file detail view.

**Details:**

Extend the `RepositoryController` (or create a new one) with an endpoint like `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**filePath`. The `**filePath` should capture the full path of the file within the repository.
1.  Receive `ownerType`, `owner`, `repoName`, `branch`, and `filePath`.
2.  Utilize `GitRepositoryService` (Task 16) to access the repository and resolve the specified blob.
3.  Retrieve the raw content of the blob.
4.  Return the content directly with the appropriate `Content-Type` header (e.g., `text/plain`, `text/markdown`, `application/octet-stream`). Handle binary files appropriately.

Consider using `ResponseEntity<byte[]>` for binary content and `String` for text content, setting the `Content-Type` header dynamically based on file extension.

**Test Strategy:**

Unit tests for the controller to verify parameter handling and service interaction. Integration tests using MockMvc to call the endpoint with various file paths (e.g., text file, markdown file, binary file) and assert the correct content and `Content-Type` header in the response. Test for non-existent files and branches.
