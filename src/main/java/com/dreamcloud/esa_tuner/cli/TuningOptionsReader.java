package com.dreamcloud.esa_tuner.cli;

import com.dreamcloud.esa_tuner.TuningOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TuningOptionsReader {
    private static String WINDOW_SIZE_START = "ws-start";
    private static String WINDOW_SIZE_END = "ws-end";
    private static String WINDOW_SIZE_STEP = "ws-step";
    private static String WINDOW_DROP_START = "wd-start";
    private static String WINDOW_DROP_END = "wd-end";
    private static String WINDOW_DROP_STEP = "wd-step";
    private static String VECTOR_LIMIT_START = "vl-start";
    private static String VECTOR_LIMIT_END = "vl-end";
    private static String VECTOR_LIMIT_STEP = "vl-step";
    private static String TUNE_TYPE = "tune-type";

    public void addOptions(Options options) {
        String[] optionNames = new String[]{
            WINDOW_SIZE_START,
            WINDOW_SIZE_END,
            WINDOW_SIZE_STEP,
            WINDOW_DROP_START,
            WINDOW_DROP_END,
            WINDOW_DROP_STEP,
            VECTOR_LIMIT_START,
            VECTOR_LIMIT_END,
            VECTOR_LIMIT_STEP,
            TUNE_TYPE
        };
        for (String optionName: optionNames) {
            Option option = new Option(null, optionName, true, "");
            option.setRequired(false);
            options.addOption(option);
        }
    }

    public TuningOptions getOptions(CommandLine cli) {
        TuningOptions options = new TuningOptions();
        if (cli.hasOption(TUNE_TYPE)) {
            options.setType(cli.getOptionValue(TUNE_TYPE));
        }

        //Window size
        if (cli.hasOption(WINDOW_SIZE_START)) {
            options.setStartingWindowSize(Integer.parseInt(cli.getOptionValue(WINDOW_SIZE_START)));
        }
        if (cli.hasOption(WINDOW_SIZE_END)) {
            options.setEndingWindowSize(Integer.parseInt(cli.getOptionValue(WINDOW_SIZE_END)));
        }
        if (cli.hasOption(WINDOW_SIZE_STEP)) {
            options.setWindowSizeStep(Integer.parseInt(cli.getOptionValue(WINDOW_SIZE_STEP)));
        }

        //Window drop
        if (cli.hasOption(WINDOW_DROP_START)) {
            options.setStartingWindowDrop(Integer.parseInt(cli.getOptionValue(WINDOW_DROP_START)));
        }
        if (cli.hasOption(WINDOW_DROP_END)) {
            options.setEndingWindowDrop(Integer.parseInt(cli.getOptionValue(WINDOW_DROP_END)));
        }
        if (cli.hasOption(WINDOW_DROP_START)) {
            options.setWindowDropStep(Integer.parseInt(cli.getOptionValue(WINDOW_DROP_START)));
        }

        //Vector limit
        if (cli.hasOption(VECTOR_LIMIT_START)) {
            options.setStartingVectorLimit(Integer.parseInt(cli.getOptionValue(VECTOR_LIMIT_START)));
        }
        if (cli.hasOption(VECTOR_LIMIT_END)) {
            options.setEndingVectorLimit(Integer.parseInt(cli.getOptionValue(VECTOR_LIMIT_END)));
        }
        if (cli.hasOption(VECTOR_LIMIT_STEP)) {
            options.setVectorLimitStep(Integer.parseInt(cli.getOptionValue(VECTOR_LIMIT_STEP)));
        }
        return options;
    }
}
