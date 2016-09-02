package com.soso.evaextra.pdr;

/**
 * Created by Kingu on 15/08/22.
 */
public class PositionConverter {

    private float distanceBetweenNodes[] = new float[3];
    private double previewNodeLat;
    private double previewNodeLng;
    private double presentNodeLat;
    private double presentNodeLng;

    public PositionConverter(double initialPositionNodeLat, double initialPositionNodeLng, double initialDirectionNodeLat, double initialDirectionNodeLng) {
        distanceBetween(
                initialPositionNodeLat, initialPositionNodeLng,
                initialDirectionNodeLat, initialDirectionNodeLng,
                this.distanceBetweenNodes);

        this.distanceBetweenNodes[1] = loopDirection(this.distanceBetweenNodes[1]);
        this.distanceBetweenNodes[2] = loopDirection(this.distanceBetweenNodes[2]);

        this.previewNodeLat = initialPositionNodeLat;
        this.previewNodeLng = initialPositionNodeLng;
        this.presentNodeLat = initialPositionNodeLat;
        this.presentNodeLng = initialPositionNodeLng;
    }

    private float loopDirection(float direction) {
        if (direction < 0) {
            return direction * -1 - 180;
        } else if (direction > 0) {
            return 180 - direction;
        } else {
            return 180;
        }
    }

    public double[] convert(double previousLatitude, double previousLongitude, double presentLatitude, double presentLongitude) {
        previewNodeLat = presentNodeLat;
        previewNodeLng = presentNodeLng;

        float[] distanceBetweenResults = new float[3];
        distanceBetween(
                previousLatitude,
                previousLongitude,
                presentLatitude,
                presentLongitude,
                distanceBetweenResults);

        double[] resultLatLng = new double[2];
        calculatePosition(
                previewNodeLat,
                previewNodeLng,
                Math.toRadians(distanceBetweenResults[1] * -1 + distanceBetweenNodes[1]),
                distanceBetweenResults[0] * 100,
                resultLatLng);

        presentNodeLat = resultLatLng[0];
        presentNodeLng = resultLatLng[1];

        return resultLatLng;
    }

    public static double latitudeToRelativeY(double latitude) {
        return (latitude / Const.TEN_CM_LATITUDE_Y) / 10;
    }

    public static double longitudeToRelativeX(double longitude) {
        return (longitude / Const.TEM_CM_LONGITUDE_X) / 10;
    }

    public static double latitudeToFloorMapY(double latitude) {
        return latitude / Const.TEN_CM_LATITUDE_Y * -1;
    }

    public static double longitudeToFloorMapX(double longitude) {
        return longitude / Const.TEM_CM_LONGITUDE_X;
    }


    public static double relativeYtoLatitude(double y) {
        return (y * 10) * Const.TEN_CM_LATITUDE_Y * -1;
    }

    public static double relativeXtoLongitude(double x) {
        return (x * 10) * Const.TEM_CM_LONGITUDE_X;
    }

    public static double floorMapYtoLatitude(double y) {
        return (y * 10) * Const.TEN_CM_LATITUDE_Y;
    }

    public static double floorMapXtoLongitude(double x) {
        return (x * 10) * Const.TEM_CM_LONGITUDE_X;
    }

    private void distanceBetween(double startLatitude, double startLongitude,
                                 double endLatitude, double endLongitude, float[] results) {
        if (results == null || results.length < 1) {
            throw new IllegalArgumentException("results is null or has length < 1");
        }
        computeDistanceAndBearing(startLatitude, startLongitude,
                endLatitude, endLongitude, results);
    }

    private void computeDistanceAndBearing(double lat1, double lon1,
                                           double lat2, double lon2, float[] results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    (B / 6.0) * cos2SM *
                                            (-3.0 + 4.0 * sinSigma * sinSigma) *
                                            (-3.0 + 4.0 * cos2SMSq)));

            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                            (sigma + C * sinSigma *
                                    (cos2SM + C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));
        results[0] = distance;
        if (results.length > 1) {
            float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
                    cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
            initialBearing *= 180.0 / Math.PI;
            results[1] = initialBearing;
            if (results.length > 2) {
                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                        -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
                finalBearing *= 180.0 / Math.PI;
                results[2] = finalBearing;
            }
        }
    }

    private void calculatePosition(double latitude, double longitude, double direction, double length, double[] results) {
        final double RX = 40076500;
        final double RY = 40008600;
        results[0] = latitude + (length * Math.sin(direction) / 100 / (RX / 360));
        results[1] = longitude + (length * Math.cos(direction) / 100 / (RY * Math.cos(Math.toRadians(latitude)) / 360));
    }


    public double getPreviewNodeLat() {
        return previewNodeLat;
    }

    public double getPreviewNodeLng() {
        return previewNodeLng;
    }

    public double getPresentNodeLat() {
        return presentNodeLat;
    }

    public double getPresentNodeLng() {
        return presentNodeLng;
    }
}
