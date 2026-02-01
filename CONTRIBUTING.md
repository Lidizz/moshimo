# Contributing to Moshimo

Thank you for your interest in contributing to Moshimo! 🎉

## Important Notice

This project uses a **proprietary license**. By contributing, you agree that:

1. Your contributions become part of this project under the same license
2. You grant the project owner full rights to use your contributions
3. You will not be compensated for contributions
4. You have the right to submit the contribution

## How to Contribute

### 1. Fork the Repository

Click the "Fork" button on GitHub to create your own copy.

### 2. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

Use prefixes:
- `feature/` - New features
- `bugfix/` - Bug fixes
- `refactor/` - Code improvements
- `docs/` - Documentation updates

### 3. Make Your Changes

- Follow existing code style
- Add comments explaining "why" not "what"
- Update documentation if needed
- Test your changes locally

### 4. Commit Your Changes

Use clear, descriptive commit messages:

```bash
git commit -m "feat: add sector filter dropdown"
git commit -m "fix: correct CAGR calculation for partial years"
git commit -m "docs: update API endpoint documentation"
```

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request on GitHub.

## Pull Request Requirements

All PRs must:

- [ ] Pass CI checks (build successful)
- [ ] Have a clear description of changes
- [ ] Reference related issues (if any)
- [ ] Not break existing functionality

## Code Style

### Backend (Java)
- Follow standard Java conventions
- Use Lombok annotations where appropriate
- Add JavaDoc for public methods
- Include "Learning Notes" comments for educational value

### Frontend (TypeScript/React)
- Use TypeScript strict mode
- Prefer functional components with hooks
- Add TSDoc for exported functions
- Use meaningful component/variable names

## What We're Looking For

✅ **Welcome:**
- Bug fixes with clear reproduction steps
- Performance improvements
- Documentation improvements
- Accessibility improvements
- Test coverage additions

⚠️ **Discuss First:**
- New features (open an issue first)
- Major architectural changes
- Dependency additions

❌ **Not Accepted:**
- Breaking changes without discussion
- Code without context/explanation
- Copy-pasted code from other projects

## Questions?

Open an issue with the `question` label.

---

Thank you for helping make Moshimo better! 🚀
