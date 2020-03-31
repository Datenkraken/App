package de.datenkraken.datenkrake.util;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Helper class, containing useful Function, such as joining an integer to a long or generating uids.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class Helper {

    /**
     * Private constructor so this class doesn't get instantiated and to silence PMD.
     */
    private Helper() {
    }

    /**
     * Joins two integer bitwise to one long. <br>
     * The second integer will be appended to the end of the first. <br>
     * If the integers are not long enough, the remaining bits will be filled with 1s.
     *
     * @param hi Integer, which will be the 32 high bits of the long
     * @param lo Integer, which will be the 32 low bits of the long
     * @return long, composing of both Integer.
     */
    public static long joinIntToLong(int hi, int lo) {
        return (((long) hi) << 32) | (lo & 0xffffffffL);
    }

    /**
     * Generates the {@link Article} uid of the given {@link Article}. <br>
     * Uses the {@link Helper#joinIntToLong(int, int)} to join the hash of the given articles title
     * and the hash of the author and {@link Source} url. <br>
     * This long is the uid of the article.
     *
     * @param article {@link Article} for which the new uid should be calculated.
     * @return the uid of the given {@link Article} calculated by combining the two hashes.
     */
    public static long generateArticleUid(@NotNull Article article) {
        return  joinIntToLong(Objects.hash(article.title),
            Objects.hash(article.author, article.source.url));
    }

    /**
     * Generates the {@link Source} uid of the given {@link Source}. <br>
     * Uses the {@link Helper#joinIntToLong(int, int)} to join the hash of the first half of
     * the url string, and the hash of the second half and the length. <br>
     * This long is the source uid.
     *
     * @param source {@link Source} for which the new uid should be generated.
     * @return the uid of the given {@link Source} calculated by combining the two hashes.
     */
    public static long generateSourceUid(@NotNull Source source) {
        String url = source.url.toString();
        int hash1 = Objects.hash(url.substring(0, (int) Math.floor(url.length() / 2)));
        int hash2 = Objects.hash(url.substring((int) Math.floor(url.length() / 2)), url.length());
        return Helper.joinIntToLong(hash1, hash2);
    }
}
