val kotlinVersion: String = property("kotlin-version").toString()

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("com.typesafe:config:1.4.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.rabbitmq:amqp-client:5.7.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
