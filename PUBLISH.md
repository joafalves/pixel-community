# PUBLISH #

### Who is this documentation for? ###

If you are responsible for publishing pixel artifacts on public repositories.

### Introduction ###

The public repository for pixel is currently powered by Sonatype (issue #OSSRH-76004)
Most of the deployment steps required can be found on the following page:

https://central.sonatype.org/publish/publish-guide/#deployment

The following subchapters assume that the project is already created on Sonatype.

### Steps ###

#### Configuration ####

Make sure that `~/.gradle/gradle.properties` has the signing and credentials filled in as follows:

```
signing.keyId=<short-gpg-key-id>
signing.password=<gpg-private-key>
signing.secretKeyRingFile=<absolute-path-to-gpg-secret-ring-file>

sonatypeUsername=<sonatype-username>
sonatypePassword=<sonatype-password>
```

All published artifacts will be signed with the GPG key (more info 
[here](https://central.sonatype.org/publish/requirements/gpg/))

#### Remote publish ####

This command sequence will publish the current build target to a remote staging repository:

`./gradlew clean publish -DBUILD_TARGET=all`

Note that the `BUILD_TARGET` argument can be changed to match a specific OS target (natives-windows-arm64,
natives-macos-arm64, etc...). 

To specify the publishing version, define the following argument (adjust the version as needed):

`-DPIXEL_RELEASE=1.0.0`

Example of a complete publish command line in a build node:

`./gradlew clean publish -DBUILD_TARGET=all -DPIXEL_RELEASE=1.0.0`

After all intended build targets are published to staging, visit the following 
[Nexus page](https://s01.oss.sonatype.org/index.html).

After logging-in, open the `Staging Repositories` and, after validation, `close` the intended staging entry, so it can
be verified for release. If everything is OK, press `release` to make the artifacts available publicly.

#### Local publish ####

This is useful primarily for testing as you can simulate the full publishing operation locally (maven local repository):

`./gradlew publishToMavenLocal -DBUILD_TARGET=all -DPIXEL_RELEASE=LOCAL`

Note that after this operation is completed, you can import the pixel dependency directly from another project
by using the specified local version (useful for importing the most up-to-date pixel implementation).



