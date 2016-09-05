package com.tencent.map.geolocation.iodetector.detector;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 
 * @ClassName: InOutRecognizer 单例初始化 getInstance()
 * @Description: 根据GPS信息识别室内或室外<br>
 *               主要方法：judgeInOut 判断室内室外(综合了绝对判断和相对判断)<br>
 *               getJudgedetail 获取判断信息详情<br>
 *               isRelatively 绝对判断结果<br>
 *               isAbsolutely 相对判断结果<br>
 * @author kejiwang@tencent.com
 * @date 2015-2-9 下午5:20:26
 * 
 */
public class InOutRecognizer {

    ArrayList<Float> mSnrList = new ArrayList<Float>();

    /**
     * 这几个参数应该可以通过训练找到最佳值
     */

    private static final int SNRNumbers = 5;// 需要的卫星数
    private static final float outdoorSingleSnrThreshold = 35;// 单个信噪比 的 室外阈值
    private static final float outdoorAvgSnrThresholdTop = 30;// 平均信噪比 的 室外阈值
    private static final float outdoorAvgSnrThresholdBottom = 22;// 平均信噪比 的 室内阈值

    private float mAvgLast = 0;
    private float mAvgMax = 0;
    private float mAvgMin = 100;

    private boolean mUsefulAbsolutely = false;// 绝对
    private boolean mUsefulRelatively = false;// 相对

    private String mDetail = "null";

    private static final boolean GPS_DEBUG = true;

    private static final String TAG = "InOutRecognizer";

    private static InOutRecognizer instance = null;

    /**
     * 
     * @Title: getInstance
     * @Description:单例
     * @param @return
     * @return InOutRecognizer
     * @throws
     */
    public static InOutRecognizer getInstance() {
        if (instance == null) {
            synchronized (InOutRecognizer.class) {
                if (instance == null) {
                    instance = new InOutRecognizer();
                }
            }
        }
        return instance;
    }

    private InOutRecognizer() {}

    /**
     * @Description: 判断方法： <br>
     *               取前几个较大的信噪比进行判断<br>
     *               信噪比的绝对值判断。 <br>
     *               1 单个信噪比大于某值->室外 35 <br>
     *               2 信噪比平均值大于某值->室外 30 <br>
     *               3 信噪比平均值小于某值->室内 22 <br>
     *               4 中间临界区暂未定义 22~30 <br>
     *               ----------------------------------------- <br>
     *               信噪比的相对判断 (在复杂的环境下可能逻辑上有一定问题) <br>
     *               主要关注————信噪比变化趋势与信噪比的最大最小值 <br>
     * 
     *               1.本次均值比上次均值少2认为信号不稳定或->室内 <br>
     *               2.如果均值大于历史记录的最大最小值的平均值 认为->室外 <br>
     *               3.如果均值小于于历史记录的最大最小值的平均值并小于22（outdoorAvgSnrThresholdBottom） 认为->室内 <br>
     *               ------------------------------------------ <br>
     *               当绝对判断和相对判断冲突时 报告冲突，以相对判断的结果为准 <br>
     *               实验中冲突的产生原因： 绝对判断滞后于相对判断 <br>
     * 
     * @return true->室外 false->室内 <br>
     */
    public boolean judgeInOut(GpsStatus GpsStatus) {
        
        int maxSatellites = GpsStatus.getMaxSatellites();
        // 创建一个迭代器保存所有卫星
        Iterator<GpsSatellite> iters = GpsStatus.getSatellites().iterator();
        int count = 0;

        mSnrList.clear();
        StringBuilder sb = new StringBuilder();
        // 获取所有卫星的信噪比
        while (iters.hasNext() && count <= maxSatellites) {
                GpsSatellite s = iters.next();
                count++;
                mSnrList.add(s.getSnr());
        }
        if (GPS_DEBUG) Log.i(TAG, "搜索到：" + count + "颗卫星");
        sb.append(count + "颗卫星,");


        if (mSnrList.size() < SNRNumbers) {// 卫星数不足情况
        } else {

            float[] snrs = new float[mSnrList.size()];
            for (int i = 0; i < snrs.length; i++) {
                snrs[i] = mSnrList.get(i);
            }
            // 取出较大的信噪比，取前SNRNumbers个
            Arrays.sort(snrs);
            float[] snrUse = new float[SNRNumbers];// 选最大的几个使用
            float avg = 0;// 信噪比均值
            for (int j = 0; j < SNRNumbers; j++) {
                snrUse[j] = snrs[snrs.length - 1 - j];
                avg += snrUse[j];
            }
            // 求信噪比均值
            avg = avg / SNRNumbers;
            sb.append("\n");

            /**
             * 信噪比 绝对值判断。 1 单个信噪比大于某值->室外 35 <br>
             * 2 信噪比平均值大于某值->室外 30 <br>
             * 3 信噪比平均值小于某值->室内 22 <br>
             * 4 中间临界区暂未定义 22~30
             */
            sb.append("绝对判断：");
            if (snrUse[0] > outdoorSingleSnrThreshold) {
                if (GPS_DEBUG) Log.i(TAG, "室外");
                mUsefulAbsolutely = true;
                sb.append("室外|");
            } else if (avg > outdoorAvgSnrThresholdTop) {
                mUsefulAbsolutely = true;
                sb.append("室外|");
            }
            if (avg < outdoorAvgSnrThresholdBottom) {
                sb.append("室内|");
                mUsefulAbsolutely = false;
            }


            /**
             * 信噪比的相对判断 (在复杂的环境下可能逻辑上有一定问题)<br>
             * 主要关注——————信噪比变化趋势与信噪比的最大最小值 信噪比均值<br>
             * 历史记录的的最大和最小值 avgMax 和 avgMin 上一次的信噪比均值 avgLast <br>
             * 1.本次均值比上次均值少2 认为信号不稳定或->室内 <br>
             * 2.如果均值大于历史记录的最大最小值的平均值 认为->室外 <br>
             * 3.如果均值小于于历史记录的最大最小值的平均值 并小于22（outdoorAvgSnrThresholdBottom） 认为->室内
             */

            sb.append("avg" + avg);
            sb.append("avg'" + (avg - mAvgLast));
            sb.append("avgMax" + mAvgMax);
            sb.append("avgMin" + mAvgMin);
            sb.append(mUsefulAbsolutely);
            sb.append("\n");

            if (mAvgMax < avg) {
                mAvgMax = avg;
            }
            if (mAvgMin > avg) {
                mAvgMin = avg;
            }
            mAvgLast = avg;

            sb.append("相对判断：");
            if (avg - mAvgLast > 3) {
                sb.append("信号增强");
            }

            if ((mAvgLast - avg) > 2) {
                sb.append("信号衰弱");
                mUsefulRelatively = false;// ->室内
            }
            if (avg > (mAvgMax + mAvgMin) / 2) {
                mUsefulRelatively = true;// ->室外

            } else {
                if (avg < outdoorAvgSnrThresholdBottom) {
                    mUsefulRelatively = false;// ->室内
                }
            }

            /**
             * 当绝对判断和相对判断冲突时 报告冲突，以相对判断的结果为准 实验中冲突的产生原因： 绝对判断滞后于相对判断
             */
            if (mUsefulAbsolutely != mUsefulRelatively) {
                sb.append("\n冲突" + mUsefulAbsolutely + "|" + mUsefulRelatively);
            }
            sb.append("\n最终结果" + mUsefulRelatively);

        }
        mDetail = sb.toString();
        return mUsefulRelatively;
    }

    /**
     * @Title: getJudgeInfo
     * @Description: 判断信息详情
     */
    public String getJudgedetail() {
        return mDetail;
    }

    /**
     * @Title: isConflict
     * @Description:判断是否两种方法结果冲突
     */
    public boolean isConflict() {
        return mUsefulAbsolutely != mUsefulRelatively;
    }

    /**
     * 
     * @Title: isRelatively
     * @Description:相对判断结果
     */
    public boolean isRelatively() {
        return mUsefulRelatively;
    }

    /**
     * @Title: isAbsolutely
     * @Description: 绝对判断结果
     */
    public boolean isAbsolutely() {
        return mUsefulAbsolutely;
    }
}
