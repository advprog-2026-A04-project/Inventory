# GitHub Organization Deliverables

This file maps the course requirements to concrete setup/actions for this repository.

## Repository
- Organization: `advprog-2026-A04-project`
- Repository: `Inventory` (should be set to `Public` in GitHub settings)

## Branching Model
- `main` = production branch
- `staging` = integration branch
- Feature branch example: `feat/inventory-katalog` (branched from `staging`)

## Required Branch Protection (GitHub UI)
Configure protection for both `main` and `staging`:
1. `Settings` -> `Branches` -> `Add branch protection rule`
2. Branch name pattern: `main` (repeat for `staging`)
3. Enable:
   - `Require a pull request before merging`
   - `Require approvals` = `1`
   - `Require status checks to pass before merging`
4. Select status checks:
   - `CI / build`
   - `CodeQL / Analyze (CodeQL) (java-kotlin)`

## CI/CD Coverage in This Repo
- CI workflow: `.github/workflows/ci.yml`
  - Unit/integration tests
  - JaCoCo report + threshold verification
  - PMD static analysis
  - Checkstyle linter
- Security workflow: `.github/workflows/codeql.yml`
- CD workflow: `.github/workflows/cd.yml`
  - Deploy to `staging` environment when CI succeeds on `staging`
  - Deploy to `production` environment when CI succeeds on `main`
  - CD is blocked unless CI succeeds (`workflow_run` + `conclusion == success`)

## Integration Feature (Frontend + Backend + DB)
- Feature: Item create/list/reserve flow
- Frontend: Thymeleaf pages in `src/main/resources/templates`
- Backend: Spring MVC + Service in `src/main/java`
- DB: MySQL runtime and H2 test profile
- Integration test proof: `src/test/java/id/ac/ui/cs/advprog/inventory/integration/ItemFlowIntegrationTest.java`

## Pull Request Flow
1. Create feature branch from `staging`.
2. Implement feature.
3. Open PR to `staging`.
4. Wait for CI + CodeQL checks and at least 1 reviewer approval.
5. Merge to `staging`.
6. Open PR from `staging` to `main` for release.
