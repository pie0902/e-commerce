package org.example.dollaruser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
	"org.example.share.config.global.entity.user",
	"org.example.dollaruser.address.entity"
})
public class DollarUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(DollarUserApplication.class, args);
	}

}
