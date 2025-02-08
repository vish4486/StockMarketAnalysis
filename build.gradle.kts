plugins {
    id("application")
    id("java")
}

group = "com.sdm.stock"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // HTTP Client for API requests
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    
    // JSON Parsing
    implementation("org.json:json:20210307")
    
    // SQLite JDBC Driver
    implementation("org.xerial:sqlite-jdbc:3.34.0")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    // JUnit for Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

application {
    mainClass.set("com.sdm.stock.StockDataFetcher")
}

tasks.test {
    useJUnitPlatform()
}

