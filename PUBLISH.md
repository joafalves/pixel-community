# PUBLISH #

### Who is this file for? ###

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

All published artifacts will be signed with the GPG key (more info [here](/Users/jfalves/Workspace/keys/secret-keys.gpg))

#### Local publish ####

This is useful primarily for testing as you can simulate the full publishing operation locally (maven local repository):

`gradle publishToMavenLocal`

#### Remote publish ####

This command will publish the current build target to a remote staging repository:

`gradle publish`

This command can be executed multiple times with different build targets (for eg. macos & windows). 

After all intended build targets are published to staging, visit the following [Nexus page](https://s01.oss.sonatype.org/index.html).

After logging-in, open the `Staging Repositories` and, after validation, `close` the intended staging entry, so it can be
verified for release. If everything is OK, press `release` to make the artifacts available publicly.



