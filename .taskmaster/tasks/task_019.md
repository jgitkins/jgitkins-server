# Task ID: 19

**Title:** Frontend Router Setup for Repository Views

**Status:** pending

**Dependencies:** None

**Priority:** high

**Description:** Configure the frontend routing to handle repository-specific URLs, including dynamic parameters for owner type, owner, repository name, branch, and file/directory path.

**Details:**

Set up a main frontend component (e.g., `RepositoryView`) and define a route structure that captures all necessary parameters: `/repositories/:ownerType/:owner/:repoName/:branch/*path`. The `*path` segment should be optional and capture the remainder of the URL for file/directory paths.

Example routing configuration (e.g., React Router, Vue Router, Angular Router):
```javascript
// Assuming a React-like router setup
<Routes>
  <Route path="/repositories/:ownerType/:owner/:repoName/:branch/*path" element={<RepositoryView />} />
</Routes>
```
The `RepositoryView` component should be able to extract these parameters (e.g., `useParams()` in React Router) to pass to child components or use for API calls. Default the `branch` to 'main' or 'master' if not explicitly provided in the URL initially.

**Test Strategy:**

Unit tests for the routing configuration to ensure correct parameter extraction. Manual testing by navigating to various valid and invalid repository URLs and verifying that the correct component renders and extracts the parameters correctly.
