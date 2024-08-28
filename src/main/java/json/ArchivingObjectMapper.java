package json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ArchivingObjectMapper {

    private static final ObjectMapper instance = new ObjectMapper();

    private ArchivingObjectMapper() {
    }

    public static ObjectMapper getInstance() {
        return instance;
    }

}
