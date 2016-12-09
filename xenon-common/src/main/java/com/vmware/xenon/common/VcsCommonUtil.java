package com.vmware.xenon.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VcsCommonUtil {

    /**
     * 
     * @param cmd 
     *  e.g. private static final String CMD = "sh /tmp/scripts/get_datastores.sh";
     * @return
     * @throws IOException
     */
    public static String readBashScript(String cmd) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        Process ps = Runtime.getRuntime().exec(cmd);
        InputStream is = ps.getInputStream();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
