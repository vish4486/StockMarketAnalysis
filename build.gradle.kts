plugins {
    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    // JSON Parsing Library
    implementation("org.json:json:20210307")

    // HTTP Client for Fetching Stock Data
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Apache Commons Math for Linear Regression Prediction
    implementation("org.apache.commons:commons-math3:3.6.1")
}

application {
    mainClass.set("com.sdm.stock.StockGUI") // Ensure GUI is the main class
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Ensure Java 17+
}

// Ensure all dependencies are packaged into the JAR file
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.sdm.stock.StockGUI"
    }
    from(*configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }.toTypedArray())
}

