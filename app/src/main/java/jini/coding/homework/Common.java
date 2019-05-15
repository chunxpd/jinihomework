package jini.coding.homework;

public class Common {

    private static final String APP_KEY = "AAAAU_Sbjx4:APA91bHihA6UaiNZ2NZyXVj1ScREhKQswLM9JL8SQ0bPV269zDh_GUCfF53Gf3AGq0GsavcPFYu1HJrBIXU1RGU7qE-MACA4JpE-b5hsHT_wr0zL46Z_Z1KN-h66TXreb0v7qJM2omDa";
    private static final String HTTP_HOST = "jinihomework.co.kr";
    private static final int SPLASH_TIME_OUT = 2000;
    private static final String PROTOCOL = "http://";
    private static final String HOMEPAGE = PROTOCOL + HTTP_HOST + "/";
    private static final String ERROR_PAGE = "file:///android_asset/404_Error.html";
    private static final String TOKEN_REGIST_PAGE = HOMEPAGE + "app/mobile_token.php";

    public static String getHttpHost() {
        return HTTP_HOST;
    }

    public static String getAppKey() {
        return APP_KEY;
    }

    public static String getTokenRegistPage() {
        return TOKEN_REGIST_PAGE;
    }

    public static int getSplashTimeOut() {
        return SPLASH_TIME_OUT;
    }

    public static String getHomepage() {
        return HOMEPAGE;
    }

    public static String getError_page() {
        return ERROR_PAGE;
    }
}
