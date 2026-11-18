package dev.demo.accounts;

import org.springframework.boot.SpringApplication;

public class TestGitopsDemoAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(GitopsDemoAppApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
