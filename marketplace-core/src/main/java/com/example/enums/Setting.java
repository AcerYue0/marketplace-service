package com.example.enums;

import java.io.File;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Setting {
    public static final int ZERO = 0;
    public static final int MAX_RETRY = 5;
    public static final int PAGE_SIZE = 1000;
    public static final int FETCH_INTERVAL = 4000;

    public static final String API_URL = "https://msu.io/marketplace/api/marketplace/explore/items";

    public static final String CLASSIFICATION_FILE = "keyword_classification.json";
    public static final File OUTPUT_FILE = Paths.get(System.getProperty("user.dir"), "var", "data", "marketplace_result.json").toFile();

    public static final Logger GLOBAL_LOGGER = LoggerFactory.getLogger(Setting.class);
}
