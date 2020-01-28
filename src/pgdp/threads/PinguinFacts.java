package pgdp.threads;

import java.util.Arrays;
import java.util.List;

public class PinguinFacts {
	/**
	 * SOURCE: https://www.factretriever.com/penguin-facts
	 */
	private static List<String> facts = Arrays.asList(
			"Penguins are one of about 40 species of flightless birds.",
			"Most scientists agree that there are 17 species of penguins. Of the 17 species, 13 are either threatened or endangered, with some on the brink of extinction.",
			"Generally, penguins are not sexually dimorphic, meaning male and female penguins look alike.",
			"Penguins swallow pebbles and stones as well as their food.",
			"Penguins do not have teeth. Instead they use their beak to grab and hold wiggling prey.",
			"Penguins spend several hours a day preening or caring for their feathers.",
			"The penguin with the highest number of species is the Macaroni Penguin, with approximately 11,654,000 pairs.",
			"Penguins molt, or lose their feathers, once a year. They always molt on land or ice and until they grow new waterproof coats, they are unable to go into the water. Molting may take weeks, and most penguins lose about half their body weight during this time.",
			"Penguins are highly social birds. Even at sea, penguins usually swim and feed in groups. Some penguin colonies on Antarctica are huge and can contain 20 million or more penguins at various times during the year.",
			"Penguins’ eyes work better under water than they do in air. Many scientists believe penguins are extremely short-sighted on land.",
			"The Galapagos Penguin lives farther north than any other penguin and is the only penguin that might venture into the Northern Hemisphere.",
			"Larger penguins usually live in cooler regions. Smaller penguins are typically found in more temperate and tropical climates.",
			"Some prehistoric penguins were very large, growing nearly as tall and heavy as a human.",
			"Penguins can control the blood flow to their extremities in order to reduce the amount of blood that gets cold, but not enough so that their extremities freeze.",
			"Penguins can drink salt water because they have a special gland, the supraorbital gland, that filters salt from the bloodstream.",
			"Most penguins are found in South Africa, New Zealand, Chili, Antarctica, Argentina, and Australia.",
			"Penguins mate, nest, and raise their chicks in a place called a 'rookery'.",
			"Penguins typically are not afraid of humans."
	);

	public static String getRandomFact() {
		return facts.get(randomIndex());
	}

	private static int randomIndex() {
		return (int) (Math.random() * facts.size());
	}
}
