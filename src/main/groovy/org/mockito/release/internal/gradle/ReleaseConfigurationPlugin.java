package org.mockito.release.internal.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.mockito.release.gradle.ReleaseConfiguration;

/**
 * Adds extension for configuring the release.
 * Intended to be used on the root project because the release is typically configured on the root project.
 * Adds extensions:
 * <ul>
 *     <li>releasing - {@link ReleaseConfiguration}</li>
 * </ul>
 */
public class ReleaseConfigurationPlugin implements Plugin<Project> {

    public final static String EXTENSION_NAME = "releasing";
    public final static String BINTRAY_KEY_ENV = "BINTRAY_API_KEY";

    public void apply(Project project) {
        //TODO unit test
        ReleaseConfiguration conf = project.getExtensions().create(EXTENSION_NAME, ReleaseConfiguration.class);
        if (project.hasProperty("releaseDryRun")) {
            conf.setDryRun(true);
        }
        String apiKey = System.getenv(BINTRAY_KEY_ENV);
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            conf.getBintray().setApiKey(apiKey);
        }
    }
}
