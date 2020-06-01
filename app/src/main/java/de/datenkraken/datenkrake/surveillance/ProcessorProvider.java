package de.datenkraken.datenkrake.surveillance;

import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import de.datenkraken.datenkrake.surveillance.processors.background.GPSLocationProcessor;
import de.datenkraken.datenkrake.surveillance.processors.background.OSInformationProcessor;
import de.datenkraken.datenkrake.surveillance.processors.background.WifiConnectionProcessor;
import de.datenkraken.datenkrake.surveillance.processors.event.ApplicationActionProcessor;
import de.datenkraken.datenkrake.surveillance.processors.event.ArticleActionProcessor;
import de.datenkraken.datenkrake.surveillance.processors.event.LogProcessor;
import de.datenkraken.datenkrake.surveillance.processors.event.SourceActionProcessor;
import de.datenkraken.datenkrake.surveillance.sender.ApplicationActionSender;
import de.datenkraken.datenkrake.surveillance.sender.ArticleActionSender;
import de.datenkraken.datenkrake.surveillance.sender.GPSLocationSender;
import de.datenkraken.datenkrake.surveillance.sender.OSInformationSender;
import de.datenkraken.datenkrake.surveillance.sender.SourceActionSender;
import de.datenkraken.datenkrake.surveillance.sender.UserActivitySender;
import de.datenkraken.datenkrake.surveillance.sender.WifiConnectionSender;

/**
 * Contains methods to acquire {@link IEventProcessor}s, {@link IBackgroundProcessor}s and
 * {@link ISendProcessedData}s.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class ProcessorProvider {

    /**
     * Private constructor so this class doesn't get instantiated and silence PMD.
     */
    private ProcessorProvider() {

    }

    /**
     * Provides all {@link IEventProcessor}s. When a new {@link IEventProcessor} gets added
     * it should be instantiated in this method.
     *
     * @return Array of {@link IEventProcessor}.
     */
    public static IEventProcessor[] getEventProcessors() {
        return new IEventProcessor[] {
            new LogProcessor(),
            new ApplicationActionProcessor(),
            new ArticleActionProcessor(),
            new SourceActionProcessor()
        };
    }

    /**
     * Provides all {@link IBackgroundProcessor}s. When a new {@link IBackgroundProcessor} gets added
     * it should be instantiated in this method.
     *
     * @return Array of {@link IBackgroundProcessor}.
     */
    public static IBackgroundProcessor[] getBackgroundProcessors() {
        return new IBackgroundProcessor[] {
            new WifiConnectionProcessor(),
            new OSInformationProcessor(),
            new GPSLocationProcessor()
        };
    }

    /**
     * Provides all {@link ISendProcessedData}s available from {@link #getEventProcessors()} and
     * {@link #getBackgroundProcessors()}.
     *
     * @return Array of {@link ISendProcessedData}.
     */
    public static ISendProcessedData[] getSendProcessor() {
        return new ISendProcessedData[] {
            new WifiConnectionSender(),
            new ApplicationActionSender(),
            new ArticleActionSender(),
            new SourceActionSender(),
            new OSInformationSender(),
            new UserActivitySender(),
            new GPSLocationSender()
        };
    }
}
