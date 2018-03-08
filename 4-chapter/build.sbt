// give the user a nice default project!
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "josiah.learningspark",
      scalaVersion := "2.11.11",
      scalacOptions += "-target:jvm-1.7",
      version := "0.1.0",
      resolvers += Classpaths.typesafeReleases,
      resolvers += Resolver.sonatypeRepo("releases"),
      resolvers += "Cloudera Artifactory Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )),
    name := "4-chapter-page-rank",
    version := "1.0.0",
    sparkVersion := "1.6.0",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    sparkComponents := Seq("core", "sql", "catalyst", "mllib"),
    parallelExecution in Test := false,
    fork := true,
    cancelable in Global := true,
    coverageHighlighting := true,
    javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "2.2.0.cloudera2",
      "org.typelevel" %% "cats-core" % "1.0.0-MF",
      "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "com.lihaoyi" %% "sourcecode" % "0.1.4",

      // Test your code PLEASE!!!
      "org.scalatest" %% "scalatest" % "3.0.2" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "com.holdenkarau" %% "spark-testing-base" % "1.6.0_0.7.2" % "test"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    pomIncludeRepository := { x => true },
    resolvers ++= Seq(
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
      Resolver.sonatypeRepo("public")
    ),
    // publish settings
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  )
