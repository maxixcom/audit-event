package com.boomstream.userlog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UserlogApplication

fun main(args: Array<String>) {
	runApplication<UserlogApplication>(*args)
}
