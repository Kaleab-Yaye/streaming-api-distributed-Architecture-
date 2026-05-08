package com.adnakiwoch.platform.streaming_api.dto.request.hook.tusd;

import java.util.ArrayList;
import java.util.HashMap;

public record TusdHTTPRequest(HashMap<String, ArrayList<String>> Header) {}
