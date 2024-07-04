// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization     := "se.thanh"
ThisBuild / organizationName := "thanh"
ThisBuild / startYear        := Some(2024)
ThisBuild / licenses         := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("lenguyenthanh", "Thanh Le")
)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / tlJdkRelease               := Some(11)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := false

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

ThisBuild / scalaVersion := "3.3.3"

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "bson4s",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % "2.11.0",
      "org.typelevel" %%% "cats-effect" % "3.5.4",
      "org.mongodb"     % "bson"        % "5.1.1"
    )
  )

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
