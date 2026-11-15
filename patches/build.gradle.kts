group = "app.meta"

patches {
    about {
        name = "Meta Patches"
        description = "Unlocks Meta's internal Employee Options / developer options in Instagram, Edits and Instants"
        source = "https://github.com/hemk651/meta-patches"
        author = "hemk651"
        contact = "https://github.com/hemk651/meta-patches/issues"
        website = "https://github.com/hemk651/meta-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
