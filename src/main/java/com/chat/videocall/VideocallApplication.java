package com.chat.videocall;

import com.chat.videocall.user.User;
import com.chat.videocall.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VideocallApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideocallApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			UserService service
	) {
		return args -> {
			service.register(User.builder()
					.username("Mihajlo")
					.email("mihajlo@mail.com")
					.password("mihajlo")
					.build());

			service.register(User.builder()
					.username("Aleks")
					.email("aleks@mail.com")
					.password("aleks")
					.build());

			service.register(User.builder()
					.username("Mijusko")
					.email("mijusko@mail.com")
					.password("mijusko")
					.build());
		};
	}

}
