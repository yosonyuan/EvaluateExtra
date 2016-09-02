package com.soso.evaextra.pdr;

/**
 * Created by mot on 7/17/15.
 */
public class Const {
    public final static double RITS_CC_LATITUDE = 34.97977501;
    public final static double RITS_CC_LONGITUDE = 135.96373915;
    public final static double OSAKA_STATION_LATITUDE = 34.70046472;
    public final static double OSAKA_STATION_LONGITUDE = 135.49728256;
    public final static double DEFAULT_STEP_LENGTH = 75.0;

    public final static double TEM_CM_LONGITUDE_X = 0.00000089831529; //緯度経度0,0付近でのみ有効
    public final static double TEN_CM_LATITUDE_Y = 0.000000904369484; //緯度経度0,0付近でのみ有効

    public final static double PDR_CHALLENGE_INITIAL_POSITION_LONGITUDE_X = 0;
    public final static double PDR_CHALLENGE_INITIAL_POSITION_LATITUDE_Y = 0;
    public final static double PDR_CHALLENGE_INITIAL_POSITION_ALTITUDE_Z = 0;
    public final static double PDR_CHALLENGE_INITIAL_DIRECTION_LONGITUDE_X = TEM_CM_LONGITUDE_X;
    public final static double PDR_CHALLENGE_INITIAL_DIRECTION_LATITUDE_Y = 0;
    public final static double PDR_CHALLENGE_INITIAL_DIRECTION_ALTITUDE_Z = 0;

    public final static double PDR_CHALLENGE_INITIAL_RELATIVE_POSITION_X = 0;
    public final static double PDR_CHALLENGE_INITIAL_RELATIVE_POSITION_Y = 0;
    public final static double PDR_CHALLENGE_INITIAL_RELATIVE_POSITION_Z = 0;
    public final static double PDR_CHALLENGE_INITIAL_RELATIVE_DIRECTION_X = 1;
    public final static double PDR_CHALLENGE_INITIAL_RELATIVE_DIRECTION_Y = 0;
    public final static double PDR_CHALLENGE_INITIAL_RELATIVE_DIRECTION_Z = 0;

    public final static int GFO_NODE_1_RELATIVE_X = 30;
    public final static int GFO_NODE_1_RELATIVE_Y = 353;
    public final static int GFO_NODE_1_RELATIVE_Z = 49;
    public final static double GFO_NODE_1_LONGITUDE = GFO_NODE_1_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_1_LATITUDE = GFO_NODE_1_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_1_ALTITUDE = GFO_NODE_1_RELATIVE_Z;

    public final static int GFO_NODE_2_RELATIVE_X = 175;
    public final static int GFO_NODE_2_RELATIVE_Y = 25;
    public final static int GFO_NODE_2_RELATIVE_Z = 49;
    public final static double GFO_NODE_2_LONGITUDE = GFO_NODE_2_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_2_LATITUDE = GFO_NODE_2_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_2_ALTITUDE = GFO_NODE_2_RELATIVE_Z;

    public final static int GFO_NODE_3_RELATIVE_X = 175;
    public final static int GFO_NODE_3_RELATIVE_Y = 205;
    public final static int GFO_NODE_3_RELATIVE_Z = 49;
    public final static double GFO_NODE_3_LONGITUDE = GFO_NODE_3_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_3_LATITUDE = GFO_NODE_3_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_3_ALTITUDE = GFO_NODE_3_RELATIVE_Z;

    public final static int GFO_NODE_4_RELATIVE_X = 175;
    public final static int GFO_NODE_4_RELATIVE_Y = 285;
    public final static int GFO_NODE_4_RELATIVE_Z = 49;
    public final static double GFO_NODE_4_LONGITUDE = GFO_NODE_4_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_4_LATITUDE = GFO_NODE_4_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_4_ALTITUDE = GFO_NODE_4_RELATIVE_Z;

    public final static int GFO_NODE_5_RELATIVE_X = 175;
    public final static int GFO_NODE_5_RELATIVE_Y = 353;
    public final static int GFO_NODE_5_RELATIVE_Z = 49;
    public final static double GFO_NODE_5_LONGITUDE = GFO_NODE_5_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_5_LATITUDE = GFO_NODE_5_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_5_ALTITUDE = GFO_NODE_5_RELATIVE_Z;

    public final static int GFO_NODE_6_RELATIVE_X = 175;
    public final static int GFO_NODE_6_RELATIVE_Y = 425;
    public final static int GFO_NODE_6_RELATIVE_Z = 49;
    public final static double GFO_NODE_6_LONGITUDE = GFO_NODE_6_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_6_LATITUDE = GFO_NODE_6_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_6_ALTITUDE = GFO_NODE_6_RELATIVE_Z;

    public final static int GFO_NODE_7_RELATIVE_X = 350;
    public final static int GFO_NODE_7_RELATIVE_Y = 25;
    public final static int GFO_NODE_7_RELATIVE_Z = 49;
    public final static double GFO_NODE_7_LONGITUDE = GFO_NODE_7_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_7_LATITUDE = GFO_NODE_7_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_7_ALTITUDE = GFO_NODE_7_RELATIVE_Z;

    public final static int GFO_NODE_8_RELATIVE_X = 350;
    public final static int GFO_NODE_8_RELATIVE_Y = 205;
    public final static int GFO_NODE_8_RELATIVE_Z = 49;
    public final static double GFO_NODE_8_LONGITUDE = GFO_NODE_8_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_8_LATITUDE = GFO_NODE_8_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_8_ALTITUDE = GFO_NODE_8_RELATIVE_Z;

    public final static int GFO_NODE_9_RELATIVE_X = 350;
    public final static int GFO_NODE_9_RELATIVE_Y = 353;
    public final static int GFO_NODE_9_RELATIVE_Z = 49;
    public final static double GFO_NODE_9_LONGITUDE = GFO_NODE_9_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_9_LATITUDE = GFO_NODE_9_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_9_ALTITUDE = GFO_NODE_9_RELATIVE_Z;

    public final static int GFO_NODE_10_RELATIVE_X = 350;
    public final static int GFO_NODE_10_RELATIVE_Y = 425;
    public final static int GFO_NODE_10_RELATIVE_Z = 49;
    public final static double GFO_NODE_10_LONGITUDE = GFO_NODE_10_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_10_LATITUDE = GFO_NODE_10_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_10_ALTITUDE = GFO_NODE_10_RELATIVE_Z;

    public final static int GFO_NODE_11_RELATIVE_X = 445;
    public final static int GFO_NODE_11_RELATIVE_Y = 205;
    public final static int GFO_NODE_11_RELATIVE_Z = 49;
    public final static double GFO_NODE_11_LONGITUDE = GFO_NODE_11_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_11_LATITUDE = GFO_NODE_11_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_11_ALTITUDE = GFO_NODE_11_RELATIVE_Z;

    public final static int GFO_NODE_21_RELATIVE_X = 30;
    public final static int GFO_NODE_21_RELATIVE_Y = 285;
    public final static int GFO_NODE_21_RELATIVE_Z = 0;
    public final static double GFO_NODE_21_LONGITUDE = GFO_NODE_21_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_21_LATITUDE = GFO_NODE_21_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_21_ALTITUDE = GFO_NODE_21_RELATIVE_Z;

    public final static int GFO_NODE_22_RELATIVE_X = 30;
    public final static int GFO_NODE_22_RELATIVE_Y = 312;
    public final static int GFO_NODE_22_RELATIVE_Z = 0;
    public final static double GFO_NODE_22_LONGITUDE = GFO_NODE_22_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_22_LATITUDE = GFO_NODE_22_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_22_ALTITUDE = GFO_NODE_22_RELATIVE_Z;

    public final static int GFO_NODE_23_RELATIVE_X = 130;
    public final static int GFO_NODE_23_RELATIVE_Y = 312;
    public final static int GFO_NODE_23_RELATIVE_Z = 0;
    public final static double GFO_NODE_23_LONGITUDE = GFO_NODE_23_RELATIVE_X * TEM_CM_LONGITUDE_X;
    public final static double GFO_NODE_23_LATITUDE = GFO_NODE_23_RELATIVE_Y * TEN_CM_LATITUDE_Y * -1;
    public final static double GFO_NODE_23_ALTITUDE = GFO_NODE_23_RELATIVE_Z;


    public final static double ROUTE_1_INITIAL_POSITION_LATITUDE = GFO_NODE_7_LATITUDE;
    public final static double ROUTE_1_INITIAL_POSITION_LONGITUDE = GFO_NODE_7_LONGITUDE;
    public final static double ROUTE_1_INITIAL_DIRECTION_LATITUDE = GFO_NODE_8_LATITUDE;
    public final static double ROUTE_1_INITIAL_DIRECTION_LONGITUDE = GFO_NODE_8_LONGITUDE;

    public final static double ROUTE_3_INITIAL_POSITION_LATITUDE = GFO_NODE_7_LATITUDE;
    public final static double ROUTE_3_INITIAL_POSITION_LONGITUDE = GFO_NODE_7_LONGITUDE;
    public final static double ROUTE_3_INITIAL_DIRECTION_LATITUDE = GFO_NODE_8_LATITUDE;
    public final static double ROUTE_3_INITIAL_DIRECTION_LONGITUDE = GFO_NODE_8_LONGITUDE;

    public final static double ROUTE_4_INITIAL_POSITION_LATITUDE = GFO_NODE_10_LATITUDE;
    public final static double ROUTE_4_INITIAL_POSITION_LONGITUDE = GFO_NODE_10_LONGITUDE;
    public final static double ROUTE_4_INITIAL_DIRECTION_LATITUDE = GFO_NODE_9_LATITUDE;
    public final static double ROUTE_4_INITIAL_DIRECTION_LONGITUDE = GFO_NODE_9_LONGITUDE;

    public final static double ROUTE_5_INITIAL_POSITION_LATITUDE = GFO_NODE_7_LATITUDE;
    public final static double ROUTE_5_INITIAL_POSITION_LONGITUDE = GFO_NODE_7_LONGITUDE;
    public final static double ROUTE_5_INITIAL_DIRECTION_LATITUDE = GFO_NODE_2_LATITUDE;
    public final static double ROUTE_5_INITIAL_DIRECTION_LONGITUDE = GFO_NODE_2_LONGITUDE;

    public final static double ROUTE_6_INITIAL_POSITION_LATITUDE = GFO_NODE_6_LATITUDE;
    public final static double ROUTE_6_INITIAL_POSITION_LONGITUDE = GFO_NODE_6_LONGITUDE;
    public final static double ROUTE_6_INITIAL_DIRECTION_LATITUDE = GFO_NODE_4_LATITUDE;
    public final static double ROUTE_6_INITIAL_DIRECTION_LONGITUDE = GFO_NODE_4_LONGITUDE;

    public final static double ROUTE_7_INITIAL_POSITION_LATITUDE = GFO_NODE_11_LATITUDE;
    public final static double ROUTE_7_INITIAL_POSITION_LONGITUDE = GFO_NODE_11_LONGITUDE;
    public final static double ROUTE_7_INITIAL_DIRECTION_LATITUDE = GFO_NODE_3_LATITUDE;
    public final static double ROUTE_7_INITIAL_DIRECTION_LONGITUDE = GFO_NODE_3_LONGITUDE;

    private Const() {
    }
}
