# BinPastes
[![Java CI](https://github.com/querwurzel/BinPastes/actions/workflows/main.yml/badge.svg)](https://github.com/querwurzel/BinPastes/actions/workflows/main.yml)

My turn on creating a simple pastebin.

## Features

* overview of notes / full-text search through title and content
* expiry of notes after some time (or never if desired)
* client-side encryption of the content
* easy setup, single binary
    * zero knowledge required about the build tools, frameworks or programming language I chose

## License

Apache License 2.0 ([Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0))

## Requirements

* JDK 17+
* MySQL 8+

Just put the [JDK](https://adoptium.net/temurin/releases/) somewhere on your file system.
The `bin` folder contains the `java` binary.
You may also want to create an environment variable pointing to the JDK directory.

`export JAVA_HOME=/path/to/jdk`

### How to configure

TODO

### How to build

For productive use with __MySQL__ run:
`./mvnw clean package -Dmysql`

### How to run

For productive use with __MySQL__ run:
`java -Dspring.profiles.active=mysql -jar binpastes.jar`

## Credits

I'd like to thank the creator of the original [PasteBin](https://github.com/lordelph/pastebin) in general and the authors of [sticky-notes](https://github.com/sayakb/sticky-notes) and [Paste](https://github.com/jordansamuel/PASTE)
in particular for their work and efforts in crafting FOSS pastebins for self-hosting.
I used and enjoyed your tools for quite some years, I even maintained them running on newer PHP versions for as long as I could.
