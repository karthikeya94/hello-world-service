package com.hello.world.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEurekaClient
@RestController
@EnableFeignClients
@RibbonClient(name="gateway-service",configuration=RibbonConfiguration.class)
public class HelloWorldMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldMicroserviceApplication.class, args);
	}

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private WorldClient world;

	@GetMapping("/helloworld")
	public String getHelloWorld(){
		StringBuilder sb = new StringBuilder();
		IntStream.range(1,100).forEach(i->{
			ResponseEntity<String> hello = restTemplate.getForEntity("http://gateway-service/hello",String.class);
			ResponseEntity<String> wo = world.getWorldEntity();
			sb.append(hello.getBody()+"\t"+wo.getBody()+"<br>");
		});
		return sb.toString();
	}

	@LoadBalanced
	@Bean
	public RestTemplate rest(RestTemplatebuilder builder){
		return builder.build();
	}

	@FeignClient(name="${feign.name}")
	interface WorldClient{
		@RequestMapping(method=RequestMethod.GET,value="/world")
		String getWorld();
		@RequestMapping(method=RequestMethod.GET,value="/world")
		ResponseEntity<String> getWorldEntity();
	}

}
class RibbonConfiguration{
	@Autowired
	IClientConfig ribbonClientConfig;
	@Bean
	public IPing ribbonPing(IClientConfig config){
		return new PingUrl();
	}
	@Bean
	public IRule ribbonRule(IClientConfig config){
		return new WeightedResponseTimeRule();
	}
}
