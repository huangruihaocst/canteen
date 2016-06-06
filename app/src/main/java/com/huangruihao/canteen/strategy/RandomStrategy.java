package com.huangruihao.canteen.strategy;

import com.huangruihao.canteen.RouletteRandom;

import java.util.Calendar;

/**
 * Created by alexwang on 6/6/16.
 */
public class RandomStrategy implements CanteenSelector {
    String TAOLI_B1 = "紫荆地下1层";
    String TAOLI_1 = "桃李1层";
    String TAOLI_2 = "桃李2层";
    String TAOLI_3 = "桃李3层";
    String ZIJING_B1 = "紫荆地下1层";
    String ZIJING_1 = "紫荆1层";
    String ZIJING_2 = "紫荆2层";
    String ZIJING_3 = "紫荆3层";
    String ZIJING_4 = "紫荆4层";
    String ZHILAN_1 = "芝兰1层";
    String ZHILAN_2 = "芝兰2层";
    String YUSHU_1 = "玉树1层";
    String YUSHU_2 = "玉树1层";
    String QINGFEN_1 = "清芬1层";
    String QINGFEN_2 = "清芬2层";
    String GUANCHOU_1 = "观畴1层";
    String GUANCHOU_2 = "观畴2层";
    String HEYUAN = "荷园";
    String QINGQING = "清青快餐";

    public Object[] getCanteensWithProbability() {
        return new Object[] {
                TAOLI_B1,      1,
                TAOLI_1,       1,
                TAOLI_2,       1,
                TAOLI_3,       1,
                ZIJING_B1,     1,
                ZIJING_1,      1,
                ZIJING_2,      1,
                ZIJING_3,      1,
                ZIJING_4,      1,
                ZHILAN_1,      1,
                ZHILAN_2,      1,
                YUSHU_1,       1,
                YUSHU_2,       1,
                GUANCHOU_1,    1,
                GUANCHOU_2,    1,
                QINGFEN_1,     1,
                QINGFEN_2,     1,
                HEYUAN,        1,
                QINGQING,      1
        };
    }

    @Override
    public String getCanteen() {
        RouletteRandom random = RouletteRandom.fromAssociatedArray(getCanteensWithProbability());
        String canteenName = (String) random.choose();
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 5) // Thursday
            canteenName = "桃李地下1层";
        return canteenName;
    }
}
