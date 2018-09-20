package mad.location.manager.lib.Filters;

/**
 * Created by lezh1k on 2/13/18.
 */

public class GeoHash {

    private static long  interleave(long x, long y) {
        x = (x | (x << 16)) & 0x0000ffff0000ffffL;
        x = (x | (x << 8)) & 0x00ff00ff00ff00ffL;
        x = (x | (x << 4)) & 0x0f0f0f0f0f0f0f0fL;
        x = (x | (x << 2)) & 0x3333333333333333L;
        x = (x | (x << 1)) & 0x5555555555555555L;

        y = (y | (y << 16)) & 0x0000ffff0000ffffL;
        y = (y | (y << 8)) & 0x00ff00ff00ff00ffL;
        y = (y | (y << 4)) & 0x0f0f0f0f0f0f0f0fL;
        y = (y | (y << 2)) & 0x3333333333333333L;
        y = (y | (y << 1)) & 0x5555555555555555L;

        return x | (y << 1);

        //use pdep instructions
//  return _pdep_u64(x, 0x5555555555555555) | _pdep_u64(y, 0xaaaaaaaaaaaaaaaa);
    }

    public static long encode_u64(double lat, double lon, int prec) {
        lat = lat/180.0 + 1.5;
        lon = lon/360.0 + 1.5;
        long ilat = Double.doubleToRawLongBits(lat);
        long ilon = Double.doubleToRawLongBits(lon);
        ilat >>= 20;
        ilon >>= 20;
        ilat &= 0x00000000ffffffffL;
        ilon &= 0x00000000ffffffffL;
        return interleave(ilat, ilon) >> (GEOHASH_MAX_PRECISION-prec)*5;
    }


    static final char base32Table[] = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static final int GEOHASH_MAX_PRECISION = 12;
    public static String geohash_str(long geohash, int prec /*hack. we don't need it, but java hasn't unsigned values*/) {
        StringBuffer buff = new StringBuffer(GEOHASH_MAX_PRECISION);
        geohash >>= 4; //cause we don't need last 4 bits. that's strange, I thought we don't need first 4 bits %)
        geohash &= 0x0fffffffffffffffl; //we don't need sign here
        while (prec-- > 0){
            buff.append(base32Table[(int)(geohash & 0x1f)]);
            geohash >>= 5;
        }
        return buff.reverse().toString();
    }
}
