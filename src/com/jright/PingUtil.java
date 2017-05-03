package com.jright;

import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

public class PingUtil {

    private static final String TAG = PingUtil.class.getSimpleName();
    private static final String PING_FAIL_RESULT = "-1";
    private static ArrayList<String> ipPingResults = new ArrayList<>();

    /**
     * Get ip from given url
     *
     * @param url url needs to ping
     * @return url's IP address such as 192.168.0.1
     */
    public static String getIPFromUrl(String url) {
        String domain = getDomain(url);
        if (null == domain) {
            return null;
        }
        ping(createSimplePingCommand(1, 100, domain), new OnPingResult() {
            @Override
            public String onPingSuccess(String pingResult) {
                if (null != pingResult) {
                    try {
                        String tempInfo = pingResult.substring(pingResult.indexOf("("));
                        String ip = tempInfo.substring(1, tempInfo.indexOf(")"));
                        return ip;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return PING_FAIL_RESULT;
            }

            @Override
            public String onPingFailure() {
                return PING_FAIL_RESULT;
            }
        });
        return PING_FAIL_RESULT;
    }

    /**
     * ping once for the given url,returns ttl of this ping action
     * @param url
     * @return
     */
    public static String getTTL(String url) {
        String domain = getDomain(url);
        if (null == domain) {
            return PING_FAIL_RESULT;
        }
        ping(createSimplePingCommand(1, 100, domain), new OnPingResult() {
            @Override
            public String onPingSuccess(String pingResult) {
                if (null != pingResult) {
                    try {
                        String tempInfo = pingResult.substring(pingResult.indexOf("ttl="));
//                        LogUtil.d(TAG, "getTTL tempInfo: " + tempInfo);
                        String s = String.valueOf(tempInfo.subSequence(tempInfo.indexOf("=") + 1, tempInfo.indexOf(" ")));
                        return s;
                    } catch (Exception e) {
                    }
                }
                return PING_FAIL_RESULT;
            }

            @Override
            public String onPingFailure() {
                return PING_FAIL_RESULT;
            }
        });
        return PING_FAIL_RESULT;
    }

    /**
     * get time used in ping
     * @param url
     * @return
     */
    public static String getElapseTime(String url) {
        String domain = getDomain(url);
        if (null == domain) {
            return null;
        }
        ping(createSimplePingCommand(1, 100, domain), new OnPingResult() {
            @Override
            public String onPingSuccess(String pingResult) {
                if (null != pingResult) {
                    try {
                        String tempInfo = pingResult.substring(pingResult.indexOf("time="));
//                        LogUtil.d(TAG, "getElapsedTime tempInfo: " + tempInfo);
                        String s = String.valueOf(tempInfo.subSequence(tempInfo.indexOf("=") + 1, tempInfo.indexOf(" ")));
                        return s;
                    } catch (Exception e) {
                    }
                }
                return PING_FAIL_RESULT;
            }

            @Override
            public String onPingFailure() {
                return PING_FAIL_RESULT;
            }
        });
        return PING_FAIL_RESULT;
    }

    public static int getMaxTTL(String pingResult) {
        ArrayList<Integer> ttlList = getPingDataListWithKey(pingResult, "ttl");
        if (ttlList.isEmpty()) {
            return Integer.valueOf(PING_FAIL_RESULT);
        } else {
            return Collections.max(ttlList);
        }
    }

    public static int getMinTTL(String pingResult) {
        ArrayList<Integer> ttlList = getPingDataListWithKey(pingResult, "ttl");
        if (ttlList.isEmpty()) {
            return Integer.valueOf(PING_FAIL_RESULT);
        } else {
            return Collections.min(ttlList);
        }
    }

    public static int getAveTTL(String pingResult) {
        ArrayList<Integer> ttlList = getPingDataListWithKey(pingResult, "ttl");
        if (ttlList.isEmpty()) {
            return Integer.valueOf(PING_FAIL_RESULT);
        } else {
            int totalTTL = 0;
            for (int ttl : ttlList) {
                totalTTL += Integer.valueOf(ttl);
            }
            return Integer.valueOf(totalTTL / ttlList.size());
        }
    }

    public static ArrayList<Integer> getPingDataListWithKey(String pingResult, String key) {
        String[] split = pingResult.split("\n");
        ArrayList<Integer> dataList = new ArrayList<>();
        for (String s : split) {
            if (s.contains(key)) {
                String keyString = s.substring(s.indexOf(key) + key.length() + 1, s.length());//key.length()+"="
                keyString = keyString.substring(0, keyString.indexOf(" "));
                dataList.add(Integer.valueOf(keyString));
            }
        }
        return dataList;
    }

    public static String getMaxElapseTime(String resultString) {
        if (null != resultString && resultString.length() != 0) {
            String tempInfo = resultString.substring(resultString.indexOf("min/avg/max/mdev") + 19);
            String[] temps = tempInfo.split("/");
            return String.valueOf(Float.valueOf(temps[2]));
        }
        return PING_FAIL_RESULT;
    }

    public static String getMinElapseTime(String resultString) {
        if (null != resultString && resultString.length() != 0) {
            String tempInfo = resultString.substring(resultString.indexOf("min/avg/max/mdev") + 19);
            String[] temps = tempInfo.split("/");
            return String.valueOf(Float.valueOf(temps[0]));
        }
        return PING_FAIL_RESULT;
    }

    public static String getAveElapseTime(String resultString) {
        if (null != resultString && resultString.length() != 0) {
            String tempInfo = resultString.substring(resultString.indexOf("min/avg/max/mdev") + 19);
            String[] temps = tempInfo.split("/");
            return String.valueOf(Float.valueOf(temps[1]));
        }
        return PING_FAIL_RESULT;
    }

    public static String getServerIP(String pingResult) {
        if (null != pingResult && pingResult.length() != 0) {
            String tempInfo = pingResult.substring(pingResult.indexOf("PING") + 5);
            String ip = tempInfo.substring(0, tempInfo.indexOf(" "));
            return ip;
        } else {
            return PING_FAIL_RESULT;
        }
    }

    public static String getPingTimes(String pingResult) {
        if (null != pingResult && pingResult.length() != 0) {
            String[] split = pingResult.split("\n");
            String info = "";
            for (String s : split) {
                if (s.contains("packets transmitted")) {
                    info = s;
                }
            }
            if (info.length() != 0) {
                info = info.substring(0, info.indexOf(" "));
                return info;
            } else {
                return PING_FAIL_RESULT;
            }

        } else {
            return PING_FAIL_RESULT;
        }
    }

    public static String getReceivedPackage(String pingResult) {
        if (null != pingResult && pingResult.length() != 0) {
            String[] split = pingResult.split("\n");
            String info = "";
            for (String s : split) {
                if (s.contains("packets transmitted")) {
                    info = s;
                }
            }
            if (info.length() != 0) {
                info = info.substring(info.indexOf(",") + 2, info.indexOf("received") - 1);
                return info;
            } else {
                return PING_FAIL_RESULT;
            }
        } else {
            return PING_FAIL_RESULT;
        }
    }

    public static String getPacketLoss(String pingResult) {
        if (null != pingResult) {
            try {
                String tempInfo = pingResult.substring(pingResult.indexOf("received,"));
                return tempInfo.substring(9, tempInfo.indexOf("packet"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return PING_FAIL_RESULT;
    }

    public static String getPacketLoss(String ip, int count, int timeout) {
        ping(createSimplePingCommand(count, timeout, ip), new OnPingResult() {
            @Override
            public String onPingSuccess(String pingResult) {
                if (null != pingResult) {
                    try {
                        String tempInfo = pingResult.substring(pingResult.indexOf("received,"));
                        return tempInfo.substring(9, tempInfo.indexOf("packet"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return PING_FAIL_RESULT;
            }

            @Override
            public String onPingFailure() {
                return PING_FAIL_RESULT;
            }
        });
        return PING_FAIL_RESULT;
    }

    private static String getDomain(String url) {
        String domain = null;
        try {
            domain = URI.create(url).getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domain;
    }

    // No regex skills.
//    private static boolean isMatch(String regex, String string) {
//        return Pattern.matches(regex, string);
//    }

    private static void ping(final String command, final OnPingResult onPingResultListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(command);
                    InputStream is = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while (null != (line = reader.readLine())) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    reader.close();
                    is.close();
                    onPingResultListener.onPingSuccess(sb.toString());
                } catch (IOException e) {
                    onPingResultListener.onPingFailure();
                    e.printStackTrace();
                } finally {
                    if (null != process) {
                        process.destroy();
                    }
                }
            }
        }).run();
    }

    private static String createSimplePingCommand(int count, int timeout, String domain) {
        return "/system/bin/ping -c " + count + " -w " + timeout + " " + domain;
    }

    public interface OnPingResult {

        String onPingSuccess(String pingResult);

        String onPingFailure();
    }
}
