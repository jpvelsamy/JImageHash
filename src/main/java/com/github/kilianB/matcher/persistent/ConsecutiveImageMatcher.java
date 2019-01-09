package com.github.kilianB.matcher.persistent;

import java.awt.image.BufferedImage;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import com.github.kilianB.datastructures.tree.Result;
import com.github.kilianB.datastructures.tree.binaryTree.BinaryTree;
import com.github.kilianB.hashAlgorithms.HashingAlgorithm;
import com.github.kilianB.matcher.Hash;

/**
 * Convenience class allowing to chain multiple hashing algorithms to find
 * similar images. The ConsecutiveImageMatcher keeps the hashes and buffered images
 * in cache.
 * 
 * @author Kilian
 *
 */
public class ConsecutiveImageMatcher extends PersitentBinaryTreeMatcher {

	private static final long serialVersionUID = 831914616034052308L;

	/**
	 * A preconfigured image matcher chaining dHash and pHash algorithms for fast
	 * high quality results.
	 * <p>
	 * The dHash is a quick algorithms allowing to filter images which are very
	 * unlikely to be similar images. pHash is computationally more expensive and
	 * used to inspect possible candidates further
	 * 
	 * @return The matcher used to check if images are similar
	 */
	public static PersitentBinaryTreeMatcher createDefaultMatcher() {
		return createDefaultMatcher(Setting.Quality);
	}

	/**
	 * A preconfigured image matcher chaining dHash and pHash algorithms for fast
	 * high quality results.
	 * <p>
	 * The dHash is a quick algorithms allowing to filter images which are very
	 * unlikely to be similar images. pHash is computationally more expensive and
	 * used to inspect possible candidates further
	 * 
	 * @param algorithmSetting
	 *                         <p>
	 *                         How aggressive the algorithm advances while comparing
	 *                         images
	 *                         </p>
	 *                         <ul>
	 *                         <li><b>Forgiving:</b> Matches a bigger range of
	 *                         images</li>
	 *                         <li><b>Fair:</b> Matches all sample images</li>
	 *                         <li><b>Quality:</b> Recommended: Does not initially
	 *                         filter as aggressively as Fair but returns usable
	 *                         results</li>
	 *                         <li><b>Strict:</b> Only matches images which are
	 *                         closely related to each other</li>
	 *                         </ul>
	 * 
	 * @return The matcher used to check if images are similar
	 */
	public static ConsecutiveImageMatcher createDefaultMatcher(Setting algorithmSetting) {
		ConsecutiveImageMatcher matcher = new ConsecutiveImageMatcher();
		matcher.addDefaultHashingAlgorithms(matcher,algorithmSetting);
		return matcher;
	}
	
	@Override
	public PriorityQueue<Result<String>> getMatchingImages(BufferedImage image, double maxiumDistance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Search for all similar images passing the algorithm filters supplied to this
	 * matcher. If the image itself was added to the tree it will be returned with a
	 * distance of 0
	 * 
	 * @param image The image other images will be matched against
	 * @return Similar images Return all images sorted by the
	 *         <a href="https://en.wikipedia.org/wiki/Hamming_distance">hamming
	 *         distance</a> of the last applied algorithms
	 */
	public PriorityQueue<Result<String>> getMatchingImages(BufferedImage image) {

		if (steps.isEmpty())
			throw new IllegalStateException(
					"Please supply at least one hashing algorithm prior to invoking the match method");

		PriorityQueue<Result<String>> returnValues = null;

		for (Entry<HashingAlgorithm, AlgoSettings> entry : steps.entrySet()) {
			HashingAlgorithm algo = entry.getKey();

			BinaryTree<String> binTree = binTreeMap.get(algo);
			AlgoSettings settings = entry.getValue();

			Hash needleHash = algo.hash(image);

			int threshold = 0;
			if (settings.isNormalized()) {
				int hashLength = needleHash.getBitResolution();
				threshold = (int)Math.round(settings.getThreshold() * hashLength);
			} else {
				threshold = (int) settings.getThreshold();
			}

			PriorityQueue<Result<String>> temp = binTree.getElementsWithinHammingDistance(needleHash, threshold);

			if (returnValues == null) {
				returnValues = temp;
			} else {
				temp.retainAll(returnValues);
				returnValues = temp;
			}
		}
		return returnValues;
	}
	
	// Don't keep a reference to the image so the garbage collector can release it
}
