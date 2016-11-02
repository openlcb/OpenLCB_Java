# How to release OpenLCB to JMRI

## Prerequisites

* You need to have a git checkout of the OpenLCB_Java repository. This may be a
  fork or you need to have write access to the repository.
* You need to be able to compile java using the ant build toolchain.
* You need to have a git checkout of JMRI, presumably from a fork.

## Release OpenLCB

1. Update the master branch

```git checkout master```
```git pull```

2. Create a new branch for your changes

```git checkout -b bracz-newrelease```

3. Update the minor version number

Edit the following files and bump the "library" / "package" minor version number:
  * manifest and edit the `Package-Version` to bump last number
  * src/org/openlcb/Version.java and bump `libMod`.
  
4. run tests to check that you updated everything

```ant run```

and observe that no errors are reported (at least in the VerionTest).

5. recompile JARs

```ant jars```

6. Commit everything and push to github

commit the four files: manifest, Version.java and the two jars. Push your
branch to github and create a pull request. Mark in the commit that you are
increasing the version number.

Example commit:
https://github.com/openlcb/OpenLCB_Java/commit/a15a782b30e982d91a039dac797f2f7daaaaca53
Another example:
https://github.com/openlcb/OpenLCB_Java/commit/80793002a217d6f3cc2f188a532525d875652e0f

## Update JMRI for new OpenLCB


1. Update the master branch of your fork by pulling from upstream

```git checkout master```
```git pull upstream```

you may also want to update your github fork of the repository

```git push origin```

2. Create a new branch for your changes

```git checkout -b openlcb-release-update master```

3. Copy the new JAR file into libs

This is stored in two places: once just the jar, and once in a versioned
subdirectory.

Copy the new JAR file into the `lib/` subdirectory:
```cp somewhere/openlcb.jar lib/```
Remove the old versioned subdirectory:
```rm -rf lib/org/openlcb/openlcb/0.7.6```
Create a new cersioned subdirectory:
```mkdir lib/org/openlcb/openlcb/0.7.7```
Put the new JAR into the new versioned subdirectory:
```cp lib/openlcb.jar lib/org/openlcb/openlcb/0.7.7/openlcb-0.7.7.jar```

4. Edit lib/README.md and update the version number for the openlcb.jar
   library.

4. Commit your code and push to github (to your own fork of the project)

Example commits: https://github.com/balazsracz/JMRI/commit/f70edb1f21f8f2844dce2871274fea159faafaf6

5. Create a pull request for JMRI

Go to github, open your own fork of the JMRI project, select the branch you
created (github will give you a quick link) and click "create pull request".

6. Wait for a JMRI project member to approve your pull request.
