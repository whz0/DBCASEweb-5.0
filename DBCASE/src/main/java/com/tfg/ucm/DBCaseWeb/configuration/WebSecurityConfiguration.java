package com.tfg.ucm.DBCaseWeb.configuration;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@SuppressWarnings("deprecation")
@Configuration
@EnableOAuth2Sso
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
   @Override
   protected void configure(HttpSecurity http) throws Exception {
	   http
       .csrf()
       .disable()
       .authorizeRequests()
       .antMatchers("/index","/", "/**/*.png", "/**/*.css", "/**/*.js", "/**/*.ttf", "/**/*.jpg")
       .permitAll()
       .anyRequest()
       .authenticated()
       .and()
       .formLogin()
       .loginPage("/login")
       .loginProcessingUrl("/perform_login1")
       .defaultSuccessUrl("/homepage.html", true)
       .and()
	   .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	   .logoutSuccessUrl("/index").deleteCookies("JSESSIONID")
	   .invalidateHttpSession(true);
   }
}