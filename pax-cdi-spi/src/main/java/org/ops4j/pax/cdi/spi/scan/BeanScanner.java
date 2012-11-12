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
package org.ops4j.pax.cdi.spi.scan;

import static org.ops4j.pax.cdi.api.Constants.MANAGED_BEANS_KEY;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans a bundle for candidate managed bean classes. The scanner only looks at bundle entries but
 * does not load classes. The set of candidate classes is passed to the CDI implementation which
 * will discard some of the candidates, e.g. if the class cannot be loaded or does not have a
 * default constructor.
 * <p>
 * The scanner returns all classes contained in the given bundle, including embedded archives and
 * directories from the bundle classpath, and all classes visible from required bundle wires
 * (package imports or required bundles), provided that the exporting bundle is a bean bundle.
 * 
 * @author Harald Wellmann
 * 
 */
public class BeanScanner {

    private Logger log = LoggerFactory.getLogger(BeanScanner.class);

    private Bundle bundle;

    private Set<URL> beanDescriptors;
    private Set<String> beanClasses;

    /**
     * Constructs a bean scanner for the given bundle.
     * @param bundle bundle to be scanned
     */
    public BeanScanner(Bundle bundle) {
        this.bundle = bundle;
        this.beanDescriptors = new HashSet<URL>();
        this.beanClasses = new TreeSet<String>();
    }

    /**
     * Returns the class names of all bean candidate classes.
     * @return unmodifiable set
     */
    public Set<String> getBeanClasses() {
        return Collections.unmodifiableSet(beanClasses);
    }

    /**
     * Return the URLs of all bean descriptors (beans.xml).
     * @return unmodifiable set
     */
    public Set<URL> getBeanDescriptors() {
        return Collections.unmodifiableSet(beanDescriptors);
    }

    /**
     * Scans the given bundle and all imports for bean classes.
     */
    public void scan() {
        scanOwnBundle();
        scanImportedPackages();
        scanRequiredBundles();
        logBeanClasses();
    }

    private void logBeanClasses() {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("candidate bean classes for bundle [{}]:", bundle);
        for (String klass : beanClasses) {
            log.debug("    {}", klass);
        }
    }

    private void scanOwnBundle() {
        String managedBeans = bundle.getHeaders().get(MANAGED_BEANS_KEY);
        if (managedBeans == null) {
            return;
        }

        String[] descriptors = managedBeans.split(",");
        for (String descriptor : descriptors) {
            URL descriptorUrl = bundle.getEntry(descriptor);
            if (descriptorUrl != null) {
                beanDescriptors.add(descriptorUrl);
            }
        }

        if (beanDescriptors.isEmpty()) {
            return;
        }

        String[] classPathElements;

        String bundleClassPath = bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
        if (bundleClassPath == null) {
            classPathElements = new String[] { "/" };
        }
        else {
            classPathElements = bundleClassPath.split(",");
        }

        for (String cp : classPathElements) {
            String classPath = cp;
            if (classPath.equals(".")) {
                classPath = "/";
            }
            Enumeration<URL> e = bundle.findEntries(classPath, "*.class", true);
            while (e.hasMoreElements()) {
                URL url = e.nextElement();
                String klass = toClassName(classPath, url);
                beanClasses.add(klass);
            }
        }
    }

    private String toClassName(String classPath, URL url) {
        return toClassName(classPath, url.getFile());
    }

    private String toClassName(String classPath, String file) {
        String klass = null;
        String[] parts = file.split("!");
        if (parts.length > 1) {
            klass = parts[1];
        }
        else {
            klass = file;
        }
        if (klass.charAt(0) == '/') {
            klass = klass.substring(1);
        }
        
        String prefix = classPath;
        if (classPath.length() > 1) {
            if ( classPath.charAt(0) == '/') {
                prefix = classPath.substring(1);
            }
            assert klass.startsWith(prefix);
            int startIndex = prefix.length();
            if (!prefix.endsWith("/")) {
                startIndex++;
            }
            klass = klass.substring(startIndex);
        }
        
        klass = klass.replace("/", ".").replace(".class", "");
        log.trace("file = {}, class = {}", file, klass);
        return klass;
    }

    private void scanImportedPackages() {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleWire> wires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
        for (BundleWire wire : wires) {
            scanForClasses(wire);
        }
    }

    private void scanForClasses(BundleWire wire) {
        log.debug("scanning wire [{}]", wire);
        BundleWiring wiring = wire.getProviderWiring();
        if (wiring.getBundle().getBundleId() == bundle.getBundleId()) {
            return;
        }
        if (!isBeanBundle(wiring.getBundle())) {
            return;
        }

        Collection<URL> entries = wiring.findEntries("/", "*.class",
            BundleWiring.FINDENTRIES_RECURSE);
        for (URL entry : entries) {
            beanClasses.add(toClassName("/", entry));
        }
    }

    private boolean isBeanBundle(Bundle candidate) {
        return candidate.getHeaders().get(MANAGED_BEANS_KEY) != null;
    }

    private void scanRequiredBundles() {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleWire> wires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
        for (BundleWire wire : wires) {
            scanForClasses(wire);
        }
    }
}