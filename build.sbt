val commonSettings = Seq(
  scalaVersion := "2.13.4",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.13"),
  organization := "org.gfccollective",

  releaseCrossBuild := true,

  scalacOptions += "-target:jvm-1.8",

  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),

  releasePublishArtifactsAction := PgpKeys.publishSigned.value,

  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  publishArtifact in Test := false,

  pomIncludeRepository := { _ => false },

  licenses := Seq("Apache-style" -> url("https://raw.githubusercontent.com/gfc-collective/gfc-aws-kinesis/master/LICENSE")),

  homepage := Some(url("https://github.com/gfc-collective/gfc-aws-kinesis")),

  pomExtra := (
    <scm>
      <url>https://github.com/gfc-collective/gfc-aws-kinesis.git</url>
      <connection>scm:git:git@github.com:gfc-collective/gfc-aws-kinesis.git</connection>
    </scm>
    <developers>
      <developer>
        <id>andreyk0</id>
        <name>Andrey Kartashov</name>
        <url>https://github.com/andreyk0</url>
      </developer>
      <developer>
        <id>krschultz</id>
        <name>Kevin Schultz</name>
        <url>https://github.com/krschultz</url>
      </developer>
      <developer>
        <id>mikegirkin</id>
        <name>Mike Girkin</name>
        <url>https://github.com/mikegirkin</url>
      </developer>
    </developers>
  )

)

lazy val client = (project in file("client"))
  .settings(commonSettings:_*)
  .settings(
  name := "gfc-aws-kinesis",
  libraryDependencies ++= Seq(
    "org.gfccollective"      %% "gfc-util"                         % "1.0.0",
    "org.gfccollective"      %% "gfc-logging"                      % "1.0.0",
    "org.gfccollective"      %% "gfc-concurrent"                   % "1.0.0",
    "com.amazonaws"          %  "aws-java-sdk-kinesis"             % "1.11.827",
    "com.amazonaws"          %  "amazon-kinesis-client"            % "1.14.0",
    "com.amazonaws"          %  "dynamodb-streams-kinesis-adapter" % "1.5.2",
    "org.specs2"             %% "specs2-scalacheck"                % "4.10.5" % Test,
  )
)

lazy val akka = (project in file("akka"))
  .settings(commonSettings:_*)
  .settings(
    name := "gfc-aws-kinesis-akka",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.12")
  .dependsOn(client)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false)
  .aggregate(client, akka)
