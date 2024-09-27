# Contributing to Resource Scanner

We would love for you to contribute to Resource Scanner and help
make it even better than it is today!
As a contributor, here are the guidelines we would like you to follow:

- [Code of Conduct](#code-of-conduct)
- [Issues and Bugs](#found-a-bug)
- [Feature Requests](#missing-a-feature)
- [Submission Guidelines](#submission-guidelines)
- [Coding Rules](#coding-rules)
- [Commit Message Guidelines](#commit-message-format)

## Code of Conduct

Help us keep Resource Scanner open and inclusive.
Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Found a Bug?

If you find a bug in the source code, you can help us by [submitting an issue](#submitting-an-issue) to our [GitHub Repository][github].
Even better, you can [submit a Pull Request](#submitting-a-pull-request-pr) with a fix.

## Missing a Feature?

You can *request* a new feature by [submitting an issue](#submitting-an-issue) to our GitHub Repository.
If you would like to *implement* a new feature, please consider the size of the change in order to determine the right steps to proceed:

- For a **Major Feature**, first open an issue and outline your proposal so that it can be discussed.
  This process allows us to better coordinate our efforts, prevent duplication of work, and help you to craft the change so that it is successfully accepted into the project.

  **Note**: Adding a new topic to the documentation, or significantly re-writing a topic, counts as a major feature.

- **Small Features** can be crafted and directly [submitted as a Pull Request](#submitting-a-pull-request-pr).

## Submission Guidelines

### Submitting an Issue

Before you submit an issue, please search the issue tracker. An issue for your problem might already exist and the discussion might inform you of workarounds readily available.

We want to fix all the issues as soon as possible, but before fixing a bug, we need to reproduce and confirm it.
In order to reproduce bugs, we require that you provide a minimal reproduction.
Having a minimal reproducible scenario gives us a wealth of important information without going back and forth to you with additional questions.

A minimal reproduction allows us to quickly confirm a bug (or point out a coding problem) as well as confirm that we are fixing the right problem.

We require a minimal reproduction to save maintainers' time and ultimately be able to fix more bugs.
Often, developers find coding problems themselves while preparing a minimal reproduction.
We understand that sometimes it might be hard to extract essential bits of code from a larger codebase, but we really need to isolate the problem before we can fix it.

Unfortunately, we are not able to investigate / fix bugs without a minimal reproduction, so if we don't hear back from you, we are going to close an issue that doesn't have enough info to be reproduced.

You can file new issues by selecting from our [new issue templates](https://github.com/mzdun/resource-scanner-mc/issues/new/choose) and filling out the issue template.

### Submitting a Pull Request (PR)

Before you submit your Pull Request (PR) consider the following guidelines:

1. Search [GitHub](https://github.com/mzdun/resource-scanner-mc/pulls) for an open or closed PR that relates to your submission.
   You don't want to duplicate existing efforts.

2. Be sure that an issue describes the problem you're fixing, or documents the design for the feature you'd like to add.
   Discussing the design upfront helps to ensure that we're ready to accept your work.

3. [Fork](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo) the `mzdun/resource-scanner-mc` repo.

4. In your forked repository, make your changes in a new git branch:

     ```shell
     git checkout -b my-fix-branch main
     ```

5. Create your patch, **including appropriate test cases**.

6. Follow our [Coding Rules](#coding-rules).

7. Run the full Resource Scanner test suite, as described in the [developer documentation][dev-doc] *(TODO: FIX LINK)*, and ensure that all tests pass.

8. Commit your changes using a descriptive commit message that follows our [commit message conventions](#commit-message-format).
   Adherence to these conventions is necessary because release notes are automatically generated from these messages.

     ```shell
     git commit --all
     ```

    Note: the optional commit `--all` command line option will automatically "add" and "rm" edited files.

9. Push your branch to GitHub:

   ```shell
   git push origin my-fix-branch
   ```

10. In GitHub, send a pull request to `resource-scanner-mc:main`.

### Reviewing a Pull Request

The Resource Scanner team reserves the right not to accept pull requests from community members who haven't been good citizens of the community. Such behavior includes not following the [Resource Scanner code of conduct](CODE_OF_CONDUCT.md) and applies within or outside of Resource Scanner managed channels.

#### Addressing review feedback

If we ask for changes via code reviews then:

1. Make the required updates to the code.

2. Re-run the Resource Scanner test suites to ensure tests are still passing.

3. Create a fixup commit and push to your GitHub repository (this will update your Pull Request):

    ```shell
    git commit --all --fixup HEAD
    git push
    ```

    For more info on working with fixup commits see [here](./contributing-docs/using-fixup-commits.md) *(TODO: FIX LINK)*.

That's it! Thank you for your contribution!

##### Updating the commit message

A reviewer might often suggest changes to a commit message (for example, to add more context for a change or adhere to our [commit message guidelines](#commit-message-format)).
In order to update the commit message of the last commit on your branch:

1. Check out your branch:

    ```shell
    git checkout my-fix-branch
    ```

2. Amend the last commit and modify the commit message:

    ```shell
    git commit --amend
    ```

3. Push to your GitHub repository:

    ```shell
    git push --force-with-lease
    ```

> NOTE: \
> If you need to update the commit message of an earlier commit, you can use `git rebase` in interactive mode.
> See the [git docs](https://git-scm.com/docs/git-rebase#_interactive_mode) for more details.

#### After your pull request is merged

After your pull request is merged, you can safely delete your branch and pull the changes from the main (upstream) repository:

- Delete the remote branch on GitHub either through the GitHub web UI or your local shell as follows:

    ```shell
    git push origin --delete my-fix-branch
    ```

- Check out the main branch:

    ```shell
    git checkout main -f
    ```

- Delete the local branch:

    ```shell
    git branch -D my-fix-branch
    ```

- Update your local `main` with the latest upstream version:

    ```shell
    git pull --ff upstream main
    ```

## Coding Rules

To ensure consistency throughout the source code, keep these rules in mind as you are working:

- All features or bug fixes **must be tested** by one or more specs (unit-tests).
- All public API methods **must be documented**.
- *TODO*: Select a formatter

## Commit Message Format

*This specification is inspired by and supersedes the [AngularJS commit message format][commit-message-format].*

We have very precise rules over how our Git commit messages must be formatted.
This format leads to **easier to read commit history**.

Each commit message consists of a **header**, a **body**, and a **footer**.

```plain
<header>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The `header` is mandatory and must conform to the [Commit Message Header](#commit-message-header) format.

The `body` is optional.
When the body is present it must be at least 20 characters long and must conform to the [Commit Message Body](#commit-message-body) format.

The `footer` is optional. The [Commit Message Footer](#commit-message-footer) format describes what the footer is used for and the structure it must have.

### Commit Message Header

```plain
<type>(<scope>): <short summary>
  │       │             │
  │       │             └─> Summary in present tense. Not capitalized. No period at the end.
  │       │
  │       └─> Commit Scope (tbd)
  │
  └─> Commit Type: feat|fix|assets|build|chore|ci|docs|perf|refactor|revert|style|test
```

The `<type>` and `<summary>` fields are mandatory, the `(<scope>)` field is optional.

#### Type

Must be one of the following:

- **feat**: new feature for the user, not a new feature for build script
- **fix**: bug fix for the user, not a fix to a build script
- **assets**: only artwork: icons, images, but not css (those belong to `style`)
- **build**: changes that affect the build system or external dependencies
- **chore**: updating grunt tasks etc; no production code change
- **ci**: changes to CI configuration files and scripts (GitHub actions)
- **docs**: changes to the documentation
- **perf**: performance improvement for the user
- **refactor**: changes of the production code that neither fixes a bug nor adds a feature, eg. renaming a variable
- **revert**: neither fixing a bug nor adding a feature, a result of `git revert`
- **style**: formatting, missing semi colons, etc; no production code change
- **test**: adding missing tests or correcting existing tests; no production code change

#### Scope

The scope is optional and no set of scopes is provided at this moment.

#### Summary

Use the summary field to provide a succinct description of the change:

- use the imperative, present tense: "change" not "changed" nor "changes"
- don't capitalize the first letter
- no dot (.) at the end

#### Commit Message Body

Just as in the summary, use the imperative, present tense: "fix" not "fixed" nor "fixes".

Explain the motivation for the change in the commit message body. This commit message should explain *why* you are making the change.
You can include a comparison of the previous behavior with the new behavior in order to illustrate the impact of the change.

#### Commit Message Footer

The footer can contain information about breaking changes and deprecations and is also the place to reference GitHub issues, Jira tickets, and other PRs that this commit closes or is related to.
For example:

```plain
BREAKING CHANGE: <breaking change summary>
<BLANK LINE>
<breaking change description + migration instructions>
<BLANK LINE>
<BLANK LINE>
Fixes #<issue number>
```

or

```plain
DEPRECATED: <what is deprecated>
<BLANK LINE>
<deprecation description + recommended update path>
<BLANK LINE>
<BLANK LINE>
Closes #<pr number>
```

Breaking Change section should start with the phrase `BREAKING CHANGE:` followed by a summary of the breaking change, a blank line, and a detailed description of the breaking change that also includes migration instructions.

Similarly, a Deprecation section should start with `DEPRECATED:` followed by a short description of what is deprecated, a blank line, and a detailed description of the deprecation that also mentions the recommended update path.

### Revert commits

If the commit reverts a previous commit, it should begin with `revert:`, followed by the header of the reverted commit.

The content of the commit message body should contain:

- information about the SHA of the commit being reverted in the following format: `This reverts commit <SHA>`,
- a clear description of the reason for reverting the commit message.

[commit-message-format]: https://docs.google.com/document/d/1QrDFcIiPjSLDn3EL15IJygNPiHORgU1_OOAqWjiDU5Y/edit#
[dev-doc]: ./contributing-docs/building-and-testing.md
[github]: https://github.com/mzdun/resource-scanner-mc
