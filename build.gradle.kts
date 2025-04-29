plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("kapt") version "1.9.22"
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "11.8.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.flywaydb:flyway-database-postgresql:11.8.0")
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.0.0-M7"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.postgresql:postgresql:42.7.3")
	implementation("org.flywaydb:flyway-database-postgresql:11.8.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.6")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("org.postgresql:postgresql")
	developmentOnly("org.springframework.ai:spring-ai-spring-boot-docker-compose")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.ai:spring-ai-spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

flyway {
	url = "jdbc:postgresql://localhost:5432/postgres"
	user = System.getenv("FLYWAY_USER")
	password = System.getenv("FLYWAY_PASS")
	locations = arrayOf("filesystem:src/main/resources/db/migration")
	cleanDisabled = false
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

kapt {
	correctErrorTypes = true
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
	imageName.set("${project.group}/${rootProject.name}:${project.version}")

	environment.set(mapOf(
		"BP_JVM_VERSION" to "21.*"
	))

	tags.set(setOf("${project.group}/${rootProject.name}:latest"))

	builder.set("paketobuildpacks/builder-jammy-base:latest")
	runImage.set("paketobuildpacks/run-jammy-base:latest")
}

// --- テスト時のログ出力設定（詳細ログを出力するための設定） ---
// ./gradlew build が失敗した場合に、有効化して調査する

// tasks.withType<Test> {
// 	testLogging {
// 		events("passed", "failed", "skipped", "standard_out", "standard_error")
// 		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
// 		showExceptions = true
// 		showCauses = true
// 		showStackTraces = true
// 	}
// }
