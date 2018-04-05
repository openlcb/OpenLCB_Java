# How to release OpenLCB to JMRI

## Prerequisites

* You need to have an account on https://oss.sonatype.org that is allowed to upload OpenLCB releases.
* You need to have a git checkout of the OpenLCB_Java repository. This may be a
  fork or you need to have write access to the repository.
* You need to be able to compile java using the ant build toolchain.
* You need to have a git checkout of JMRI, presumably from a fork.
* For the JMRI update you need to have the maven tool installed. (For me maven2
  did not work, had to use maven: ```apt-get install maven```)
* The maven ```settings.xml``` file needs to be configured to authenticate with the ossrh server as described here http://central.sonatype.org/pages/apache-maven.html#distribution-management-and-authentication
* You must have a GPG key to sign the release.

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
  
4. recompile JARs

  ```ant jars```

5. run tests to check that you updated everything

  ```ant run```

  and observe that no errors are reported (at least in the VerionTest).

6. Commit everything and push to github

  commit the four files: manifest, Version.java and the two jars. Push your
  branch to github and create a pull request. Mark in the commit that you are
  increasing the version number.

  Example commit:
  https://github.com/openlcb/OpenLCB_Java/commit/a15a782b30e982d91a039dac797f2f7daaaaca53
  Another example:
  https://github.com/openlcb/OpenLCB_Java/commit/80793002a217d6f3cc2f188a532525d875652e0f

7. Publish to Maven Central

  ```
  mvn versions:set -DnewVersion=0.7.14
  mvn clean
  mvn deploy -P release
  ```

## Update JMRI for new OpenLCB

This process is generally defined by
https://github.com/JMRI/JMRI/blob/master/lib/README.md#updates so it's worth to
check out if the process changed form what's described below.

1. Verify the version to update JMRI with is published at http://repo1.maven.org/maven2/org/openlcb/openlcb

2. Update the master branch of your fork by pulling from upstream

  ```git checkout master```
  
  ```git pull upstream```

  you may also want to update your github fork of the repository

  ```git push origin```

3. Create a new branch for your changes

  ```git checkout -b openlcb-release-update master```

4. Download the new JAR file into libs

  Download the new JAR file into the `lib/` subdirectory (replace VERSION with the real version):
  
  ```curl -o lib/openlcb.jar http://repo1.maven.org/maven2/org/openlcb/openlcb/VERSION/openlcb-VERSION.jar```

5. Update the library dependency version number:

  Edit lib/README.md and update the version number for the openlcb.jar library.
   
  Edit pom.xml and update the version number for the openlcb.jar library.
  
  You don't need to edit build.xml, .classpath, nbproject/ide-file-targets.xml
  and nbproject/project.xml because these do not refer to the file by version
  number.
  
6. It's a good idea to test JMRI with the new library version.

    ```ant panelpro```

    Click around to check a few things related to OpenLCB.

7. Commit your code, binaries and push to github (to your own fork of the
   project)

    Example commits: https://github.com/JMRI/JMRI/pull/2463/commits/a361846a13e6dc84cb43d9b81430b2a9a00418c0

8. Create a pull request for JMRI

    Go to github, open your own fork of the JMRI project, select the branch you
    created (github will give you a quick link in yellow) and click "create
    pull request".

9. Wait for a JMRI project member to approve your pull request.
