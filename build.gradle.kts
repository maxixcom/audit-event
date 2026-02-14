plugins {
	kotlin("jvm") version "2.2.21"
	`java-library`
	`maven-publish`
}

group = "com.boomstream"
version = "0.0.1-SNAPSHOT"
description = "User audit events logging library"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
	withSourcesJar()
	withJavadocJar()
}

repositories {
	mavenCentral()
}

dependencies {
	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// JSON сериализация
	implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

	// Тестирование
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])

			pom {
				name.set("User Log Library")
				description.set("Structured audit event logging library for Kotlin/Java")
				url.set("https://github.com/boomstream/user-log-lib")

				licenses {
					license {
						name.set("MIT License")
						url.set("https://opensource.org/licenses/MIT")
					}
				}

				developers {
					developer {
						id.set("boomstream")
						name.set("Boomstream")
					}
				}
			}
		}
	}
}
