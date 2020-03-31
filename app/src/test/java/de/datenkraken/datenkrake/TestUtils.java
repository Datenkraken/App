package de.datenkraken.datenkrake;

import org.mockito.internal.util.io.IOUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @version 1.0
 * @since 06.12.2019
 * Class containing useful functionality for testing purpose.
 */
class TestUtils {

    /**
     * Loads the resource at the given path.
     * 'projectDir/app/src/test/resources' is the root path
     * @param path resource to load
     * @return resource as string
     * @throws IOException throws when the resource is in use
     */
    static String getResource(String path) throws IOException {
        if (TestUtils.class.getClassLoader() == null) {
            return null;
        }

        ArrayList<String> list = new ArrayList<String>(IOUtil.readLines(TestUtils.class.getClassLoader().getResourceAsStream(path)));
        StringBuilder result = new StringBuilder();

        for (String s : list) {
            result.append(s).append("\n");
        }
        return result.toString();
    }
}
