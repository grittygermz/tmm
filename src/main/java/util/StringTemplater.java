package util;

import json.JobInput;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class StringTemplater {
    private final String datafileNameTemplate = "${doctype}.${archiveid}.${bookingcenter}..${date}.S${seqnum}.V${versionnum}.U1.D${datanum}.data.pdf";
    private final String folderTemplate = "${doctype}.${archiveid}.${bookingcenter}..${date}.S${seqnum}.V${versionnum}";
    private final String zipTemplate = "${doctype}.${archiveid}.${bookingcenter}..${date}.S${seqnum}.V${versionnum}.zip";
    private final String metafileNameTemplate = "${doctype}.${archiveid}.${bookingcenter}..${date}.S${seqnum}.V${versionnum}.U1.M1.meta.xml";
    private final String auditfileNameTemplate = "${doctype}.${archiveid}.${bookingcenter}..${date}.S${seqnum}.V${versionnum}.audit.xml";
    private final String controlfileNameTemplate = "${doctype}.${archiveid}.${bookingcenter}..${date}.S${seqnum}.V${versionnum}.control";

    Map<String, String> parameters;

    public StringTemplater(JobInput jobInput) {
        parameters = new HashMap<>();
        parameters.put("doctype", jobInput.getDocType());
        parameters.put("archiveid", jobInput.getArchiveId());
        parameters.put("bookingcenter", jobInput.getBookingCenter());
        parameters.put("date", jobInput.getDate());
        parameters.put("versionnum", String.valueOf(jobInput.getVersionNum()));
    }

    public Map<String, String> getParametersMap() {
        return parameters;
    }

    public String getFolderName() {
        StringSubstitutor substitutor = new StringSubstitutor(parameters);
        return substitutor.replace(folderTemplate);
    }

    public String getDatafileName() {
        StringSubstitutor substitutor = new StringSubstitutor(parameters);
        return substitutor.replace(datafileNameTemplate);
    }

    public String getMetafileName() {
        StringSubstitutor substitutor = new StringSubstitutor(parameters);
        return substitutor.replace(metafileNameTemplate);
    }

    public String getAuditfileName() {
        StringSubstitutor substitutor = new StringSubstitutor(parameters);
        return substitutor.replace(auditfileNameTemplate);
    }

    public String getControlfileName() {
        StringSubstitutor substitutor = new StringSubstitutor(parameters);
        return substitutor.replace(controlfileNameTemplate);
    }

    public String getZipName() {
        StringSubstitutor substitutor = new StringSubstitutor(parameters);
        return substitutor.replace(zipTemplate);
    }
}
