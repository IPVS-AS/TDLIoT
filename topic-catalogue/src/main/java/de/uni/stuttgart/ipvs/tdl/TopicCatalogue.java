package de.uni.stuttgart.ipvs.tdl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.uni.stuttgart.ipvs.tdl.property.PropertyLoader;

@SpringBootApplication
public class TopicCatalogue {

	public static void main(String[] args) {
		PropertyLoader loader = new PropertyLoader();
		loader.loadPropertyValues();
		SpringApplication.run(TopicCatalogue.class, args);
	}
}
