package com.tencent.map.geolocation.iodetector.detector;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.tencent.map.geolocation.iodetector.DetectionProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by toveyliu on 2016/8/26.
 */

public class WifiDetector extends AbstractDetector {
    private static final String TAG = "WifiDetector";
    /**
     * 当最大值大于THRESHOLD_RSSI_INDOOR,标志可能为室内[2个则必为室内]
     */
    private final int THRESHOLD_RSSI_INDOOR = -55;
    /**
     * 当最大值小于THRESHOLD_RSSI_OUTDOOR,标志可能为室内
     */
    private final int THRESHOLD_RSSI_OUTDOOR = -70;

    // 0 ~ -130共12个区间
    private final int MAX_RSSI_RANGE = 13;
    private int[] rssiCount = new int[MAX_RSSI_RANGE];
    private int[] rssiBigCount = new int[MAX_RSSI_RANGE];

    private List<ScanResult> wifiList;
    private WifiInfo connWifiInfo;
    private int[] rssiArr;

    // 执行最近2条Wifi的合并
    private List<ScanResult> previousWifiList = null;
    private long previousWifiTime = 0;

    /**
     * Singleton:static inner class
     */
    private static class SingletonHolder {
        public static final WifiDetector INSTANCE = new WifiDetector();
    }

    private WifiDetector() {
        super();
    }

    public static WifiDetector getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onWifiEvent(List<ScanResult> wifiList) {
        if (!isRunning()) {
            return;
        }
        
        // this.wifiList = new ArrayList<>(wifiList);
        this.wifiList = combineWifiList(wifiList);

        if (wifiList == null || wifiList.size() == 0) {
            mProfile.setConfidence(0.0f, 0.0f, 0.0f);
        } else {
            updateProfile();
        }

        notifyDetecterListener(0);
        notifyDetecterDataDescListener(0);
    }

    /**
     * 结合最近2次的Wifi扫描信息<br/>
     * 若超过20s，则直接丢弃上一条List<br/>
     * 若该mac地址2条List中均有，则信号强度求平均，<br/>
     * 若只有一条有，则直接放最终的List中
     */
    private List<ScanResult> combineWifiList(List<ScanResult> newWifiList) {
        long curTime = System.currentTimeMillis();
        // 1、初次来合并
        // 2、2次Wif间隔10s
        // 3、上次list为null或扫描到的wifi个数为0
        if (previousWifiTime == 0 || previousWifiTime - curTime >= 10000 || previousWifiList == null || previousWifiList.size() == 0) {
            previousWifiTime = curTime;
            previousWifiList = new ArrayList<ScanResult>(newWifiList);
            return previousWifiList;
        }
        // 开始合并，将新值添加到previousWifiList
        List<ScanResult> tmpWifiList = previousWifiList;
        int tmpLength = tmpWifiList.size();
        for (ScanResult newR : newWifiList) {
            int i = 0;
            // 只需要比较前面size个，面会改变tmpWifiList的长度，后面增加的不需要比较
            // 故使用固定的值tmpLength
            for (; i < tmpLength; i++) {
                ScanResult tmpR = tmpWifiList.get(i);
                if (tmpR.BSSID.equals(newR.BSSID)) {
                    tmpR.level = (tmpR.level + newR.level) / 2;
                }
            }
            // 未找到
            if (i == tmpLength) {
                tmpWifiList.add(newR);
            }
        }

        // 重新赋值previousWifiList
        previousWifiList = new ArrayList<ScanResult>(newWifiList);
        previousWifiTime = curTime;
        return tmpWifiList;
    }

    @Override
    public void updateProfile() {
        initArr();
        // 恢复缩放因子
        mProfile.setFcator(1.0f);
        // ① 初始化
        mProfile.setConfidence(0.0f, 0.0f, 0.0f);

        if (connWifiInfo != null) {
            // TODO 判断Wifi连接，如果当前wifi有连接，且信号强度足够大，则可以基本判断为室内（但是移动电源判断有问题）
            // 但是某些地方有大功率wifi，直接覆盖大范围的WLAN，比如校园中间
            // 因此可以放大Wifi检测器的缩放因子
            // Android系统中，只要>-55dBm，就会显示满格，因此可以通过此阈值来进行判断
            //            Log.d(TAG, "updateProfile: " + connWifiInfo.getSSID() + ":" + connWifiInfo.getRssi());
        }

        Collections.sort(wifiList, comparator);
        int countBig55 = 0;
        rssiArr = new int[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {
            rssiArr[i] = wifiList.get(i).level;
            rssiCount[(-rssiArr[i]) / 10]++;
            if (rssiArr[i] >= -55) {
                countBig55++;
            }
        }

        if (countBig55 >= 2) {
            mProfile.setConfidence(1.0f, 0.0f, 0.0f);
        } else {
            wifiDetectorAlgorithm01();
        }
    }

    /**
     * 根据不同的Wifi个数来进行判断
     */
    private void wifiDetectorAlgorithm01() {
        rssiBigCount[0] = rssiCount[0];
        for (int i = 1; i < rssiBigCount.length; i++) {
            rssiBigCount[i] = rssiBigCount[i - 1] + rssiCount[i];
        }

        // 个数>=5时，判断最大值
        // 判断信号强度最大值
        int maxRssi = rssiArr[0];
        if (maxRssi > -40) {
            mProfile.addConfidence(1.0f, 0.0f, 0.0f);
        } else if (maxRssi > -50) {
            mProfile.addConfidence(0.8f, 0.2f, 0.0f);
        } else if (maxRssi > -60) {
            mProfile.addConfidence(0.7f, 0.3f, 0.0f);
        } else if (maxRssi > -70) {
            mProfile.addConfidence(0.6f, 0.4f, 0.0f);
        } else {
            mProfile.addConfidence(0.2f, 0.8f, 0.0f);
        }

        if (rssiBigCount[6] == 0) {// 全部<=-70
            mProfile.addConfidence(0.1f, 0.9f, 0.0f);
        } else {
            if (rssiBigCount[5] == 0) { // [-60,70)之间有值
                if (rssiBigCount[6] >= 4) {
                    mProfile.addConfidence(0.5f, 0.5f, 0.0f);
                } else {
                    mProfile.addConfidence(0.3f, 0.7f, 0.0f);
                }
            } else {
                // TODO 如果有多个大于-60的，需要进一步判断当前状态是否是行车
                if (rssiBigCount[4] == 0) {
                    if (rssiBigCount[5] >= 2) {
                        mProfile.addConfidence(0.7f, 0.3f, 0.0f);
                    } else {
                        mProfile.addConfidence(0.6f, 0.4f, 0.0f); // [-50,60)之间有值
                    }
                } else {
                    if (rssiBigCount[3] == 0) {
                        mProfile.addConfidence(0.8f, 0.2f, 0.0f); // [-40,50)之间有值
                    } else {
                        mProfile.addConfidence(0.9f, 0.1f, 0.0f); // [-30,-40)之间有值
                    }
                }
            }
        }
    }

    @Override
    protected String getDetectorDataDesc() {
        StringBuilder sb = new StringBuilder();
        if (rssiArr == null || rssiArr.length == 0) {
            return "";
        }
        sb.append(rssiArr[0]);
        for (int i = 1; i < rssiArr.length; i++) {
            sb.append(',').append(rssiArr[i]);
        }
        sb.append("$").append(wifiList.size());
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult sResult = wifiList.get(i);
            sb.append("$").append(sResult.level).append(',').append(sResult.BSSID).append(',');
            String ssid = sResult.SSID.replaceAll("[,$ \t\n]", "");
            sb.append(ssid);
        }
        return sb.toString();
    }

    @Override
    public int getDetectorType() {
        return DetectionProfile.TYPE_WIFI;
    }

    public void setConnectWifiInfo(WifiInfo wifiInfo) {
        this.connWifiInfo = wifiInfo;
    }

    private void initArr() {
        for (int i = 0; i < MAX_RSSI_RANGE; i++) {
            rssiCount[i] = 0;
            rssiBigCount[i] = 0;
        }
    }

    private Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            // 按照信号强度，从高到低
            return rhs.level - lhs.level;
        }
    };
}
