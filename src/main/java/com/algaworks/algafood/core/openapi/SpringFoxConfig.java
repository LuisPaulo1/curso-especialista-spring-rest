package com.algaworks.algafood.core.openapi;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.algaworks.algafood.api.exception.Problem;
import com.algaworks.algafood.api.model.CozinhaModel;
import com.algaworks.algafood.api.openapi.model.CozinhasModelOpenApi;
import com.algaworks.algafood.api.openapi.model.PageableModelOpenApi;
import com.fasterxml.classmate.TypeResolver;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RepresentationBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableOpenApi
public class SpringFoxConfig implements WebMvcConfigurer {

	
	@Bean
	public Docket apiDocket() {
		
		var typeResolver = new TypeResolver();
		
		return new Docket(DocumentationType.OAS_30)
			.select()
				.apis(RequestHandlerSelectors.basePackage("com.algaworks.algafood.api"))
				.build()
				.apiInfo(apiInfo())
				.tags(tags()[0], tags())		
				.useDefaultResponseMessages(false)
				.globalResponses(HttpMethod.GET, globalGetResponseMessages())
				.globalResponses(HttpMethod.POST, globalPostPutResponseMessages())
				.globalResponses(HttpMethod.PUT, globalPostPutResponseMessages())
				.globalResponses(HttpMethod.DELETE, globalDeleteResponseMessages())
				.additionalModels(typeResolver.resolve(Problem.class))
				.additionalModels(typeResolver.resolve(CozinhasModelOpenApi.class))
				.ignoredParameterTypes(ServletWebRequest.class)
				.directModelSubstitute(Pageable.class, PageableModelOpenApi.class)
				.alternateTypeRules(AlternateTypeRules.newRule(
						typeResolver.resolve(Page.class, CozinhaModel.class),
						CozinhasModelOpenApi.class));
	}	
	
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("AlgaFood API")
				.description("API aberta para clientes e restaurantes")
				.version("1.0")
				.contact(new Contact("AlgaWorks", "https://www.algaworks.com", "contato@algaworks.com"))
				.build();
	}
	
	private Tag[] tags() {
		return new Tag[] {
				new Tag("Cidades", "Gerencia as cidades"),
				new Tag("Grupos", "Gerencia os grupos de usuários"),
				new Tag("Cozinhas", "Gerencia as cozinhas"),
				new Tag("Formas de pagamento", "Gerencia as formas de pagamento")
		};
	}
	
	private List<Response> globalGetResponseMessages() {
		return Arrays.asList(
			new ResponseBuilder()					
				.code(String.valueOf(HttpStatus.NOT_ACCEPTABLE.value()))
				.description("Recurso não possui representação que poderia ser aceita pelo consumidor")
				.build(),
			new ResponseBuilder()
				.code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.description("Erro interno do servidor")
				.representation(MediaType.APPLICATION_JSON ).apply(builderModelProblema())
				.build()
		);			
	}	
	
	private List<Response> globalPostPutResponseMessages() {
		return Arrays.asList(
			new ResponseBuilder()					
				.code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
				.description("Requisição inválida (erro do cliente)")
				.representation(MediaType.APPLICATION_JSON ).apply(builderModelProblema())
				.build(),
			new ResponseBuilder()					
				.code(String.valueOf(HttpStatus.NOT_ACCEPTABLE.value()))
				.description("Recurso não possui representação que poderia ser aceita pelo consumidor")
				.build(),				
			new ResponseBuilder()					
				.code(String.valueOf(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
				.description("Requisição recusada porque o corpo está em um formato não suportado")
				.build(),		
			new ResponseBuilder()
				.code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.description("Erro interno do servidor")
				.build()				
		);			
	}			
	
	private List<Response> globalDeleteResponseMessages() {
		return Arrays.asList(
			new ResponseBuilder()					
				.code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
				.description("Requisição inválida (erro do cliente)")
				.representation(MediaType.APPLICATION_JSON ).apply(builderModelProblema())
				.build(),
			new ResponseBuilder()
				.code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.description("Erro interno do servidor")
				.representation(MediaType.APPLICATION_JSON ).apply(builderModelProblema())
				.build()
		);			
	}	
	
	private Consumer<RepresentationBuilder> builderModelProblema() {
		return r -> r.model(m -> m.name("Problema")
				.referenceModel(
					ref -> ref.key(
							k -> k.qualifiedModelName(q -> q.name("Problema").namespace("com.algaworks.algafood.api.exception")
						))));
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		 registry.addResourceHandler("index.html")
         .addResourceLocations("classpath:/META-INF/resources/");
	}
	
}
