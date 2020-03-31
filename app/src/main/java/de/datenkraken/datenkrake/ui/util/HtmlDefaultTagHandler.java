package de.datenkraken.datenkrake.ui.util;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

import timber.log.Timber;

/**
 * TagHandler for unknown Tags. Uses Timber to display the unknown Tag. <br>
 * Implements Html.TagHandler. <br>
 * Displays a timber message of the form: "Parser did not know Tag: ... ".
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class HtmlDefaultTagHandler implements Html.TagHandler {

    /**
     * Displays an unknown Tag with Timber.
     *
     * @param opening if tag is opening. Not used here.
     * @param tag that is not known.
     * @param output for TagHandler. Not used here.
     * @param xmlReader to read XML. Not used here.
     */
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        Timber.d("Parser did not know Tag: %s", tag);
    }
}
