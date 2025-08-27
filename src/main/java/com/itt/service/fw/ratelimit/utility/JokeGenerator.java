package com.itt.service.fw.ratelimit.utility;

import com.itt.service.fw.ratelimit.dto.response.JokeResponseDto;
import net.datafaker.Faker;
import net.datafaker.providers.entertainment.HarryPotter;
import net.datafaker.providers.entertainment.Joke;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating random jokes.
 */
@Component
public class JokeGenerator {

	private final Joke joke;
	private final HarryPotter harryPotter;

	public JokeGenerator() {
		this.joke = new Faker().joke();
		this.harryPotter=new Faker().harryPotter();
	}

	/**
	 * Generates a random joke.
	 *
	 * @return JokeResponseDto containing the generated joke
	 */
	public JokeResponseDto generate() {
		if(false){
			return harryPotter();
		}
		final var pun = joke.pun();
		return JokeResponseDto.builder().joke(pun).build();
	}
	public JokeResponseDto harryPotter() {
		final var pun = harryPotter.character();
		return JokeResponseDto.builder().joke(pun).build();
	}

}
