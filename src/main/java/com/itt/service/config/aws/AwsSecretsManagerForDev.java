package com.itt.service.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.itt.service.config.AwsSecrets;

@Configuration
public class AwsSecretsManagerForDev {

	@Bean("awsSecrets")
	public AwsSecrets getSecret() {
		AwsSecrets secrets = new AwsSecrets();
		// Write Database (Master)
		secrets.setDatabaseWriteHost("apps-itt-rds-dev.cpkkcewaom6b.us-east-1.rds.amazonaws.com");
		secrets.setDatabaseWritePort("3306");
		secrets.setDatabaseWriteUsername("apps-itt-srv-dev");
		secrets.setDatabaseWritePassword("Sh@dow#Moon_Flame37@");
		secrets.setDatabaseWriteDatabase("TnT-ITT");

		// Read Database (Replica)
		secrets.setDatabaseReadHost("apps-itt-rds-dev.cpkkcewaom6b.us-east-1.rds.amazonaws.com");
		secrets.setDatabaseReadPort("3306");
		secrets.setDatabaseReadUsername("apps-itt-srv-dev");
		secrets.setDatabaseReadPassword("Sh@dow#Moon_Flame37@");
		secrets.setDatabaseReadDatabase("TnT-ITT");

		// Auth0 Configuration
		secrets.setAuth0ClientId("dLQlQhjgWSw63xlub2nQcZZxRanqgowO");
		secrets.setAuth0ClientSecret("FZi1maVpvdc_FOvXBslvMo6PBY0RLeOZO0-1CgkUro12qOjQMVDrg84dYTrJJPdd");
		secrets.setAuth0Domain("bdpint-smarthub-test.us.auth0.com");

		// OAuth2 Configuration
		secrets.setOauth2JwtIssuerUri("https://testlogin.bdpsmart.com/");

		return secrets;
	}
}