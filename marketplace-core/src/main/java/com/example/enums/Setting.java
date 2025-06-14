package com.example.enums;

import java.io.File;
import java.nio.file.Paths;

public class Setting {
    public static final int ZERO = 0;
    public static final int PAGE_SIZE = 1000;

    public static final String API_URL = "https://msu.io/marketplace/api/marketplace/explore/items";

    public static final File CLASSIFICATION_FILE = Paths.get("keyword_classification.json").toFile();
    public static final File OUTPUT_FILE = Paths.get("marketplace_result.json").toFile();
}
