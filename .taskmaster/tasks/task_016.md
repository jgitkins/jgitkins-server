# Task ID: 16

**Title:** Setup Git Repository Access Service

**Status:** pending

**Dependencies:** None

**Priority:** high

**Description:** Create a foundational backend service layer for interacting with Git repositories. This service will abstract low-level Git operations, such as repository cloning, opening, and navigating through its contents.

**Details:**

Implement a Java service, e.g., `GitRepositoryService`, utilizing the JGit library. This service should provide methods for: 1. Initializing a Git repository object given a local path or URL. 2. Fetching references (branches, tags). 3. Resolving a tree object for a specific branch and path. 4. Resolving a blob object for a specific branch and path. Ensure proper resource management for Git repository objects (e.g., `Repository.close()`).

Example pseudo-code for service method:
```java
public class GitRepositoryService {
    public Repository getRepository(String owner, String repoName) { /* ... */ }
    public RevWalk getRevWalk(Repository repo) { /* ... */ }
    public ObjectId resolveTree(Repository repo, String branch, String path) { /* ... */ }
    public TreeWalk getTreeWalk(Repository repo, RevTree tree, String path) { /* ... */ }
    public byte[] getBlobContent(Repository repo, ObjectId blobId) { /* ... */ }
}
```

**Test Strategy:**

Unit tests for `GitRepositoryService` methods to ensure correct interaction with JGit. Test repository initialization, branch resolution, tree object lookup, and blob content retrieval. Use mocked Git objects or an in-memory Git repository for isolated testing. Integration tests with a real, small Git repository to confirm end-to-end functionality.
