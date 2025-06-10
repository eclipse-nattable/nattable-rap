# Contributing to Nebula NatTable RAP

Thanks for your interest in this project. The following information will help you setting up your development environment in order to be able to contribute bugfixes or enhancements to NatTable.

## Project description:

NatTable is a powerful and flexible SWT table/grid widget that is built to handle very large data sets, real-time updates, dynamic styling, and more.
NatTable is a subproject of the Nebula Project, the home of further supplemental custom widgets for SWT.

The project details can be found [here](https://projects.eclipse.org/projects/technology.nebula.nattable).

This project contains a fragment for NatTable Core to make NatTable work with [RAP](https://eclipse.dev/rap/).

This project uses [GitHub Issues](https://github.com/eclipse-nattable/nattable-rap/issues) to track ongoing development and issues.
Be sure to search for existing bugs before you create another one. Remember that contributions are always welcome!

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team, contributors must
electronically sign the [Eclipse Contributor Agreement (ECA)](https://www.eclipse.org/legal/ECA.php).

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please have a look at the [Eclipse Committer Handbook](https://www.eclipse.org/projects/handbook/#resources-commit)

## Setup and Workflow

The NatTable project basically follows the same process as the Eclipse Platform. As a project hosted on GitHub, we accept pull requests. Please follow the guidelines in the [Eclipse Platform Contribution Guide](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md) if you want to create a pull request for the NatTable project:
* [Setting up Your Eclipse and GitHub Account](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#setting-up-your-eclipse-and-github-account)
* [Recommended Workflow](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#recommended-workflow)
* [Commit Message Recommendations](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#commit-message-recommendations)

## Contact

Contact the project developers via the [project's "dev" mailing list](https://dev.eclipse.org/mailman/listinfo/nattable-dev).

## Environment

The development tools with minimum versions that are used by the NatTable team are:

* JDK 21
* Eclipse 4.32 (2024-06)
* Maven 3.9.8 with Tycho 4.0.13
* Git
* JUnit5

### Source code organization

The NatTable source is divided into the following main projects:

* org.eclipse.nebula.widgets.nattable.rap - NatTable Core RAP Fragment
* org.eclipse.nebula.widgets.nattable.rap.examples - NatTable RAP example application containing several examples
* org.eclipse.nebula.widgets.nattable.rap.updatesite - NatTable RAP update site

## Development IDE Configuration

### Java Requirements

NatTable RAP has a Java 21 and RAP 4.0 as minimum requirements, so dependencies to newer Java and platform versions must be avoided.

### Dependencies

After importing the NatTable projects in Eclipse, they will not compile due to missing dependencies. NatTable provides a target platform definition that should be activated in order to resolve the missing dependencies.

* Open the *target-platform-rap* project
* Open the *target-platform-rap.target* file (this may take a while as it downloads the indexes of the p2 repositories the target platform refers to)
* In the resulting editor, click on the *Set as Target Platform* link at the top right (this may also take a while)

After that, the workspace should build cleanly. If not, try *Project > Clean... > All*. If this also doesn't help open *Preferences > Plug-In Development > Target Platform*, select the checked target platform and click *Reload...* this will flush PDE's bundle cache and re-download the artifacts listed in the target platform.

## Build

The NatTable build is based on pomless Tycho. To build from the command line, you need to execute the following command from the *NATTABLE_TRUNK/nattable-rap* directory:

```
mvn clean verify
```

After the build successfully finished, you will find an Update Site archive in

*NATTABLE_TRUNK/nattable-rap/org.eclipse.nebula.widgets.nattable.rap.updatesite/target*

that you can use for example in a local target definition.
