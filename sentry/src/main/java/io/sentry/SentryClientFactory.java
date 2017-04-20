package io.sentry;

import io.sentry.dsn.Dsn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Factory in charge of creating {@link SentryClient} instances.
 * <p>
 * The factories register themselves through the {@link ServiceLoader} system.
 */
public abstract class SentryClientFactory {
    private static final ServiceLoader<SentryClientFactory> AUTO_REGISTERED_FACTORIES =
        ServiceLoader.load(SentryClientFactory.class, SentryClientFactory.class.getClassLoader());
    private static final Set<SentryClientFactory> MANUALLY_REGISTERED_FACTORIES = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(SentryClientFactory.class);

    /**
     * Manually adds a SentryFactory to the system.
     * <p>
     * Usually SentryFactories are automatically detected with the {@link ServiceLoader} system, but some systems
     * such as Android do not provide a fully working ServiceLoader.<br>
     * If the factory isn't detected automatically, it's possible to add it through this method.
     *
     * @param sentryClientFactory sentryFactory to support.
     */
    public static void registerFactory(SentryClientFactory sentryClientFactory) {
        MANUALLY_REGISTERED_FACTORIES.add(sentryClientFactory);
    }

    private static Iterable<SentryClientFactory> getRegisteredFactories() {
        List<SentryClientFactory> sentryFactories = new LinkedList<>();
        sentryFactories.addAll(MANUALLY_REGISTERED_FACTORIES);
        for (SentryClientFactory autoRegisteredFactory : AUTO_REGISTERED_FACTORIES) {
            sentryFactories.add(autoRegisteredFactory);
        }
        return sentryFactories;
    }

    /**
     * Creates an instance of Sentry using the DSN obtain through {@link io.sentry.dsn.Dsn#dsnLookup()}.
     *
     * @return an instance of Sentry.
     */
    public static SentryClient sentryInstance() {
        return sentryInstance(Dsn.dsnLookup());
    }

    /**
     * Creates an instance of Sentry using the provided DSN.
     *
     * @param dsn Data Source Name of the Sentry server.
     * @return an instance of Sentry.
     */
    public static SentryClient sentryInstance(String dsn) {
        return sentryInstance(new Dsn(dsn));
    }

    /**
     * Creates an instance of Sentry using the provided DSN.
     *
     * @param dsn Data Source Name of the Sentry server.
     * @return an instance of Sentry.
     */
    public static SentryClient sentryInstance(Dsn dsn) {
        return sentryInstance(dsn, null);
    }

    /**
     * Creates an instance of Sentry using the provided DSN and the specified factory.
     *
     * @param dsn              Data Source Name of the Sentry server.
     * @param sentryFactoryName name of the SentryFactory to use to generate an instance of Sentry.
     * @return an instance of Sentry.
     * @throws IllegalStateException when no instance of Sentry has been created.
     */
    public static SentryClient sentryInstance(Dsn dsn, String sentryFactoryName) {
        logger.debug("Attempting to find a working SentryFactory");

        // Loop through registered factories, keeping track of which classes we skip, which we try to instantiate,
        // and the last exception thrown.
        ArrayList<String> skippedFactories = new ArrayList<>();
        ArrayList<String> triedFactories = new ArrayList<>();
        RuntimeException lastExc = null;

        for (SentryClientFactory sentryClientFactory : getRegisteredFactories()) {
            String name = sentryClientFactory.getClass().getName();
            if (sentryFactoryName != null && !sentryFactoryName.equals(name)) {
                skippedFactories.add(name);
                continue;
            }

            logger.debug("Attempting to use '{}' as a SentryFactory.", sentryClientFactory);
            triedFactories.add(name);
            try {
                SentryClient sentryClientInstance = sentryClientFactory.createSentryInstance(dsn);
                logger.debug("The SentryFactory '{}' created an instance of Sentry.", sentryClientFactory);
                return sentryClientInstance;
            } catch (RuntimeException e) {
                lastExc = e;
                logger.debug("The SentryFactory '{}' couldn't create an instance of Sentry.", sentryClientFactory, e);
            }
        }

        if (sentryFactoryName != null && triedFactories.isEmpty()) {
            try {
                // see if the provided class exists on the classpath at all
                Class.forName(sentryFactoryName);
                logger.error(
                    "The SentryFactory class '{}' was found on your classpath but was not "
                    + "registered with Sentry, see: "
                    + "https://github.com/getsentry/sentry-java/#custom-sentryfactory", sentryFactoryName);
            } catch (ClassNotFoundException e) {
                logger.error("The SentryFactory class name '{}' was specified but "
                    + "the class was not found on your classpath.", sentryFactoryName);
            }
        }

        // Throw an IllegalStateException that attempts to be helpful.
        StringBuilder sb = new StringBuilder();
        sb.append("Couldn't create a sentry instance for: '");
        sb.append(dsn);
        sb.append('\'');
        if (sentryFactoryName != null) {
            sb.append("; sentryFactoryName: ");
            sb.append(sentryFactoryName);

            if (skippedFactories.isEmpty()) {
                sb.append("; no skipped factories");
            } else {
                sb.append("; skipped factories: ");
                String delim = "";
                for (String skippedFactory : skippedFactories) {
                    sb.append(delim);
                    sb.append(skippedFactory);
                    delim = ", ";
                }
            }
        }

        if (triedFactories.isEmpty()) {
            sb.append("; no factories tried!");
            throw new IllegalStateException(sb.toString());
        }

        sb.append("; tried factories: ");
        String delim = "";
        for (String triedFactory : triedFactories) {
            sb.append(delim);
            sb.append(triedFactory);
            delim = ", ";
        }

        sb.append("; cause contains exception thrown by the last factory tried.");
        throw new IllegalStateException(sb.toString(), lastExc);
    }

    /**
     * Creates an instance of Sentry given a DSN.
     *
     * @param dsn Data Source Name of the Sentry server.
     * @return an instance of Sentry.
     * @throws RuntimeException when an instance couldn't be created.
     */
    public abstract SentryClient createSentryInstance(Dsn dsn);

    @Override
    public String toString() {
        return "SentryFactory{"
                + "name='" + this.getClass().getName() + '\''
                + '}';
    }
}
