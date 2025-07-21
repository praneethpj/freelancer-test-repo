plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":company-lookup"))
    implementation(project(":util"))
    implementation(project(":tenant"))
    implementation(project(":company"))
    implementation(project(":contact"))


    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("junit:junit:4.13.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")

    runtimeOnly("org.postgresql:postgresql")


}