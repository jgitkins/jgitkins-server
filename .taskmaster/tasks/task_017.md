# Task ID: 17

**Title:** Implement Backend API for File Tree Content

**Status:** pending

**Dependencies:** 16

**Priority:** high

**Description:** Develop a REST API endpoint to retrieve the contents of a specified directory (tree) within a Git repository for a given branch and path. This will support the file tree view.

**Details:**

Create a Spring Boot REST controller, e.g., `RepositoryController`, with an endpoint like `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**path`. The `**path` should capture the full path of the directory within the repository. The endpoint should:
1.  Receive `ownerType`, `owner`, `repoName`, `branch`, and `path` from the URL.
2.  Utilize `GitRepositoryService` (Task 16) to access the repository and resolve the specified tree.
3.  Iterate through the `TreeWalk` to list files and subdirectories within the current path.
4.  Return a JSON array of objects, each containing `name`, `type` ('file'/'directory'), and potentially `size` (for files) and `sha`.

Example API response:
```json
[
  {"name": "src", "type": "directory", "sha": "..."},
  {"name": "README.md", "type": "file", "size": 1234, "sha": "..."}
]
```

**Test Strategy:**

Unit tests for the controller to verify request parameter parsing and service method calls. Integration tests using MockMvc to call the endpoint with various valid and invalid parameters (e.g., existing path, non-existent path, different branches) and assert the correct JSON response structure and content. Test edge cases like root directory (`path=''`) and deeply nested directories.
