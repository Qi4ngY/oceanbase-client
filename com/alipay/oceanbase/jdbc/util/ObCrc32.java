package com.alipay.oceanbase.jdbc.util;

public class ObCrc32
{
    static final long[] CRC32_TAB;
    
    public static long calculate(final byte[] buf) {
        return calculate(buf, 0, buf.length);
    }
    
    public static long calculate(final byte[] buf, final int offset, int length) {
        long crc = 0L;
        int i = offset;
        while (length > 0) {
            crc = (ObCrc32.CRC32_TAB[(int)((crc ^ (long)buf[i++]) & 0xFFL)] ^ crc >> 8);
            --length;
        }
        return crc;
    }
    
    static {
        CRC32_TAB = new long[] { 0L, 4067132163L, 3778769143L, 324072436L, 3348797215L, 904991772L, 648144872L, 3570033899L, 2329499855L, 2024987596L, 1809983544L, 2575936315L, 1296289744L, 3207089363L, 2893594407L, 1578318884L, 274646895L, 3795141740L, 4049975192L, 51262619L, 3619967088L, 632279923L, 922689671L, 3298075524L, 2592579488L, 1760304291L, 2075979607L, 2312596564L, 1562183871L, 2943781820L, 3156637768L, 1313733451L, 549293790L, 3537243613L, 3246849577L, 871202090L, 3878099393L, 357341890L, 102525238L, 4101499445L, 2858735121L, 1477399826L, 1264559846L, 3107202533L, 1845379342L, 2677391885L, 2361733625L, 2125378298L, 820201905L, 3263744690L, 3520608582L, 598981189L, 4151959214L, 85089709L, 373468761L, 3827903834L, 3124367742L, 1213305469L, 1526817161L, 2842354314L, 2107672161L, 2412447074L, 2627466902L, 1861252501L, 1098587580L, 3004210879L, 2688576843L, 1378610760L, 2262928035L, 1955203488L, 1742404180L, 2511436119L, 3416409459L, 969524848L, 714683780L, 3639785095L, 205050476L, 4266873199L, 3976438427L, 526918040L, 1361435347L, 2739821008L, 2954799652L, 1114974503L, 2529119692L, 1691668175L, 2005155131L, 2247081528L, 3690758684L, 697762079L, 986182379L, 3366744552L, 476452099L, 3993867776L, 4250756596L, 255256311L, 1640403810L, 2477592673L, 2164122517L, 1922457750L, 2791048317L, 1412925310L, 1197962378L, 3037525897L, 3944729517L, 427051182L, 170179418L, 4165941337L, 746937522L, 3740196785L, 3451792453L, 1070968646L, 1905808397L, 2213795598L, 2426610938L, 1657317369L, 3053634322L, 1147748369L, 1463399397L, 2773627110L, 4215344322L, 153784257L, 444234805L, 3893493558L, 1021025245L, 3467647198L, 3722505002L, 797665321L, 2197175160L, 1889384571L, 1674398607L, 2443626636L, 1164749927L, 3070701412L, 2757221520L, 1446797203L, 137323447L, 4198817972L, 3910406976L, 461344835L, 3484808360L, 1037989803L, 781091935L, 3705997148L, 2460548119L, 1623424788L, 1939049696L, 2180517859L, 1429367560L, 2807687179L, 3020495871L, 1180866812L, 410100952L, 3927582683L, 4182430767L, 186734380L, 3756733383L, 763408580L, 1053836080L, 3434856499L, 2722870694L, 1344288421L, 1131464017L, 2971354706L, 1708204729L, 2545590714L, 2229949006L, 1988219213L, 680717673L, 3673779818L, 3383336350L, 1002577565L, 4010310262L, 493091189L, 238226049L, 4233660802L, 2987750089L, 1082061258L, 1395524158L, 2705686845L, 1972364758L, 2279892693L, 2494862625L, 1725896226L, 952904198L, 3399985413L, 3656866545L, 731699698L, 4283874585L, 222117402L, 510512622L, 3959836397L, 3280807620L, 837199303L, 582374963L, 3504198960L, 68661723L, 4135334616L, 3844915500L, 390545967L, 1230274059L, 3141532936L, 2825850620L, 1510247935L, 2395924756L, 2091215383L, 1878366691L, 2644384480L, 3553878443L, 565732008L, 854102364L, 3229815391L, 340358836L, 3861050807L, 4117890627L, 119113024L, 1493875044L, 2875275879L, 3090270611L, 1247431312L, 2660249211L, 1828433272L, 2141937292L, 2378227087L, 3811616794L, 291187481L, 34330861L, 4032846830L, 615137029L, 3603020806L, 3314634738L, 939183345L, 1776939221L, 2609017814L, 2295496738L, 2058945313L, 2926798794L, 1545135305L, 1330124605L, 3173225534L, 4084100981L, 17165430L, 307568514L, 3762199681L, 888469610L, 3332340585L, 3587147933L, 665062302L, 2042050490L, 2346497209L, 2559330125L, 1793573966L, 3190661285L, 1279665062L, 1595330642L, 2910671697L };
    }
}
