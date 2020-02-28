package org.goflex.wp2.fman;

/*-
 * #%L
 * GOFLEX::WP2::FlexOfferManager Backend
 * %%
 * Copyright (C) 2017 - 2020 The GOFLEX Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */



import org.goflex.wp2.fman.user.UserRepository;
import org.goflex.wp2.fman.user.UserRole;
import org.goflex.wp2.fman.user.UserT;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.Executor;

@SpringBootApplication(scanBasePackages={"org.goflex.wp2.fman", "org.goflex.wp2.core"})
@EnableScheduling
@EnableAsync
@Configuration
@EntityScan(basePackages = {"org.goflex.wp2.fman", "org.goflex.wp2.core"} )
@ComponentScan({"org.goflex.wp2.fman", "org.goflex.wp2.core"})
public class FmanApplication {

	@Autowired
	private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(FmanApplication.class, args);
	}

	@Bean
	InitializingBean initDatabase() {
		return () -> {

			if (userRepository.count() == 0) {

				UserT u = new UserT();
				u.setUserName("admin");
				u.setFirstName("< Please set >");
				u.setLastName("< Please set >");
				u.setEmail("< Please set @ me > ");
				u.setPassword(passwordEncoder.encode("admin"));
				u.setRole(UserRole.ROLE_ADMIN);

				userRepository.save(u);
			}

			String systemUser = "sysadmin";
			String systemUserPass = "XXXXXXXXXXXXXX";
			if (userRepository.findByUserName(systemUser) == null) {

                    UserT u1 = new UserT();
					u1.setUserName(systemUser);
					u1.setFirstName("System");
					u1.setLastName("Admin");
					u1.setEmail("sysadmin@email.com");
					u1.setPassword(passwordEncoder.encode(systemUserPass));
					u1.setRole(UserRole.ROLE_ADMIN);

					userRepository.save(u1);
			}
		};
	}


    @Bean(name = "threadPoolTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("Async-");
		executor.setCorePoolSize(3);
		executor.setMaxPoolSize(3);
		executor.setQueueCapacity(600);
        executor.initialize();
        return executor;
    }
}
