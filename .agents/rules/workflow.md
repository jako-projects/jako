# Git Workflow & Pre-Commit Parity Guard Rule

## 1. Repository Topology

| Remote Name | Repository URL | Purpose | Push Policy |
| :--- | :--- | :--- | :--- |
| **`origin`** | `https://github.com/jako-projects/jako.git` | **Official Open-Source Repo** | All development happens on `main` tracking `origin/main`. Auto-push (`git push origin main`) immediately after user reviews & approves a commit. |

---

## 2. Development & Commit Workflow

1. All development happens on `main`, which tracks `origin/main` (`jako-projects/jako`).
2. Once a feature or code change has been reviewed and approved by the user for commit, **automatically run `git push origin main`** immediately after `git commit` by default, without asking for separate confirmation to push (unless the user explicitly requests a local-only commit).
3. Ensure `origin` points to `https://github.com/jako-projects/jako.git` (`git remote -v`) and `.git/hooks/pre-commit` is installed and executable via `./scripts/install-git-hooks.sh`.

---

## 3. Local `pre-commit` Hook Guard

1. **`pre-commit` (API Compatibility & JDK Target Parity Check)**:
   - Installed via `./scripts/install-git-hooks.sh` into `.git/hooks/pre-commit`.
   - Automatically runs `./gradlew :jako-stdlib:jvmTest --tests "jako.test.parity.JdkSignatureParityTest" --quiet` before every commit to verify:
     - `commonMain` vs `nativeMain` supertype & actual parity.
     - `commonMain` supertypes, methods, arities, and properties against target JDK (`--release <N>`, default `8`).
     - 100% coverage & bidirectional sync between target JDK classes, [`API_CHECKLIST.md`](../../API_CHECKLIST.md), and [`UNSUPPORTED_APIS.md`](../../UNSUPPORTED_APIS.md).
     - Backward compatibility against committed API baselines (`api/jako-stdlib.api` and `api/jako-android.api`). When adding new public APIs, run `./gradlew dumpApiBaseline` to update the baseline snapshot.
