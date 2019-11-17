val kotlinVersion: String = property("kotlin-version").toString()

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
