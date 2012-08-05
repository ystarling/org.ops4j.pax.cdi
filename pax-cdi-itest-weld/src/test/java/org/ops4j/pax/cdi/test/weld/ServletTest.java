/*
 * Copyright 2012 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.cdi.test.weld;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.ops4j.pax.cdi.test.weld.TestConfiguration.regressionDefaults;
import static org.ops4j.pax.cdi.test.weld.TestConfiguration.workspaceBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.cdi.sample1.client.IceCreamClient;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ServletTest {

    @Inject
    private CdiContainerFactory containerFactory;

    @Inject
    private IceCreamClient iceCreamClient;

    @Configuration
    public Option[] config() {
        return options(
            regressionDefaults(),

            workspaceBundle("pax-cdi-samples/pax-cdi-sample1"),
            workspaceBundle("pax-cdi-samples/pax-cdi-sample1-client"),
            //workspaceBundle("pax-cdi-samples/pax-cdi-sample1-web"),
            mavenBundle("org.ops4j.pax.cdi.samples", "pax-cdi-sample1-web", "0.2.0-SNAPSHOT"),
            workspaceBundle("pax-cdi-extender"),
            workspaceBundle("pax-cdi-extension"),
            workspaceBundle("pax-cdi-api"),
            workspaceBundle("pax-cdi-spi"),
            workspaceBundle("pax-cdi-web"),
            workspaceBundle("pax-cdi-weld"),

            mavenBundle("org.slf4j", "slf4j-ext", "1.6.4"),
            mavenBundle("ch.qos.cal10n", "cal10n-api", "0.7.4"),
            mavenBundle("org.ops4j.pax.swissbox", "pax-swissbox-tracker").versionAsInProject(),
            mavenBundle("org.apache.xbean", "xbean-finder").versionAsInProject(),
            mavenBundle("org.apache.geronimo.specs", "geronimo-interceptor_1.1_spec")
                .versionAsInProject(),
            mavenBundle("org.apache.geronimo.specs", "geronimo-el_2.2_spec").versionAsInProject(),
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.asm")
                .versionAsInProject(), //
            mavenBundle("org.jboss.weld", "weld-osgi-bundle", "1.2.0-SNAPSHOT"),

            // Pax Web

            mavenBundle("org.ops4j.pax.web", "pax-web-spi")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.web", "pax-web-api")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.web", "pax-web-extender-war")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.web", "pax-web-extender-whiteboard")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.web", "pax-web-jetty")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.web", "pax-web-runtime")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.web", "pax-web-jsp")
                .version("2.0.3-SNAPSHOT"),
            mavenBundle("org.eclipse.jdt.core.compiler", "ecj")
                .version("3.5.1"),
            mavenBundle("org.eclipse.jetty", "jetty-util")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-io")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-http")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-continuation")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-server")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-security")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-xml")
                .version("8.1.4.v20120524"),
            mavenBundle("org.eclipse.jetty", "jetty-servlet")
                .version("8.1.4.v20120524"),
            mavenBundle("org.apache.geronimo.specs", "geronimo-servlet_3.0_spec").version("1.0"),
            mavenBundle("org.osgi", "org.osgi.compendium", "4.3.0")

        );

    }

    @Test
    public void checkContainers() throws InterruptedException {
        assertThat(containerFactory.getContainers().size(), is(2));
    }

}