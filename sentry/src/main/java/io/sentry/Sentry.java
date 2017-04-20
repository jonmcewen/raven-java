package io.sentry;

import io.sentry.event.Breadcrumb;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.User;

/**
 * Sentry provides easy access to a statically stored {@link SentryClient} instance.
 */
public final class Sentry {
    /**
     * The most recently constructed Sentry instance, used by static helper
     * methods like {@link Sentry#capture(Event)}.
     */
    private static volatile SentryClient storedClient = null;

    /**
     * Hide constructor.
     */
    private Sentry() {

    }

    /**
     * Returns the last statically stored Sentry instance or null if one has
     * never been stored.
     *
     * @return statically stored {@link SentryClient} instance
     */
    public static SentryClient getStoredClient() {
        return storedClient;
    }

    public static void setStoredClient(SentryClient storedClient) {
        Sentry.storedClient = storedClient;
    }

    private static void verifyStoredClient() {
        if (storedClient == null) {
            throw new NullPointerException("No stored Sentry instance is available to use."
                + " You must construct a Sentry instance before using the static Sentry methods.");
        }
    }

    /**
     * Send an Event using the statically stored Sentry instance.
     *
     * @param event Event to send to the Sentry server
     */
    public static void capture(Event event) {
        verifyStoredClient();
        getStoredClient().sendEvent(event);
    }

    /**
     * Sends an exception (or throwable) to the Sentry server using the statically stored Sentry instance.
     * <p>
     * The exception will be logged at the {@link Event.Level#ERROR} level.
     *
     * @param throwable exception to send to Sentry.
     */
    public static void capture(Throwable throwable) {
        verifyStoredClient();
        getStoredClient().sendException(throwable);
    }

    /**
     * Sends a message to the Sentry server using the statically stored Sentry instance.
     * <p>
     * The message will be logged at the {@link Event.Level#INFO} level.
     *
     * @param message message to send to Sentry.
     */
    public static void capture(String message) {
        verifyStoredClient();
        getStoredClient().sendMessage(message);
    }

    /**
     * Builds and sends an {@link Event} to the Sentry server using the statically stored Sentry instance.
     *
     * @param eventBuilder {@link EventBuilder} to send to Sentry.
     */
    public static void capture(EventBuilder eventBuilder) {
        verifyStoredClient();
        getStoredClient().sendEvent(eventBuilder);
    }

    /**
     * Record a {@link Breadcrumb}.
     *
     * @param breadcrumb Breadcrumb to record
     */
    public static void record(Breadcrumb breadcrumb) {
        verifyStoredClient();
        getStoredClient().getContext().recordBreadcrumb(breadcrumb);
    }

    /**
     * Set the {@link User} in the current context.
     *
     * @param user User to store.
     */
    public static void setUser(User user) {
        verifyStoredClient();
        getStoredClient().getContext().setUser(user);
    }

    /**
     * Clears the current context.
     */
    public static void clearContext() {
        verifyStoredClient();
        getStoredClient().getContext().clear();
    }


}
