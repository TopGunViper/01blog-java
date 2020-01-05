package edu.ouc.ds.highavailability.utils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;

public class EtcdUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdUtils.class);

    public static void main(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption("h")) {
                new HelpFormatter().printHelp("java -e <endpoints> -k <key> -m <parallel>", options);
                return;
            }

            final String[] endpointsArr = cmdLine.getOptionValues("e");
            final String keyStr = cmdLine.getOptionValue("k");

            ByteSequence key = ByteSequence.from(keyStr, StandardCharsets.UTF_8);
            Collection<URI> endpoints = Util.toURIs(Arrays.asList(endpointsArr));

            CountDownLatch latch = new CountDownLatch(1);
            Watch.Listener listener = Watch.listener(response -> {
                LOGGER.info("Watching for key={}", keyStr);

                for (WatchEvent event : response.getEvents()) {
                    LOGGER.info("type={}, key={}, value={}",
                            event.getEventType(),
                            Optional.ofNullable(event.getKeyValue().getKey())
                                    .map(bs -> bs.toString(StandardCharsets.UTF_8))
                                    .orElse(""),
                            Optional.ofNullable(event.getKeyValue().getValue())
                                    .map(bs -> bs.toString(StandardCharsets.UTF_8))
                                    .orElse("")
                    );
                }
                latch.countDown();
            });

            try (Client client = Client.builder().endpoints(endpointsArr).build()) {
                Watch watch = client.getWatchClient();
                watch.watch(key, listener);

                latch.await();
            } catch (Exception e) {
                LOGGER.error("Watching Error {}", e);
                System.exit(1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Options buildOptions() {
        final Options options = new Options();
        final Option input = new Option("e", "--endpoints", true, "the etcd endpoints");
        options.addOption(input);
        final Option output = new Option("k", "--key", true, "the key to watch");
        output.setRequired(true);
        options.addOption(output);

        final Option helpOption = new Option("h", "--help", false, "Help");
        options.addOption(helpOption);
        return options;
    }

}
