group = "app.hemk651"

patches {
    about {
        name = "Meta Patches"
        description = "Shared MetaConfig file patch for selected Meta Android apps"
        source = "https://github.com/hemk651/meta-patches"
        author = "hemk651"
        contact = "https://github.com/hemk651"
        website = "https://github.com/hemk651/meta-patches"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Generate the Morphe patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
