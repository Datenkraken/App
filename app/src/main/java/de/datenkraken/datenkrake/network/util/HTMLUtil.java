package de.datenkraken.datenkrake.network.util;

import android.net.Uri;

import java.util.Iterator;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utility class, providing functionality to work with the HTML content.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public final class HTMLUtil {

    /**
     * Private constructor so this class doesn't get instantiated and to silence PMD.
     */
    private HTMLUtil() {

    }

    /**
     * Returns the best suitable image for front page purpose and removes it from the given elements
     * and the DOM tree. Currently it's the first image in the given elements.
     *
     * @param elements of the search-space the image can be contained in.
     * @return Uri leading to an image, null if no suitable image was found.
     */
    public static Uri getTitleImage(Elements elements) {
        if (elements == null) {
            return null;
        }

        Iterator<Element> iterator = elements.iterator();
        Element element;

        while (iterator.hasNext()) {
            element = iterator.next();
            if (element != null && element.hasAttr("src")) {
                element.remove(); // remove from DOM tree
                iterator.remove(); // remove from given list
                return Uri.parse(element.attr("src"));
            }
        }
        return null;
    }

    /**
     * Removes all image tracker from the given elements and their DOM tree.
     * Identifies them by there size (trackerSizeLimit).
     *
     * @param elements Elements with or without image tracker
     * @param trackerSizeLimit size limit for trackers to be filtered out.
     */
    public static void removeImageTracker(Elements elements, int trackerSizeLimit) {
        Iterator<Element> iterator = elements.iterator();
        Element element;
        while (iterator.hasNext()) {
            element = iterator.next();
            if (checkIfImageIsSmaller(element, trackerSizeLimit)) {
                element.remove(); // remove from DOM tree
                iterator.remove(); // remove from given list
            }
        }
    }


    /**
     * Checks, if the size of a given element is smaller than the given size. <br>
     * If there is no size in the element, it will assume that the element is bigger than the size.
     *
     * @param element to be checked for its size.
     * @param size minimum size of the element.
     * @return true, if element has smaller width or height, than size, else false.
     */
    public static boolean checkIfImageIsSmaller(Element element, int size) {
        if (element == null) {
            return false;
        }

        String width = element.attr("width");
        String height = element.attr("height");

        if (width == null || height == null || height.isEmpty() || width.isEmpty()) {
            return false;
        }

        boolean minWidth = Integer.parseInt(width) < size;
        boolean minHeight = Integer.parseInt(height) < size;
        return minHeight || minWidth;
    }
}
