package com.erp_mes.erp.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); 
	}
	
	@Bean
	public WebSecurityCustomizer ignoreStaticResources() { // 메서드 이름 무관
		return (web) -> web.ignoring() // web 객체에 대한 보안 필터 무시
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 일반적인 정적 리소트 경로 모두 지정(css, js, images, error 등)
	}
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    	return httpSecurity
    		.csrf(csrf -> csrf
    				.ignoringRequestMatchers("/ws/**") 
    			)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/main","/bootstrap/**").permitAll()
                .requestMatchers("/login").permitAll()
                // 관리자 근태관리 URL 접근 제한
                .requestMatchers("/attendance/adminCommute/**", "/attendance/adminCommuteLog/**")
                .hasAnyRole("AUT001","AUT002")
                .requestMatchers("/dev-login/**").permitAll() // 개발용 로그인 엔드포인트 허용
                .requestMatchers("/personnel/**").authenticated()
                .requestMatchers("/groupware/**").authenticated()
                .requestMatchers("/notice/**").authenticated()
                .requestMatchers("/schedule/**").authenticated()
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/attendance/**").authenticated()
                .requestMatchers("/approval/**").authenticated()
                .requestMatchers("/quality/**").permitAll()
                .requestMatchers("/ws/**", "/chat", "/privateChat").permitAll()
                .requestMatchers("/plant/**t").authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/login")
                .usernameParameter("empId")
                .passwordParameter("empPasswd")
                .defaultSuccessUrl("/main", true)
//                .successHandler(new EmpAuthenticationSuccessHandler())
                .permitAll()
            )
            // 로그아웃 처리
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .deleteCookies("remember-me")
                .permitAll()
            )
            // 자동 로그인처리
            .rememberMe(rememberMeCustomizer -> rememberMeCustomizer
//            		.userDetailsService(userDetailsService())
					.rememberMeParameter("remember-id") // 자동 로그인 수행하기 위한 체크박스 파라미터명 지정
					.tokenValiditySeconds(60 * 60 * 24) // 자동 로그인 토큰 유효기간 설정(기본값 14일 -> 1일 변경)
					)
    		.build();
    }
    
    // 인사 회원 첫 등록 시 사용 
//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//            .userDetailsService(userDetailsService())
//            .passwordEncoder(passwordEncoder())
//            .and()
//            .build();
//    }
//
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//       UserDetails user = User.builder()
//              .username("1234")
//              .password(new BCryptPasswordEncoder().encode("1234"))  
//              .roles("USER")
//              .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }

}