# Task ID: 21

**Title:** Integrate File Tree API with Frontend Component

**Status:** pending

**Dependencies:** 17, 20

**Priority:** high

**Description:** Connect the `FileTree` UI component to the backend API developed in Task 17 to fetch and display the contents of a specified repository path.

**Details:**

Within the `RepositoryView` (Task 19) or a child component, implement data fetching logic.
1.  When the component mounts or URL parameters (owner, repoName, branch, path) change, make an API call to `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**path`.
2.  Store the fetched data in the component's state.
3.  Pass the fetched file/directory list to the `FileTree` component (Task 20) for rendering.
4.  Display loading indicators while data is being fetched and error messages if the API call fails.

Use a state management solution (e.g., React Context, Redux, Vuex, NGRX) for managing repository data if applicable.

**Test Strategy:**

Integration tests to verify the frontend component successfully fetches data from the mocked backend API and renders it correctly in the `FileTree` component. Test various scenarios: successful fetch, API error, empty directory. End-to-end tests using a tool like Cypress or Playwright to simulate user navigation and verify content display.
