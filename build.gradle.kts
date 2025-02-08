plugins {
    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    // JSON Parsing Library
    implementation("org.json:json:20210307")

    // HTTP Client for API Calls
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // SQLite JDBC Driver
    implementation("org.xerial:sqlite-jdbc:3.36.0")

    // Apache Commons Math (For Stock Price Prediction)
    implementation("org.apache.commons:commons-math3:3.6.1")
}

application {
    mainClass.set("com.sdm.stock.StockDataFetcher")
}

