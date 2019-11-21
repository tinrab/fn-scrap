val kotlinVersion: String = property("kotlin-version").toString()

dependencies {
    implementation(project(":scrap-common"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("com.typesafe:config:1.4.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.rabbitmq:amqp-client:5.7.3")
    implementation("io.javalin:javalin:3.6.0")
    implementation("org.slf4j:slf4j-simple:1.7.29")
}
